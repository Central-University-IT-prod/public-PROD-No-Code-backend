package xyz.neruxov.nocode.backend.data.post.model

import jakarta.persistence.*
import xyz.neruxov.nocode.backend.data.attachment.model.PostAttachment
import xyz.neruxov.nocode.backend.data.integration.model.IntegrationGroup
import xyz.neruxov.nocode.backend.data.organization.model.Organization
import xyz.neruxov.nocode.backend.data.user.model.User
import java.util.*

@Entity
@Table(name = "posts")
data class Post(

    @Id
    @GeneratedValue
    val id: Long = 0,

    @Column(length = 200)
    var name: String,

    @Column(length = 1024)
    var body: String,

    @ManyToOne
    @JoinColumn(name = "creator_id")
    val creator: User,

    @ManyToOne
    @JoinColumn(name = "organization_id")
    val organization: Organization,

    var uploadDate: Date,

    @Column(name = "tags")
    var tags: Array<String>,

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "postId")
    val attachments: MutableList<PostAttachment> = mutableListOf(),

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "post_integration_groups",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "integration_id")]
    )
    val integrationGroups: MutableList<IntegrationGroup> = mutableListOf(),

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "postId")
    val messages: MutableList<Message> = mutableListOf()

) {

    fun toMap() = mapOf(
        "id" to id,
        "name" to name,
        "body" to body,
        "creator_id" to creator.id,
        "organization_id" to organization.id,
        "upload_date" to uploadDate.time,
        "is_uploaded" to isUploaded(),
        "tags" to tags,
        "attachments" to attachments.map { it.toMap() },
        "integrations" to integrationGroups.map { it.toMap() }
    )

    fun isUploaded(): Boolean {
        val currentDate = Date()
        return currentDate > uploadDate
    }

}