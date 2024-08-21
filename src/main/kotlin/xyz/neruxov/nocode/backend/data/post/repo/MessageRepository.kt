package xyz.neruxov.nocode.backend.data.post.repo

import org.springframework.data.jpa.repository.JpaRepository
import xyz.neruxov.nocode.backend.data.integration.model.IntegrationGroup
import xyz.neruxov.nocode.backend.data.post.model.Message
import java.util.*

interface MessageRepository : JpaRepository<Message, Long> {

    fun findMessageByPostIdAndIntegrationGroup(postId: Long, integrationGroup: IntegrationGroup): Optional<Message>

    fun findAllByIntegrationGroupId(id: Long): List<Message>

}