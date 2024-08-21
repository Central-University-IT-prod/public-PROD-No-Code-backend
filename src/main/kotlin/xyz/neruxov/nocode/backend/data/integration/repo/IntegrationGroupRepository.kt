package xyz.neruxov.nocode.backend.data.integration.repo

import org.springframework.data.jpa.repository.JpaRepository
import xyz.neruxov.nocode.backend.data.integration.model.IntegrationGroup
import java.util.*

interface IntegrationGroupRepository : JpaRepository<IntegrationGroup, Long> {

    fun findByGroupId(groupId: Long): Optional<IntegrationGroup>

    fun existsByGroupId(groupId: Long): Boolean

}