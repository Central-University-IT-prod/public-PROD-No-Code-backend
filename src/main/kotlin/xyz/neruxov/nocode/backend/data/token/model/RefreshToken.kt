package xyz.neruxov.nocode.backend.data.token.model

import jakarta.persistence.*
import xyz.neruxov.nocode.backend.data.user.model.User
import java.util.*

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(

    @Id
    @GeneratedValue
    val id: Long = 0,

    @Column(unique = true)
    val token: String,

    val expiry: Date,

    var revoked: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User

)