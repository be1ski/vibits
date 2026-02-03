package space.be1ski.vibits.feature.memos.domain.model

import space.be1ski.vibits.feature.memos.domain.usecase.ClassifyPostTypeUseCase

data class Memo(
  val name: String = "",
  val content: String = "",
  val createTime: kotlin.time.Instant? = null,
  val updateTime: kotlin.time.Instant? = null,
)

/**
 * Computed property that classifies the memo's post type.
 */
val Memo.postType: PostFilter
  get() = ClassifyPostTypeUseCase(this)

/**
 * Returns true if this is a habits config post.
 */
val Memo.isConfigPost: Boolean
  get() = postType == PostFilter.CONFIG

/**
 * Returns true if this is a habit tracking post.
 */
val Memo.isTrackingPost: Boolean
  get() = postType == PostFilter.HABIT_TRACKING

/**
 * Returns true if this is a regular post (not config or tracking).
 */
val Memo.isRegularPost: Boolean
  get() = postType == PostFilter.REGULAR

/**
 * Returns true if this post can be deleted from the feed.
 * Config and tracking posts should be deleted from their edit dialogs, not from the feed.
 */
val Memo.canDeleteFromFeed: Boolean
  get() = isRegularPost
