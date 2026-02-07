/**
 * Project structure checks.
 *
 * Enforces package placement rules:
 * - @Composable code belongs in ui/view packages
 * - expect/actual declarations belong in platform/room packages
 * - Test fakes belong in testing/ modules, not production code
 * - core/ modules only depend on other core/ modules (never on feature/)
 */

// region Constants

val kotlinSourceSets = listOf("commonMain", "androidMain", "desktopMain", "iosMain", "nonWasmMain", "wasmJsMain")

val uiPackageSegments = listOf(".ui.", ".view.")
val platformPackageSegments = listOf(".platform.", ".room.")
val testPackageSegments = listOf(".test.", ".testing.")

val testFakePrefixes = listOf("Fake", "Recording", "Tracking", "Stateful")

val expectDeclarationPattern = Regex("""\bexpect\s+(fun|class|interface|object|val|var|abstract|annotation)\b""")
val featureDependencyPattern = Regex("""projects\.feature\.|project\(":feature:""")

val coreModulePrefix = ":core:"
val composableAnnotation = "@Composable"
val productionSourceSet = "commonMain"

// endregion

fun String.matchesAnySegment(segments: List<String>): Boolean =
  segments.any { segment -> contains(segment) || endsWith(segment.removeSurrounding(".")) }

fun checkComposablePlacement(
  pkg: String,
  content: String,
  relativePath: String,
  violations: MutableList<String>,
) {
  if (!pkg.matchesAnySegment(uiPackageSegments) && content.contains(composableAnnotation)) {
    violations += "$relativePath — @Composable found in package '$pkg'. Move to a *.ui.* or *.view.* package."
  }
}

fun checkExpectDeclarationPlacement(
  pkg: String,
  content: String,
  relativePath: String,
  violations: MutableList<String>,
) {
  if (!pkg.matchesAnySegment(platformPackageSegments) && expectDeclarationPattern.containsMatchIn(content)) {
    violations += "$relativePath — expect declaration found in package '$pkg'. Move to a *.platform.* or *.room.* package."
  }
}

fun checkTestFakePlacement(
  pkg: String,
  fileName: String,
  sourceSet: String,
  relativePath: String,
  violations: MutableList<String>,
) {
  if (sourceSet != productionSourceSet || pkg.matchesAnySegment(testPackageSegments)) return
  val matchedPrefix = testFakePrefixes.firstOrNull { fileName.startsWith(it) }
  if (matchedPrefix != null) {
    violations += "$relativePath — ${matchedPrefix}* class in production code. Move to a testing/ module or commonTest source set."
  }
}

fun checkCoreDependencies(
  violations: MutableList<String>,
) {
  subprojects.filter { it.path.startsWith(coreModulePrefix) }.forEach { coreProject ->
    val buildFile = coreProject.file("build.gradle.kts")
    if (buildFile.exists() && featureDependencyPattern.containsMatchIn(buildFile.readText())) {
      violations += "${buildFile.relativeTo(rootDir).path} — core/ module depends on feature/. Core modules can only depend on other core/ modules."
    }
  }
}

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

          checkComposablePlacement(pkg, content, relativePath, violations)
          checkExpectDeclarationPlacement(pkg, content, relativePath, violations)
          checkTestFakePlacement(pkg, file.name, sourceSet, relativePath, violations)
        }
      }
    }

    checkCoreDependencies(violations)

    if (violations.isNotEmpty()) {
      val summary = violations.joinToString("\n") { "  - $it" }
      throw GradleException("Found ${violations.size} convention violation(s):\n$summary")
    } else {
      logger.lifecycle("Convention check passed — no violations found.")
    }
  }
}
