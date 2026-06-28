package space.be1ski.vibits.core.ui.test

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import space.be1ski.vibits.core.ui.theme.VibitsTheme
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

private const val WIDE_WIDTH = 900
private const val WIDE_HEIGHT = 700
private const val COMPACT_WIDTH = 540
private const val COMPACT_HEIGHT = 1080
private const val HERO_WIDTH = 5600
private const val HERO_HEIGHT = 3200

@OptIn(ExperimentalTestApi::class)
fun runWideUiTest(block: suspend DesktopComposeUiTest.() -> Unit) =
  runDesktopComposeUiTest(width = WIDE_WIDTH, height = WIDE_HEIGHT, block = block)

@OptIn(ExperimentalTestApi::class)
fun runCompactUiTest(block: suspend DesktopComposeUiTest.() -> Unit) =
  runDesktopComposeUiTest(width = COMPACT_WIDTH, height = COMPACT_HEIGHT, block = block)

@OptIn(ExperimentalTestApi::class)
fun runHeroUiTest(block: suspend DesktopComposeUiTest.() -> Unit) =
  runDesktopComposeUiTest(width = HERO_WIDTH, height = HERO_HEIGHT, block = block)

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.saveHeroImage(suffix: String = "") {
  waitForIdle()
  val roots = onAllNodes(isRoot())
  val firstImage = roots[0].captureToImage().toAwtImage()
  val composite = BufferedImage(firstImage.width, firstImage.height, BufferedImage.TYPE_INT_ARGB)
  val g: Graphics2D = composite.createGraphics()
  g.drawImage(firstImage, 0, 0, null)
  val count = roots.fetchSemanticsNodes().size
  for (i in 1 until count) {
    val layerImage = roots[i].captureToImage().toAwtImage()
    g.drawImage(layerImage, 0, 0, null)
  }
  g.dispose()

  val targetW = composite.width / 2
  val targetH = composite.height / 2
  val downscaled = BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB)
  val dg: Graphics2D = downscaled.createGraphics()
  dg.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC)
  dg.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
  dg.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY)
  dg.drawImage(composite, 0, 0, targetW, targetH, null)
  dg.dispose()

  val filename = if (suffix.isEmpty()) "hero.png" else "hero-$suffix.png"
  val dir = File("build/hero").also { it.mkdirs() }
  ImageIO.write(downscaled, "png", File(dir, filename))
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setThemedContent(
  darkTheme: Boolean = false,
  wideLayout: Boolean = true,
  content: @Composable () -> Unit,
) {
  setContent {
    VibitsTheme(darkTheme = darkTheme, wideLayout = wideLayout) {
      content()
    }
  }
}

@OptIn(ExperimentalTestApi::class)
fun captureAllVariants(
  name: String,
  hero: Boolean = false,
  assertions: ComposeUiTest.() -> Unit = {},
  content: @Composable () -> Unit,
) {
  runWideUiTest {
    setThemedContent(darkTheme = false, wideLayout = true, content = content)
    saveScreenshot("wide_light_$name", hero = hero)
    setThemedContent(darkTheme = true, wideLayout = true, content = content)
    saveScreenshot("wide_dark_$name", hero = hero)
    assertions()
  }
  runCompactUiTest {
    setThemedContent(darkTheme = false, wideLayout = false, content = content)
    saveScreenshot("compact_light_$name", hero = hero)
    setThemedContent(darkTheme = true, wideLayout = false, content = content)
    saveScreenshot("compact_dark_$name", hero = hero)
    assertions()
  }
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.saveScreenshot(
  scenario: String,
  hero: Boolean = false,
) {
  waitForIdle()
  val roots = onAllNodes(isRoot())
  val firstImage = roots[0].captureToImage().toAwtImage()
  val composite = BufferedImage(firstImage.width, firstImage.height, BufferedImage.TYPE_INT_ARGB)
  val g: Graphics2D = composite.createGraphics()
  g.drawImage(firstImage, 0, 0, null)
  val count = roots.fetchSemanticsNodes().size
  for (i in 1 until count) {
    val layerImage = roots[i].captureToImage().toAwtImage()
    g.drawImage(layerImage, 0, 0, null)
  }
  g.dispose()
  val dir = File("build/ui-screenshots").also { it.mkdirs() }
  val file = File(dir, "$scenario.png")
  ImageIO.write(composite, "png", file)
  if (hero) {
    val heroDir = File("build/hero").also { it.mkdirs() }
    File(heroDir, "hero-candidates.txt").appendText("${file.name}\n")
  }
}
