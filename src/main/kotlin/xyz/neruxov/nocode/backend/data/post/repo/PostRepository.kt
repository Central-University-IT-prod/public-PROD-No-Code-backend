package xyz.neruxov.nocode.backend.data.post.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import xyz.neruxov.nocode.backend.data.post.model.Post
import java.util.*

interface PostRepository : JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.uploadDate >= :firstDate AND p.uploadDate <= :secondDate AND p.organization.id = :organizationId")
    fun findOrganizationPostsByDateRange(firstDate: Date, secondDate: Date, organizationId: Long): List<Post>

    @Query("SELECT p FROM Post p WHERE p.uploadDate >= :firstDate AND p.uploadDate <= :secondDate")
    fun findPostsByDateRange(firstDate: Date, secondDate: Date): List<Post>

    @Query(
        "SELECT p " +
                "FROM Post p " +
                "JOIN p.integrationGroups pig " +
                "WHERE pig.id = :id"
    )
    fun findAllByIntegrationGroupId(id: Long): List<Post>

}