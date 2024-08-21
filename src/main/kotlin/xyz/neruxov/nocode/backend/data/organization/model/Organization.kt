package xyz.neruxov.nocode.backend.data.organization.model

import jakarta.persistence.*
import xyz.neruxov.nocode.backend.data.integration.model.IntegrationGroup
import xyz.neruxov.nocode.backend.data.organization.enums.OrganizationRole
import xyz.neruxov.nocode.backend.data.post.model.Post
import xyz.neruxov.nocode.backend.data.user.model.User

@Entity
@Table(name = "organizations")
data class Organization(

    @Id
    @GeneratedValue
    val id: Long = 0,

    var name: String,

    var description: String,

    @OneToMany(mappedBy = "organization", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    val members: MutableList<OrganizationMember> = mutableListOf(),

    @OneToMany(mappedBy = "organization", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    val posts: MutableList<Post> = mutableListOf(),

    @OneToMany(mappedBy = "organizationId", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    val integrations: MutableList<IntegrationGroup> = mutableListOf()

) {

    val owner
        get() = members.find { it.role == OrganizationRole.OWNER }?.user
            ?: throw IllegalStateException("Owner not found")

    fun toMap() = mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "owner_id" to owner.id
    )

    fun removeMember(targetUser: User) {
        members.removeIf { it.user.id == targetUser.id }
    }

    override fun toString(): String {
        return toMap().toString()
    }

}
