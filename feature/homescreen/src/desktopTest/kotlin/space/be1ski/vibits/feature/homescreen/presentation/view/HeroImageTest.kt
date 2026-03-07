package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.unit.Density
import space.be1ski.vibits.core.ui.test.hero.HeroCanvas
import space.be1ski.vibits.core.ui.test.hero.HeroVariant
import space.be1ski.vibits.core.ui.test.runHeroUiTest
import space.be1ski.vibits.core.ui.test.saveHeroImage
import java.io.File
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HeroImageTest {
  @Test
  fun `generate hero image`() {
    val buildDir = System.getProperty("hero.buildDir") ?: return
    val root = File(buildDir)
    val screenshotsDir = File(root, "ui-screenshots")
    val heroDir = File(root, "hero")

    for (variant in HeroVariant.entries) {
      runHeroUiTest {
        setContent {
          CompositionLocalProvider(LocalDensity provides Density(4f)) {
            HeroCanvas(
              screenshotsDir = screenshotsDir,
              heroDir = heroDir,
              variant = variant,
            )
          }
        }
        saveHeroImage(variant.name.lowercase())
      }
    }
  }
}
