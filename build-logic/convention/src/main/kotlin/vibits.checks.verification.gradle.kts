tasks.register("checkJvm") {
  group = "verification"
  description = "Runs JVM checks: detekt, compile, and tests (Linux-safe)"
  dependsOn("checkConventions")
  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      tasks.findByName("detekt")?.let { dependsOn(it) }
      tasks.findByName("desktopTest")?.let { dependsOn(it) }
    }
  }
  dependsOn(":app:android:ktlintCheck", ":app:android:detekt", ":app:android:compileDebugKotlin")
  dependsOn(":app:desktop:ktlintCheck", ":app:desktop:detekt", ":app:desktop:compileKotlinDesktop")
  dependsOn(":app:web:ktlintCheck", ":app:web:detekt", ":app:web:compileKotlinWasmJs")
}

tasks.register("checkIos") {
  group = "verification"
  description = "Runs iOS checks: compile, ktlint (requires macOS)"
  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      tasks.findByName("compileKotlinIosArm64")?.let { dependsOn(it) }
      tasks.findByName("ktlintCheck")?.let { dependsOn(it) }
    }
  }
}

tasks.register("checkAll") {
  group = "verification"
  description = "Runs all checks including iOS (requires macOS)"
  dependsOn("checkJvm", "checkIos")
}
