tasks.register("installGitHooks") {
  group = "setup"
  description = "Installs git pre-commit hook"

  val hookFile = rootProject.file(".git/hooks/pre-commit")

  outputs.file(hookFile)

  doLast {
    hookFile.parentFile.mkdirs()
    hookFile.writeText(
      """
      |#!/bin/bash
      |
      |echo "Running pre-commit checks..."
      |
      |if ! ./gradlew checkAll --quiet; then
      |  echo ""
      |  echo "Pre-commit verification failed. Please fix the issues above before committing."
      |  exit 1
      |fi
      |
      |echo "Pre-commit verification passed."
      """.trimMargin() + "\n",
    )
    hookFile.setExecutable(true)
  }
}
