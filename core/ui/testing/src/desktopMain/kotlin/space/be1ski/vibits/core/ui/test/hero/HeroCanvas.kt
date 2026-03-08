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

private val MACBOOK_FOREGROUND_SHADOW_ELEVATION = 24.dp
private val MACBOOK_BACKGROUND_SHADOW_ELEVATION = 14.dp
private val MACBOOK_SHADOW_SHAPE = RoundedCornerShape(10.dp)
private val IPHONE_FOREGROUND_SHADOW_ELEVATION = 20.dp
private val IPHONE_BACKGROUND_SHADOW_ELEVATION = 12.dp
private val IPHONE_SHADOW_SHAPE = RoundedCornerShape(24.dp)

private const val FOREGROUND_Z_THRESHOLD = 4

private data class DeviceLayout(
  val device: HeroDevice,
  val theme: String,
  val screenshot: ImageBitmap,
  val offsetX: Dp,
  val offsetY: Dp,
  val width: Dp,
  val height: Dp,
  val shadowElevation: Dp,
  val shadowShape: RoundedCornerShape,
)

private data class HeroLayout(
  val deviceLayouts: List<DeviceLayout>,
)

private data class PairLayouts(
  val front: DeviceLayout? = null,
  val back: DeviceLayout? = null,
)

private data class Rect(
  val left: Float,
  val top: Float,
  val right: Float,
  val bottom: Float,
)

private fun DeviceLayout.toRect(): Rect =
  Rect(
    left = offsetX.value,
    top = offsetY.value,
    right = offsetX.value + width.value,
    bottom = offsetY.value + height.value,
  )

private fun overlapRatio(
  front: DeviceLayout,
  back: DeviceLayout,
): Float {
  val f = front.toRect()
  val b = back.toRect()
  val overlapW = (minOf(f.right, b.right) - maxOf(f.left, b.left)).coerceAtLeast(0f)
  val overlapH = (minOf(f.bottom, b.bottom) - maxOf(f.top, b.top)).coerceAtLeast(0f)
  val overlapArea = overlapW * overlapH
  val backArea = (b.right - b.left) * (b.bottom - b.top)
  if (backArea <= 0f) return 0f
  return overlapArea / backArea
}

private val PAIR_SUFFIX_REGEX = Regex("-(desktop|mobile)-(front|back)$")

private fun pairKeyFor(
  id: String,
): String? {
  val match = PAIR_SUFFIX_REGEX.find(id) ?: return null
  return id.removeRange(match.range)
}

private fun validatePairOverlaps(deviceLayouts: List<DeviceLayout>) {
  val pairs = mutableMapOf<String, PairLayouts>()
  deviceLayouts.forEach { layout ->
    val key = pairKeyFor(layout.device.id) ?: return@forEach
    val current = pairs[key] ?: PairLayouts()
    val id = layout.device.id
    pairs[key] =
      when {
        id.endsWith("-front") -> current.copy(front = layout)
        id.endsWith("-back") -> current.copy(back = layout)
        else -> current
      }
  }

  pairs.forEach { (key, pair) ->
    val front = pair.front ?: return@forEach
    val back = pair.back ?: return@forEach
    val ratio = overlapRatio(front, back)
    require(ratio <= 0.5f) {
      "Pair overlap for $key is ${(ratio * 100).toInt()}%, expected <= 50%"
    }
  }
}

private fun assignScreenshots(
  config: HeroConfig,
  variant: HeroVariant,
  heroDir: File,
): Map<String, String> {
  val manifest = File(heroDir, "hero-candidates.txt")
  require(manifest.exists()) { "Missing hero-candidates.txt in $heroDir" }

  val candidates = manifest.readLines().filter { it.isNotBlank() }.toSet()
  require(candidates.isNotEmpty()) { "No hero candidates listed in $manifest" }

  val oppositeTheme =
    when (variant) {
      HeroVariant.DARK -> HeroVariant.LIGHT.theme
      HeroVariant.LIGHT -> HeroVariant.DARK.theme
    }

  return config.devices.associate { device ->
    val layout = if (device.type == "macbook") "wide" else "compact"
    val theme =
      if (device.zIndex >= FOREGROUND_Z_THRESHOLD) variant.theme else oppositeTheme
    val filename = "${layout}_${theme}_${device.scenario}.png"
    require(filename in candidates) {
      "Missing screenshot for ${device.id}: $filename"
    }
    device.id to filename
  }
}

private fun deviceTheme(
  device: HeroDevice,
  variant: HeroVariant,
): String {
  val oppositeTheme =
    when (variant) {
      HeroVariant.DARK -> HeroVariant.LIGHT.theme
      HeroVariant.LIGHT -> HeroVariant.DARK.theme
    }
  return if (device.zIndex >= FOREGROUND_Z_THRESHOLD) variant.theme else oppositeTheme
}

private fun computeLayout(
  config: HeroConfig,
  variant: HeroVariant,
  screenshotsDir: File,
  heroDir: File,
): HeroLayout {
  val assignments = assignScreenshots(config, variant, heroDir)

  val deviceLayouts =
    config.devices.sortedBy { it.zIndex }.map { device ->
      val fileName = requireNotNull(assignments[device.id])
      val screenshot =
        org.jetbrains.skia.Image
          .makeFromEncoded(File(screenshotsDir, fileName).readBytes())
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
          "macbook" ->
            if (device.zIndex >= FOREGROUND_Z_THRESHOLD) {
              MACBOOK_FOREGROUND_SHADOW_ELEVATION to MACBOOK_SHADOW_SHAPE
            } else {
              MACBOOK_BACKGROUND_SHADOW_ELEVATION to MACBOOK_SHADOW_SHAPE
            }
          else ->
            if (device.zIndex >= FOREGROUND_Z_THRESHOLD) {
              IPHONE_FOREGROUND_SHADOW_ELEVATION to IPHONE_SHADOW_SHAPE
            } else {
              IPHONE_BACKGROUND_SHADOW_ELEVATION to IPHONE_SHADOW_SHAPE
            }
        }

      DeviceLayout(
        device = device,
        theme = deviceTheme(device, variant),
        screenshot = screenshot,
        offsetX = offsetX,
        offsetY = offsetY,
        width = devW,
        height = devH,
        shadowElevation = shadowElevation,
        shadowShape = shadowShape,
      )
    }

  validatePairOverlaps(deviceLayouts)
  return HeroLayout(deviceLayouts = deviceLayouts)
}

@Composable
private fun HeroDots(config: HeroConfig) {
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
}

@Composable
private fun HeroDevices(layout: HeroLayout) {
  for (dl in layout.deviceLayouts) {
    Box(
      modifier =
        Modifier
          .offset(dl.offsetX, dl.offsetY)
          .graphicsLayer { rotationZ = dl.device.rotate.toFloat() }
          .shadow(dl.shadowElevation, dl.shadowShape),
    ) {
      when (dl.device.type) {
        "macbook" ->
          MacbookFrame(
            screenshot = dl.screenshot,
            screenWidth = dl.device.screenWidth!!.dp,
            theme = dl.theme,
          )
        "iphone" ->
          IphoneFrame(
            screenshot = dl.screenshot,
            bodyWidth = dl.device.bodyWidth!!.dp,
            theme = dl.theme,
          )
      }
    }
  }
}

@Composable
fun HeroCanvas(
  screenshotsDir: File,
  heroDir: File,
  variant: HeroVariant,
  config: HeroConfig = heroConfig,
) {
  val layout =
    remember(variant, config) { computeLayout(config, variant, screenshotsDir, heroDir) }

  Box(Modifier.size(config.canvas.width.dp, config.canvas.height.dp)) {
    HeroDots(config)
    HeroDevices(layout)
  }
}
