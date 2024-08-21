package xyz.neruxov.nocode.backend.data.user.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import xyz.neruxov.nocode.backend.data.user.model.User
import java.util.*

interface UserRepository : JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE lower(u._username) = lower(:username)")
    fun findByUsernameIgnoreCase(username: String): Optional<User>

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE lower(u._username) = lower(:username)")
    fun existsByUsernameIgnoreCase(username: String): Boolean

    @Query("SELECT u FROM User u WHERE u._username LIKE %:username% ORDER BY u._username ASC LIMIT 10")
    fun find10ByUsernameContaining(username: String): List<User>

}