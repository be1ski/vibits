package space.be1ski.vibits.feature.memos.domain.model

/**
 * Post tag constants used throughout the app to identify post types.
 */
object PostTags {
  const val HABITS_CONFIG = "#habits/config"
  const val HABITS_CONFIG_ALT = "#habits_config"
  const val HABITS_DAILY = "#habits/daily"
  const val DAILY = "#daily"

  // Tag prefixes used for parsing and normalization
  const val HABITS_PREFIX = "#habits/"
  const val HABIT_PREFIX = "#habit/"

  // Base hashtag for filtering all habits-related content
  const val HABITS_HASHTAG = "#habits"
}

/**
 * Checks if this string represents a config memo by detecting config tags in content.
 */
fun String.isConfigMemo(): Boolean = contains(PostTags.HABITS_CONFIG) || contains(PostTags.HABITS_CONFIG_ALT)

/**
 * Checks if this string represents a daily habits memo.
 */
fun String.isDailyMemo(): Boolean = contains(PostTags.HABITS_DAILY) || contains(PostTags.DAILY)

/**
 * Checks if this string is a config tag (starts with config prefix).
 */
fun String.isConfigTag(): Boolean = startsWith(PostTags.HABITS_CONFIG) || startsWith(PostTags.HABITS_CONFIG_ALT)

/**
 * Removes habit tag prefixes from this string.
 */
fun String.stripHabitPrefixes(): String = removePrefix(PostTags.HABITS_PREFIX).removePrefix(PostTags.HABIT_PREFIX)
