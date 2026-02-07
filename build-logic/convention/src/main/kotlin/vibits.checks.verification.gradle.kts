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
  dependsOn(":androidApp:ktlintCheck", ":androidApp:detekt", ":androidApp:compileDebugKotlin")
  dependsOn(":desktopApp:ktlintCheck", ":desktopApp:detekt", ":desktopApp:compileKotlinDesktop")
  dependsOn(":webApp:ktlintCheck", ":webApp:detekt", ":webApp:compileKotlinWasmJs")
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

tasks.register<Copy>("installGitHooks") {
  group = "setup"
  description = "Installs git pre-commit hook"
  from("scripts/pre-commit")
  into(".git/hooks")
  filePermissions {
    unix("rwxr-xr-x")
  }
}
