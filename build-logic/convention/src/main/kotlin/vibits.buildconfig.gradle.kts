import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

abstract class BuildConfigExtension {
  internal val fields = mutableMapOf<String, String>()

  fun field(name: String, value: String) {
    fields[name] = value
  }
}

val ext = extensions.create<BuildConfigExtension>("generatedConfig")

val packagePrefix = providers.gradleProperty("vibits.packagePrefix")

val generateBuildConfig = tasks.register("generateBuildConfig") {
  val outputDir = layout.buildDirectory.dir("generated/buildconfig")
  val fields = ext.fields.toMap()
  val pathSegments = project.path.split(":").filter { it.isNotEmpty() }
  val packageName = "${packagePrefix.get()}.${pathSegments.joinToString(".")}"

  inputs.property("fields", fields.toString())
  outputs.dir(outputDir)

  doLast {
    val dir = outputDir.get().asFile.resolve(packageName.replace('.', '/'))
    dir.mkdirs()
    dir.resolve("BuildConfig.kt").writeText(
      buildString {
        appendLine("package $packageName")
        appendLine()
        appendLine("object BuildConfig {")
        fields.forEach { (name, value) ->
          appendLine("  const val $name: String = \"$value\"")
        }
        appendLine("}")
      },
    )
  }
}

plugins.withId("org.jetbrains.kotlin.multiplatform") {
  extensions.getByType<KotlinMultiplatformExtension>().sourceSets.getByName("commonMain") {
    kotlin.srcDir(generateBuildConfig)
  }
}
