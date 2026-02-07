/**
 * Project structure checks.
 *
 * Enforces package placement rules:
 * - @Composable code belongs in ui/view packages
 * - expect/actual declarations belong in platform/room packages
 * - Test fakes belong in testing/ modules, not production code
 */

val kotlinSourceSets = listOf("commonMain", "androidMain", "desktopMain", "iosMain", "nonWasmMain", "wasmJsMain")

val uiPackageSegments = listOf(".ui.", ".view.")
val platformPackageSegments = listOf(".platform.", ".room.")
val testPackageSegments = listOf(".test.", ".testing.")

val testFakePrefixes = listOf("Fake", "Recording", "Tracking", "Stateful")

val expectDeclarationPattern = Regex("\\bexpect\\s+(fun|class|interface|object|val|var|abstract|annotation)\\b")

fun String.matchesAnySegment(segments: List<String>): Boolean =
  segments.any { segment -> contains(segment) || endsWith(segment.removeSurrounding(".")) }

tasks.register("checkConventions") {
  group = "verification"
  description = "Checks project structure conventions across source files"
  doLast {
    val violations = mutableListOf<String>()

    subprojects.forEach { sub ->
      kotlinSourceSets.forEach { sourceSet ->
        val srcDir = sub.file("src/$sourceSet/kotlin")
        if (!srcDir.exists()) return@forEach

        srcDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
          val relativePath = file.relativeTo(rootDir).path
          val lines = file.readLines()
          val pkg = lines.firstOrNull { it.startsWith("package ") }
            ?.removePrefix("package ")?.trim() ?: ""
          val content = file.readText()

          val isUiPackage = pkg.matchesAnySegment(uiPackageSegments)
          val isPlatformPackage = pkg.matchesAnySegment(platformPackageSegments)
          val isTestPackage = pkg.matchesAnySegment(testPackageSegments)

          if (!isUiPackage && content.contains("@Composable")) {
            violations += "$relativePath — @Composable found in package '$pkg'. Move to a *.ui.* or *.view.* package."
          }

          if (!isPlatformPackage && expectDeclarationPattern.containsMatchIn(content)) {
            violations += "$relativePath — expect declaration found in package '$pkg'. Move to a *.platform.* or *.room.* package."
          }

          if (sourceSet == "commonMain" && !isTestPackage) {
            val matchedPrefix = testFakePrefixes.firstOrNull { file.name.startsWith(it) }
            if (matchedPrefix != null) {
              violations += "$relativePath — ${matchedPrefix}* class in production code. Move to a testing/ module or commonTest source set."
            }
          }
        }
      }
    }

    if (violations.isNotEmpty()) {
      val summary = violations.joinToString("\n") { "  - $it" }
      throw GradleException("Found ${violations.size} convention violation(s):\n$summary")
    } else {
      logger.lifecycle("Convention check passed — no violations found.")
    }
  }
}
