package xyz.neruxov.nocode.backend.data.post.model

import jakarta.persistence.*
import xyz.neruxov.nocode.backend.data.integration.model.IntegrationGroup

@Entity
@Table(name = "messages")
data class Message(

    @Id
    @GeneratedValue
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "integration_id")
    val integrationGroup: IntegrationGroup,

    val messageId: Long,

    val postId: Long

)
