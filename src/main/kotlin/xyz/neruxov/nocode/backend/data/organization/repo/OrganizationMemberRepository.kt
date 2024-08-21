package xyz.neruxov.nocode.backend.data.organization.repo

import org.springframework.data.jpa.repository.JpaRepository
import xyz.neruxov.nocode.backend.data.organization.enums.OrganizationRole
import xyz.neruxov.nocode.backend.data.organization.model.Organization
import xyz.neruxov.nocode.backend.data.organization.model.OrganizationMember
import java.util.*

interface OrganizationMemberRepository : JpaRepository<OrganizationMember, Long> {

    fun findByOrganizationAndRole(organization: Organization, role: OrganizationRole): Optional<OrganizationMember>

    fun findByOrganization(organization: Organization): List<OrganizationMember>

}