package space.be1ski.vibits.core.ui.test

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import space.be1ski.vibits.core.ui.theme.VibitsTheme
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

private const val WIDE_WIDTH = 900
private const val WIDE_HEIGHT = 700
private const val COMPACT_WIDTH = 540
private const val COMPACT_HEIGHT = 1080

@OptIn(ExperimentalTestApi::class)
fun runWideUiTest(block: suspend DesktopComposeUiTest.() -> Unit) =
  runDesktopComposeUiTest(width = WIDE_WIDTH, height = WIDE_HEIGHT, block = block)

@OptIn(ExperimentalTestApi::class)
fun runCompactUiTest(block: suspend DesktopComposeUiTest.() -> Unit) =
  runDesktopComposeUiTest(width = COMPACT_WIDTH, height = COMPACT_HEIGHT, block = block)

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
fun ComposeUiTest.captureInBothThemes(
  name: String,
  wideLayout: Boolean = true,
  hero: Boolean = false,
  content: @Composable () -> Unit,
) {
  setThemedContent(darkTheme = false, wideLayout = wideLayout, content = content)
  saveScreenshot("${name}_light", hero = hero)
  setThemedContent(darkTheme = true, wideLayout = wideLayout, content = content)
  saveScreenshot("${name}_dark", hero = hero)
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
  ImageIO.write(composite, "png", File(dir, "$scenario.png"))
  if (hero) {
    File(dir, "hero-candidates.txt").appendText("$scenario.png\n")
  }
}
