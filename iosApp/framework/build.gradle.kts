import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  id("vibits.kmp.library")
}

kotlin {
  val xcframework = XCFramework("shared")

  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
      baseName = "shared"
      if (buildType == NativeBuildType.RELEASE) {
        freeCompilerArgs += listOf("-Xdisable-phases=Devirtualization")
      }
      export(projects.feature.homescreen)
      xcframework.add(this)
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        api(projects.feature.homescreen)
      }
    }
    iosMain {
      dependencies {
        api(projects.feature.homescreen)
      }
    }
  }
}
