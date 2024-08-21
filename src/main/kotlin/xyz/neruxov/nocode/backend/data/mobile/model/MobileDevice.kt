package xyz.neruxov.nocode.backend.data.mobile.model

import jakarta.persistence.*
import xyz.neruxov.nocode.backend.data.user.model.User

@Entity
@Table(name = "mobile_devices")
data class MobileDevice(

    @Id
    @GeneratedValue
    val id: Long,

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: User,

    var token: String,

    )
