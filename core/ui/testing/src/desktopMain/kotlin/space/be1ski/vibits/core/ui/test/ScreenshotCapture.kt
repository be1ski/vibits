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

private const val DESKTOP_WIDTH = 900
private const val DESKTOP_HEIGHT = 700
private const val MOBILE_WIDTH = 393
private const val MOBILE_HEIGHT = 852

@OptIn(ExperimentalTestApi::class)
fun runDesktopShellUiTest(block: suspend DesktopComposeUiTest.() -> Unit) =
  runDesktopComposeUiTest(width = DESKTOP_WIDTH, height = DESKTOP_HEIGHT, block = block)

@OptIn(ExperimentalTestApi::class)
fun runMobileUiTest(block: suspend DesktopComposeUiTest.() -> Unit) =
  runDesktopComposeUiTest(width = MOBILE_WIDTH, height = MOBILE_HEIGHT, block = block)

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setThemedContent(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  setContent {
    VibitsTheme(darkTheme = darkTheme) {
      content()
    }
  }
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.captureInBothThemes(
  name: String,
  content: @Composable () -> Unit,
) {
  setThemedContent(darkTheme = false, content = content)
  saveScreenshot("light/$name")
  setThemedContent(darkTheme = true, content = content)
  saveScreenshot("dark/$name")
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.saveScreenshot(scenario: String) {
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
  val file = File("build/ui-screenshots/$scenario.png")
  file.parentFile.mkdirs()
  ImageIO.write(composite, "png", file)
}
