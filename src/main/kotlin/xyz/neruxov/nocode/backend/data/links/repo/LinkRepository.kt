package xyz.neruxov.nocode.backend.data.links.repo

import org.springframework.data.jpa.repository.JpaRepository
import xyz.neruxov.nocode.backend.data.links.model.Link
import java.util.*

interface LinkRepository : JpaRepository<Link, Long> {

    fun findByCode(code: String): Optional<Link>

    fun findAllByMessageId(messageId: Long): List<Link>

}