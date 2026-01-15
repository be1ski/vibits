package space.be1ski.vibits.shared.core.ui

internal const val DEMO_PLACEHOLDER_CONTENT = "Hidden content"
internal const val DEMO_PLACEHOLDER_HABIT = "Hidden habit"

internal fun obfuscateIfNeeded(
  text: String,
  enabled: Boolean,
  placeholder: String
): String {
  return if (enabled && text.isNotBlank()) placeholder else text
}
