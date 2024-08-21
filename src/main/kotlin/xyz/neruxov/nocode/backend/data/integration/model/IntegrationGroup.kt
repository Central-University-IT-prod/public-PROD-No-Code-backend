package xyz.neruxov.nocode.backend.data.integration.model

import jakarta.persistence.*
import xyz.neruxov.nocode.backend.data.integration.enums.IntegrationType

@Entity
@Table(name = "integration_groups")
data class IntegrationGroup(

    @Id
    @GeneratedValue
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    val type: IntegrationType,

    val groupId: Long,

    @Column(nullable = true)
    val groupAvatar: ByteArray?,

    val groupName: String,

    val organizationId: Long

) {

    fun toMap() = mapOf(
        "id" to id,
        "type" to type,
        "group_id" to groupId,
        "group_avatar" to groupAvatar,
        "group_name" to groupName,
        "organization_id" to organizationId
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntegrationGroup

        if (id != other.id) return false
        if (type != other.type) return false
        if (groupId != other.groupId) return false
        if (groupAvatar != null) {
            if (other.groupAvatar == null) return false
            if (!groupAvatar.contentEquals(other.groupAvatar)) return false
        } else if (other.groupAvatar != null) return false
        if (groupName != other.groupName) return false
        if (organizationId != other.organizationId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + (groupAvatar?.contentHashCode() ?: 0)
        result = 31 * result + groupName.hashCode()
        result = 31 * result + organizationId.hashCode()
        return result
    }

}