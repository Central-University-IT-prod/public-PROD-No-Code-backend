package xyz.neruxov.nocode.backend.data.user.model

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import xyz.neruxov.nocode.backend.data.mobile.model.MobileDevice
import xyz.neruxov.nocode.backend.data.organization.model.OrganizationMember
import xyz.neruxov.nocode.backend.data.user.enums.UserRole
import java.util.*

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue
    val id: Long = 0,

    @Column(unique = true, name = "username")
    val _username: String,

    var fullName: String,

    @Column(name = "password")
    var _password: String,

    @Enumerated(EnumType.STRING)
    val role: UserRole = UserRole.USER,

    var lastPasswordChange: Date = Date(0),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val organizationMembers: MutableList<OrganizationMember> = mutableListOf(),

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val mobileDevice: MobileDevice? = null

) : UserDetails {

    fun toMap() = mapOf(
        "id" to id,
        "username" to _username,
        "full_name" to fullName,
    )

    override fun getAuthorities(): MutableCollection<GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority(role.name))

    override fun getPassword() = _password

    override fun getUsername() = _username

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true

    fun setPassword(password: String) {
        this._password = password
    }
}