package xyz.neruxov.nocode.backend.data.token.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import xyz.neruxov.nocode.backend.data.token.model.RefreshToken
import xyz.neruxov.nocode.backend.data.user.model.User
import java.util.*

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    fun findByToken(token: String): Optional<RefreshToken>

    @Query("SELECT r FROM RefreshToken r WHERE r.expiry > CURRENT_TIMESTAMP AND r.user = :user AND r.revoked = false")
    fun findAllValidByUser(user: User): List<RefreshToken>

}