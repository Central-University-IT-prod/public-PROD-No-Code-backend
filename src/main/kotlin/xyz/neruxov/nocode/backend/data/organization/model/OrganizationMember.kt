package xyz.neruxov.nocode.backend.data.organization.model

import jakarta.persistence.*
import xyz.neruxov.nocode.backend.data.organization.enums.OrganizationRole
import xyz.neruxov.nocode.backend.data.user.model.User

@Entity
@Table(name = "organization_members")
data class OrganizationMember(

    @Id
    @GeneratedValue
    val id: Long = 0,

    @ManyToOne
    var organization: Organization,

    @ManyToOne
    var user: User,

    @Enumerated(EnumType.STRING)
    var role: OrganizationRole

) {

    fun toMap() = mapOf(
        "user" to user.toMap(),
        "role" to role
    )

    override fun toString(): String {
        return toMap().toString()
    }

}
