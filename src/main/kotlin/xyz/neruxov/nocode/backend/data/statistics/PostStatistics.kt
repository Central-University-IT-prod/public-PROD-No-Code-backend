package xyz.neruxov.nocode.backend.data.statistics

data class PostStatistics(
    val viewCount: Int,
    val positiveReactionCount: Int,
    val neutralReactionCount: Int,
    val negativeReactionCount: Int,
    val commentsCount: Int,
    val membersCount: Int,
    val linkClicks: Map<String, Int>
)