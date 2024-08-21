package xyz.neruxov.nocode.backend.service

import org.springframework.stereotype.Service
import xyz.neruxov.nocode.backend.data.integration.enums.IntegrationType
import xyz.neruxov.nocode.backend.data.organization.enums.OrganizationRole
import xyz.neruxov.nocode.backend.data.organization.model.Organization
import xyz.neruxov.nocode.backend.data.organization.model.OrganizationMember
import xyz.neruxov.nocode.backend.data.organization.repo.OrganizationMemberRepository
import xyz.neruxov.nocode.backend.data.organization.repo.OrganizationRepository
import xyz.neruxov.nocode.backend.data.user.model.User
import xyz.neruxov.nocode.backend.data.user.repo.UserRepository
import xyz.neruxov.nocode.backend.exception.type.StatusCodeException
import xyz.neruxov.nocode.backend.integration.impl.telegram.TelegramIntegration
import xyz.neruxov.nocode.backend.util.MessageResponse
import kotlin.jvm.optionals.getOrElse

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val organizationMemberRepository: OrganizationMemberRepository,
    private val userRepository: UserRepository,
    val telegramIntegration: TelegramIntegration
) {

    fun getOrganizationById(id: Long): Any = MessageResponse.success(
        organizationRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Организация не найдена")
        }.toMap()
    )

    fun createOrganization(owner: User, name: String, description: String): Any {
        val organization = organizationRepository.save(
            Organization(name = name, description = description)
        )

        organization.members.add(
            OrganizationMember(
                organization = organization,
                user = owner,
                role = OrganizationRole.OWNER
            )
        )

        organizationRepository.save(organization)
        return MessageResponse.success(mapOf("id" to organization.id))
    }

    fun editOrganization(user: User, id: Long, name: String?, description: String?): Any {
        val organization = getOrganizationAndCheckPermissions(user, id)

        organization.apply {
            name?.let { this.name = it }
            description?.let { this.description = it }
        }

        organizationRepository.save(organization)
        return MessageResponse.success()
    }

    fun deleteOrganization(user: User, id: Long): Any {
        val organization = getOrganizationAndCheckPermissions(user, id, OrganizationRole.OWNER)

        user.organizationMembers.removeIf { it.organization.id == organization.id }
        organization.members.forEach {
            it.user.organizationMembers.removeIf { member -> member.organization.id == organization.id }
        }

        organization.members.clear()

        organization.integrations.clear()

        organization.posts.forEach {
            it.integrationGroups.clear()
            it.attachments.clear()
            it.messages.clear()
        }

        organization.posts.clear()

        organizationRepository.save(organization)
        organizationRepository.delete(organization)

        return MessageResponse.success()
    }

    fun getOrganizationMembers(id: Long): Any {
        val organization = getOrganization(id)
        return MessageResponse.success(organization.members.map { it.toMap() })
    }

    fun addOrganizationMember(user: User, id: Long, username: String, organizationRole: OrganizationRole): Any {
        val organization = getOrganizationAndCheckPermissions(user, id)
        if (organizationRole == OrganizationRole.OWNER) {
            throw StatusCodeException(400, "Нельзя добавить владельца")
        }

        val requestedUser = userRepository.findByUsernameIgnoreCase(username).getOrElse {
            throw StatusCodeException(404, "Пользователь не найден")
        }

        if (organization.members.any { it.user.id == requestedUser.id }) {
            throw StatusCodeException(400, "Пользователь уже состоит в организации")
        }

        organization.members.add(
            OrganizationMember(
                organization = organization,
                user = requestedUser,
                role = organizationRole
            )
        )

        organizationRepository.save(organization)

        return MessageResponse.success()
    }

    fun removeOrganizationMember(user: User, id: Long, username: String): Any {
        val organization = getOrganizationAndCheckPermissions(user, id)

        val requestedUser = userRepository.findByUsernameIgnoreCase(username).getOrElse {
            throw StatusCodeException(404, "Пользователь не найден")
        }

        if (requestedUser.id == user.id) {
            throw StatusCodeException(400, "Нельзя исключить себя")
        }

        requestedUser.organizationMembers.removeIf { it.organization.id == organization.id }
        userRepository.save(requestedUser)

        organization.removeMember(requestedUser)
        organizationRepository.save(organization)

        return MessageResponse.success()
    }

    fun editOrganizationMember(user: User, id: Long, username: String, organizationRole: OrganizationRole): Any {
        val organization = getOrganizationAndCheckPermissions(user, id)
        if (organizationRole == OrganizationRole.OWNER) {
            throw StatusCodeException(400, "Нельзя изменить на роль владельца")
        }

        val requestedUser = userRepository.findByUsernameIgnoreCase(username).getOrElse {
            throw StatusCodeException(404, "Пользователь не найден")
        }

        if (requestedUser.id == user.id) {
            throw StatusCodeException(400, "Нельзя изменить свою роль")
        }

        if (organization.members.find { it.user.id == requestedUser.id }?.role == OrganizationRole.OWNER) {
            throw StatusCodeException(400, "Нельзя изменить роль владельца")
        }

        val member = organization.members.find { it.user.id == requestedUser.id } ?: throw StatusCodeException(
            404,
            "Пользователь не состоит в организации"
        )

        member.role = organizationRole
        organizationRepository.save(organization)

        return MessageResponse.success()
    }

    fun getOrganizationIntegrations(user: User, id: Long): Any {
        val organization = getOrganization(id)
        return MessageResponse.success(organization.integrations.map { it.toMap() })
    }

    fun getUserOrganizations(user: User): Any {
        return MessageResponse.success(user.organizationMembers.map { it.organization.toMap() })
    }

    fun getOrganizationIntegrationAddLink(user: User, id: Long, type: IntegrationType): Any {
        val organization = getOrganizationAndCheckPermissions(user, id)
        return MessageResponse.success(
            when (type) {
                IntegrationType.TELEGRAM -> telegramIntegration.getAddLink(organization)
            }
        )
    }

    private fun getOrganizationAndCheckPermissions(
        user: User,
        id: Long,
        role: OrganizationRole = OrganizationRole.ADMIN
    ): Organization {
        val organization = getOrganization(id)
        checkOrganizationPermissions(user, organization, role)
        return organization
    }

    private fun getOrganization(id: Long): Organization {
        val organization = organizationRepository.findById(id).getOrElse {
            throw StatusCodeException(404, "Организация не найдена")
        }

        return organization
    }

    private fun checkOrganizationPermissions(user: User, organization: Organization, role: OrganizationRole) {
        if (!organization.members.find { it.user.id == user.id }!!.role.isEqualOrHigherThan(role)) {
            throw StatusCodeException(403, "Вы не имеете права выполнять данное действие")
        }
    }

}