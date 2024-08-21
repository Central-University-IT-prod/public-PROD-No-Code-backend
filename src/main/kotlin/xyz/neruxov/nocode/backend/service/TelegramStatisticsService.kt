package xyz.neruxov.nocode.backend.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import xyz.neruxov.nocode.backend.data.integration.model.IntegrationGroup
import xyz.neruxov.nocode.backend.data.links.repo.LinkRepository
import xyz.neruxov.nocode.backend.data.statistics.PostStatistics
import xyz.neruxov.nocode.backend.data.statistics.ReactionsCount
import xyz.neruxov.nocode.backend.integration.impl.telegram.TelegramBotController
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Service
class TelegramStatisticsService(
    val botController: TelegramBotController,
    val linkRepository: LinkRepository
) {

    @Value("\${service.telegram.statistics.url}")
    val url: String = ""

    private val gson = Gson()
    private val httpClient = HttpClient.newHttpClient()

    private val viewCountCache = Caffeine.newBuilder()
        .expireAfterWrite(3.minutes.toJavaDuration())
        .buildAsync<Pair<Long, Long>, Int> { (peerId, messageId) ->
            getRequest("$url/views?peerId=$peerId&messageId=$messageId")?.getCount() ?: 0
        }

    private val commentsCountCache = Caffeine.newBuilder()
        .expireAfterWrite(3.minutes.toJavaDuration())
        .buildAsync<Pair<Long, Long>, Int> { (peerId, messageId) ->
            getRequest("$url/comments?peerId=$peerId&messageId=$messageId")?.getCount() ?: 0
        }

    private val membersCountCache = Caffeine.newBuilder()
        .expireAfterWrite(3.minutes.toJavaDuration())
        .buildAsync<Long, Int> { groupId ->
            botController.getMembersCount(groupId)
        }

    private val reactionsCache = Caffeine.newBuilder()
        .expireAfterWrite(3.minutes.toJavaDuration())
        .buildAsync<Pair<Long, Long>, ReactionsCount> { (peerId, messageId) ->
            getRequest("$url/reactions?peerId=$peerId&messageId=$messageId")?.let {
                gson.fromJson(it, JsonObject::class.java).let { obj ->
                    ReactionsCount(
                        obj["positive_count"].asInt,
                        obj["neutral_count"].asInt,
                        obj["negative_count"].asInt
                    )
                }
            } ?: ReactionsCount(0, 0, 0)
        }

    fun getViewCount(groupId: Long, messageId: Long): Int {
        return viewCountCache.get(groupId to messageId).join()
    }

    fun getCommentsCount(groupId: Long, messageId: Long): Int {
        return commentsCountCache.get(groupId to messageId).join()
    }

    fun getMembersCount(groupId: Long): Int {
        return membersCountCache.get(groupId).join()
    }

    fun getReactions(groupId: Long, messageId: Long): ReactionsCount {
        return reactionsCache.get(groupId to messageId).join()
    }

    fun getStatistics(group: IntegrationGroup, messageId: Long): PostStatistics {
        val (positiveReactions, neutralReactions, negativeReactions) = getReactions(group.groupId, messageId)
        return PostStatistics(
            getViewCount(group.groupId, messageId),
            positiveReactions,
            neutralReactions,
            negativeReactions,
            getCommentsCount(group.groupId, messageId),
            getMembersCount(group.groupId),
            getLinkClicks(messageId)
        )
    }

    fun getLinkClicks(messageId: Long): Map<String, Int> {
        val links = linkRepository.findAllByMessageId(messageId)
        val result = mutableMapOf<String, Int>()

        links.forEach {
            result.getOrDefault(it.url, 0) + 1
        }

        return result
    }

    private fun getRequest(uri: String): String? = try {
        val response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build(),
            BodyHandlers.ofString()
        )

        if (response.statusCode() != 200) throw RuntimeException()
        response.body()
    } catch (ex: Exception) {
        null
    }

    private fun String.getCount() = try {
        gson.fromJson(this, JsonObject::class.java).get("count").asInt
    } catch (ex: Exception) {
        0
    }

}
