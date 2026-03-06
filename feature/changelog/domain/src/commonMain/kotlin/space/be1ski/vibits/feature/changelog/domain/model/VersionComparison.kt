package space.be1ski.vibits.feature.changelog.domain.model

fun parseVersion(version: String): List<Int>? {
  val cleaned = version.removePrefix("v")
  val parts = cleaned.split(".")
  val numbers = parts.mapNotNull { it.toIntOrNull() }
  return if (numbers.size == parts.size && numbers.isNotEmpty()) numbers else null
}

fun compareVersions(
  a: List<Int>,
  b: List<Int>,
): Int {
  val maxLen = maxOf(a.size, b.size)
  for (i in 0 until maxLen) {
    val partA = a.getOrElse(i) { 0 }
    val partB = b.getOrElse(i) { 0 }
    if (partA != partB) return partA.compareTo(partB)
  }
  return 0
}
