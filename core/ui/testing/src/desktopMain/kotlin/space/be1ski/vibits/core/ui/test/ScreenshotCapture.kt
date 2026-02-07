package space.be1ski.vibits.core.ui.test

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

private const val APP_WIDTH = 540
private const val APP_HEIGHT = 1080

@OptIn(ExperimentalTestApi::class)
fun runAppUiTest(block: suspend DesktopComposeUiTest.() -> Unit) =
  runDesktopComposeUiTest(width = APP_WIDTH, height = APP_HEIGHT, block = block)

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setThemedContent(content: @Composable () -> Unit) {
  setContent {
    VibitsTheme(darkTheme = false) {
      Surface(modifier = Modifier.fillMaxSize()) {
        content()
      }
    }
  }
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.saveScreenshot(
  feature: String,
  testClass: String,
  scenario: String,
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
  val dir = File("build/ui-screenshots/$feature/$testClass").also { it.mkdirs() }
  ImageIO.write(composite, "png", File(dir, "$scenario.png"))
}
