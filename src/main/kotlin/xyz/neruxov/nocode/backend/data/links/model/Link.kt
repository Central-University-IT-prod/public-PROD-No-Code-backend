package xyz.neruxov.nocode.backend.data.links.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "links")
data class Link(

    @Id
    @GeneratedValue
    val id: Long = 0,

    val url: String,

    val messageId: Long,

    val code: String,

    var clicks: Long = 0

)
