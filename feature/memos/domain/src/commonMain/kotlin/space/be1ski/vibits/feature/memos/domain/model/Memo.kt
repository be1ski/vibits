package space.be1ski.vibits.feature.memos.domain.model

import space.be1ski.vibits.feature.memos.domain.usecase.ClassifyPostTypeUseCase

data class Memo(
  val name: String = "",
  val content: String = "",
  val createTime: kotlin.time.Instant? = null,
  val updateTime: kotlin.time.Instant? = null,
)

val Memo.postType: PostFilter
  get() = ClassifyPostTypeUseCase(this)

val Memo.isConfigPost: Boolean
  get() = postType == PostFilter.CONFIG

val Memo.isTrackingPost: Boolean
  get() = postType == PostFilter.HABIT_TRACKING

val Memo.isRegularPost: Boolean
  get() = postType == PostFilter.REGULAR

/**
 * Config and tracking posts should be deleted from their edit dialogs, not from the feed.
 */
val Memo.canDeleteFromFeed: Boolean
  get() = isRegularPost
