package space.be1ski.vibits.core.ui.test.hero

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File

private val PURPLE_DOT = Color(124, 58, 237, (0.45f * 255).toInt())
private val BLUE_DOT = Color(59, 130, 246, (0.5f * 255).toInt())

private val MACBOOK_SHADOW_ELEVATION = 24.dp
private val MACBOOK_SHADOW_SHAPE = RoundedCornerShape(10.dp)
private val IPHONE_SHADOW_ELEVATION = 20.dp
private val IPHONE_SHADOW_SHAPE = RoundedCornerShape(24.dp)

private data class DeviceLayout(
  val device: HeroDevice,
  val screenshot: ImageBitmap,
  val offsetX: Dp,
  val offsetY: Dp,
  val shadowElevation: Dp,
  val shadowShape: RoundedCornerShape,
)

private data class HeroLayout(
  val deviceLayouts: List<DeviceLayout>,
)

private fun assignScreenshots(
  config: HeroConfig,
  screenshotsDir: File,
): Map<String, String> {
  val heroDir = File(screenshotsDir, "hero")
  require(heroDir.exists()) { "Missing hero screenshots directory: $heroDir" }

  val candidates =
    heroDir
      .listFiles()
      .orEmpty()
      .filter { it.isFile && it.extension == "png" }
      .map { it.name }
  require(candidates.isNotEmpty()) { "No hero candidate screenshots in $heroDir" }

  val sorted = candidates.sorted()
  val typeToLayout = mapOf("macbook" to "wide", "iphone" to "compact")
  val usedFiles = mutableSetOf<String>()
  val usedByLayout =
    mutableMapOf(
      "wide" to mutableSetOf<String>(),
      "compact" to mutableSetOf<String>(),
    )
  val result = mutableMapOf<String, String>()

  for (device in config.devices) {
    val layout = typeToLayout.getValue(device.type)
    val prefix = "${layout}_${device.theme}_"
    val used = usedByLayout.getValue(layout)

    var match =
      sorted.firstOrNull { f ->
        f.startsWith(prefix) && f !in usedFiles && f.removePrefix(prefix) !in used
      }
    if (match == null) {
      match = sorted.firstOrNull { f -> f.startsWith(prefix) && f !in usedFiles }
    }
    requireNotNull(match) { "No hero candidate for ${device.id} ($prefix*)" }

    result[device.id] = match
    usedFiles.add(match)
    used.add(match.removePrefix(prefix))
  }

  return result
}

private fun computeLayout(
  config: HeroConfig,
  screenshotsDir: File,
): HeroLayout {
  val assignments = assignScreenshots(config, screenshotsDir)
  val heroDir = File(screenshotsDir, "hero")

  val deviceLayouts =
    config.devices.sortedBy { it.zIndex }.map { device ->
      val fileName = requireNotNull(assignments[device.id])
      val screenshot =
        org.jetbrains.skia.Image
          .makeFromEncoded(File(heroDir, fileName).readBytes())
          .toComposeImageBitmap()
      val aspectRatio = screenshot.width.toFloat() / screenshot.height.toFloat()

      val (devW, devH) =
        when (device.type) {
          "macbook" -> macbookSize(device.screenWidth!!, aspectRatio)
          "iphone" -> iphoneSize(device.bodyWidth!!, aspectRatio)
          else -> error("Unknown device type: ${device.type}")
        }

      val pos = device.position
      val offsetX =
        pos.left?.dp
          ?: (config.canvas.width.dp - pos.right!!.dp - devW)
      val offsetY =
        pos.top?.dp
          ?: (config.canvas.height.dp - pos.bottom!!.dp - devH)

      val (shadowElevation, shadowShape) =
        when (device.type) {
          "macbook" -> MACBOOK_SHADOW_ELEVATION to MACBOOK_SHADOW_SHAPE
          else -> IPHONE_SHADOW_ELEVATION to IPHONE_SHADOW_SHAPE
        }

      DeviceLayout(
        device = device,
        screenshot = screenshot,
        offsetX = offsetX,
        offsetY = offsetY,
        shadowElevation = shadowElevation,
        shadowShape = shadowShape,
      )
    }

  return HeroLayout(deviceLayouts = deviceLayouts)
}

@Composable
fun HeroCanvas(screenshotsDir: File) {
  val config = heroConfig
  val layout = remember { computeLayout(config, screenshotsDir) }

  Box(Modifier.size(config.canvas.width.dp, config.canvas.height.dp)) {
    // Decorative dots
    for (dot in config.dots) {
      val dotColor = if (dot.color == "purple") PURPLE_DOT else BLUE_DOT
      val x =
        dot.left?.dp
          ?: (config.canvas.width.dp - dot.right!!.dp - dot.size.dp)
      Box(
        Modifier
          .offset(x, dot.top.dp)
          .size(dot.size.dp)
          .clip(CircleShape)
          .background(dotColor),
      )
    }

    // Devices sorted by zIndex
    for (dl in layout.deviceLayouts) {
      Box(
        modifier =
          Modifier
            .offset(dl.offsetX, dl.offsetY)
            .graphicsLayer {
              rotationZ = dl.device.rotate.toFloat()
            }.shadow(dl.shadowElevation, dl.shadowShape),
      ) {
        when (dl.device.type) {
          "macbook" ->
            MacbookFrame(
              screenshot = dl.screenshot,
              screenWidth = dl.device.screenWidth!!.dp,
              theme = dl.device.theme,
            )
          "iphone" ->
            IphoneFrame(
              screenshot = dl.screenshot,
              bodyWidth = dl.device.bodyWidth!!.dp,
              theme = dl.device.theme,
            )
        }
      }
    }
  }
}
