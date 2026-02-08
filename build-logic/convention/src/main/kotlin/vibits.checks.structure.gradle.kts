/**
 * Project structure checks.
 *
 * Enforces package placement rules:
 * - @Composable code belongs in ui/view packages
 * - expect/actual declarations belong in platform/room packages
 * - Test fakes belong in testing/ modules, not production code
 * - core/ modules only depend on other core/ modules (never on feature/)
 * - Feature layers follow dependency rules: domain <- data, domain <- presentation
 * - No empty modules (registered in settings.gradle.kts but containing no Kotlin sources)
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
val featureModulePrefix = ":feature:"
val composableAnnotation = "@Composable"
val productionSourceSet = "commonMain"

val featureLayers = setOf("domain", "data", "presentation")
val exemptFeatures = setOf("homescreen")

// Matches feature deps like: projects.feature.memos.data or project(":feature:memos:data")
// Captures: feature name and layer
val typeSafeFeatureDepPattern = Regex("""projects\.feature\.(\w+)\.(domain|data|presentation)(?!\.\w)""")
val stringFeatureDepPattern = Regex("""project\(":feature:(\w+):(domain|data|presentation)"\)""")

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

/**
 * Extracts feature name and layer from a project path like ":feature:habits:domain".
 * Returns null for non-feature, exempt, or testing modules.
 */
fun extractFeatureLayer(projectPath: String): Pair<String, String>? {
  if (!projectPath.startsWith(featureModulePrefix)) return null
  val segments = projectPath.removePrefix(featureModulePrefix).split(":")
  if (segments.size < 2) return null
  val featureName = segments[0]
  val layer = segments[1]
  if (featureName in exemptFeatures) return null
  if (layer !in featureLayers) return null
  if (segments.size > 2 && segments[2] == "testing") return null
  return featureName to layer
}

/**
 * Extracts the content of the commonMain { } block from a build file.
 * Platform source sets (nonWasmMain, androidMain, etc.) may have legitimate cross-layer
 * dependencies (e.g., Room shared database), so only commonMain is checked.
 * Looks for `commonMain {` or `commonMain{` pattern to find the block declaration.
 */
fun extractCommonMainBlock(text: String): String {
  val pattern = Regex("""commonMain\s*\{""")
  val match = pattern.find(text) ?: return ""
  val blockStart = match.range.last // position of '{'
  var depth = 1
  for (i in (blockStart + 1) until text.length) {
    if (text[i] == '{') depth++
    else if (text[i] == '}') {
      depth--
      if (depth == 0) return text.substring(blockStart, i + 1)
    }
  }
  return text.substring(blockStart)
}

fun checkFeatureLayerDependencies(
  violations: MutableList<String>,
) {
  subprojects.forEach { sub ->
    val (featureName, layer) = extractFeatureLayer(sub.path) ?: return@forEach
    val buildFile = sub.file("build.gradle.kts")
    if (!buildFile.exists()) return@forEach

    // Only check commonMain dependencies (platform source sets may have legitimate cross-layer deps for Room)
    val content = extractCommonMainBlock(buildFile.readText())
    val relativePath = buildFile.relativeTo(rootDir).path

    // Collect all feature dependencies (excluding .testing references)
    val deps = mutableListOf<Triple<String, String, String>>() // depFeature, depLayer, matchText
    typeSafeFeatureDepPattern.findAll(content).forEach { match ->
      deps += Triple(match.groupValues[1], match.groupValues[2], match.value)
    }
    stringFeatureDepPattern.findAll(content).forEach { match ->
      deps += Triple(match.groupValues[1], match.groupValues[2], match.value)
    }

    for ((depFeature, depLayer, matchText) in deps) {
      val isSameFeature = depFeature == featureName
      val isCrossFeature = !isSameFeature

      when (layer) {
        "domain" -> {
          if (depLayer == "data" || depLayer == "presentation") {
            violations += "$relativePath — domain depends on $depLayer ($matchText). Domain can only depend on other domains."
          }
        }
        "data" -> {
          if (depLayer == "presentation") {
            violations += "$relativePath — data depends on presentation ($matchText). Data cannot depend on presentation."
          }
          if (isCrossFeature && depLayer == "data") {
            violations += "$relativePath — data depends on other feature's data ($matchText). Cross-feature dependencies are only allowed on domain."
          }
        }
        "presentation" -> {
          if (depLayer == "data") {
            violations += "$relativePath — presentation depends on data ($matchText). Presentation cannot depend on data."
          }
          if (isCrossFeature && depLayer == "presentation") {
            violations += "$relativePath — presentation depends on other feature's presentation ($matchText). Cross-feature dependencies are only allowed on domain."
          }
        }
      }
    }
  }
}

fun checkEmptyModules(violations: MutableList<String>) {
  val allPaths = subprojects.map { it.path }.toSet()
  subprojects
    .filter { it.path.startsWith(featureModulePrefix) || it.path.startsWith(coreModulePrefix) }
    .filter { sub -> allPaths.none { it != sub.path && it.startsWith("${sub.path}:") } }
    .forEach { sub ->
      val srcDir = sub.file("src")
      val hasFiles = srcDir.exists() && srcDir.walkTopDown().any { it.isFile }
      if (!hasFiles) {
        violations += "${sub.path} — module has no source files. Remove it from settings.gradle.kts or add source files."
      }
    }
}

tasks.register("checkConventions") {
  group = "verification"
  description = "Checks project structure conventions across source files"
  notCompatibleWithConfigurationCache("walks subproject source trees at execution time")
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
    checkFeatureLayerDependencies(violations)
    checkEmptyModules(violations)

    if (violations.isNotEmpty()) {
      val summary = violations.joinToString("\n") { "  - $it" }
      throw GradleException("Found ${violations.size} convention violation(s):\n$summary")
    } else {
      logger.lifecycle("Convention check passed — no violations found.")
    }
  }
}
