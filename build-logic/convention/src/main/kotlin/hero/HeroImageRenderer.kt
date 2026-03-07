package hero

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class HeroImageRenderer(
  private val screenshotsDir: File,
  private val outputFile: File,
) {

  private val scale = 2
  private val config = heroConfig

  fun render() {
    val screenshotMap = assignScreenshots()

    val canvasW = config.canvas.width * scale
    val canvasH = config.canvas.height * scale
    val image = BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB)
    val g2d = image.createGraphics()
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

    drawDots(g2d, config.canvas)
    val sortedDevices = config.devices.sortedBy { it.zIndex }
    for (device in sortedDevices) {
      val screenshotName = requireNotNull(screenshotMap[device.id]) { "No screenshot for ${device.id}" }
      drawDevice(g2d, device, config.canvas, screenshotName)
    }

    g2d.dispose()

    outputFile.parentFile.mkdirs()

    val tempPng = File(outputFile.parentFile, "hero-temp.png")
    ImageIO.write(image, "png", tempPng)

    val webpOutput = if (outputFile.extension == "webp") outputFile else File(outputFile.parentFile, outputFile.nameWithoutExtension + ".webp")
    val cwebpResult = runCwebp(tempPng, webpOutput)
    if (cwebpResult) {
      tempPng.delete()
      println("Done: ${webpOutput.name}")
    } else {
      val pngOutput = File(outputFile.parentFile, outputFile.nameWithoutExtension + ".png")
      tempPng.renameTo(pngOutput)
      println("Warning: cwebp not found, saved as ${pngOutput.name}")
    }
  }

  private fun assignScreenshots(): Map<String, String> {
    val heroDir = File(screenshotsDir, "hero")
    require(heroDir.exists()) { "Missing hero screenshots directory: $heroDir" }

    val candidates = heroDir.listFiles().orEmpty()
      .filter { it.isFile && it.extension == "png" }
      .map { it.name }
    require(candidates.isNotEmpty()) { "No hero candidate screenshots in $heroDir" }

    val sorted = candidates.sorted()
    val typeToLayout = mapOf("macbook" to "wide", "iphone" to "compact")
    val usedFiles = mutableSetOf<String>()
    val usedByLayout = mutableMapOf("wide" to mutableSetOf<String>(), "compact" to mutableSetOf<String>())
    val result = mutableMapOf<String, String>()

    for (device in config.devices) {
      val layout = typeToLayout.getValue(device.type)
      val prefix = "${layout}_${device.theme}_"
      val used = usedByLayout.getValue(layout)

      var match = sorted.firstOrNull { f ->
        f.startsWith(prefix) && f !in usedFiles && f.removePrefix(prefix) !in used
      }
      if (match == null) {
        match = sorted.firstOrNull { f -> f.startsWith(prefix) && f !in usedFiles }
      }
      requireNotNull(match) { "No hero candidate for ${device.id} (${prefix}*)" }

      result[device.id] = match
      usedFiles.add(match)
      used.add(match.removePrefix(prefix))
    }

    return result
  }

  private data class Dot(
    val color: String,
    val size: Int,
    val top: Int,
    val left: Int? = null,
    val right: Int? = null,
  )

  private val dots = listOf(
    Dot("purple", 10, 25, left = 200),
    Dot("blue", 8, 12, left = 520),
    Dot("purple", 11, 55, right = 280),
    Dot("blue", 7, 760, left = 90),
    Dot("purple", 9, 740, left = 550),
    Dot("blue", 11, 430, right = 10),
    Dot("purple", 6, 785, left = 900),
    Dot("blue", 9, 780, right = 130),
    Dot("purple", 8, 370, left = 8),
    Dot("blue", 7, 18, right = 40),
    Dot("purple", 7, 570, left = 700),
    Dot("blue", 8, 150, left = 380),
  )

  private fun drawDots(g2d: Graphics2D, canvas: Canvas) {
    val purple = Color(124, 58, 237, (0.45 * 255).toInt())
    val blue = Color(59, 130, 246, (0.5 * 255).toInt())

    for (dot in dots) {
      val color = if (dot.color == "purple") purple else blue
      val right = dot.right
      val x = if (dot.left != null) dot.left * scale else canvas.width * scale - requireNotNull(right) * scale - dot.size * scale
      val y = dot.top * scale
      val size = dot.size * scale
      g2d.color = color
      g2d.fillOval(x, y, size, size)
    }
  }

  private fun drawDevice(g2d: Graphics2D, device: Device, canvas: Canvas, screenshotName: String) {
    val screenshotFile = File(File(screenshotsDir, "hero"), screenshotName)
    val screenshot = ImageIO.read(screenshotFile)

    val deviceImage = when (device.type) {
      "macbook" -> DeviceFramePainter.paintMacbook(
        screenshot,
        requireNotNull(device.screenWidth) { "macbook ${device.id} missing screenWidth" },
        device.theme,
        scale,
      )
      "iphone" -> DeviceFramePainter.paintIphone(
        screenshot,
        requireNotNull(device.bodyWidth) { "iphone ${device.id} missing bodyWidth" },
        device.theme,
        scale,
      )
      else -> error("Unknown device type: ${device.type}")
    }

    val devW = deviceImage.width
    val devH = deviceImage.height
    val pos = device.position
    val x = pos.left?.let { it * scale } ?: (canvas.width * scale - requireNotNull(pos.right) { "${device.id} needs left or right" } * scale - devW)
    val y = pos.top?.let { it * scale } ?: (canvas.height * scale - requireNotNull(pos.bottom) { "${device.id} needs top or bottom" } * scale - devH)

    val (shadowOffsetY, shadowBlur, shadowColor) = when (device.type) {
      "macbook" -> Triple(16 * scale, 32 * scale, Color(0, 0, 0, (0.14 * 255).toInt()))
      else -> Triple(12 * scale, 28 * scale, Color(0, 0, 0, (0.18 * 255).toInt()))
    }

    val oldTransform = g2d.transform
    if (device.rotate != 0) {
      val cx = x + devW / 2.0
      val cy = y + devH / 2.0
      g2d.rotate(Math.toRadians(device.rotate.toDouble()), cx, cy)
    }

    ShadowPainter.drawWithShadow(g2d, deviceImage, x, y, shadowOffsetY, shadowBlur, shadowColor)

    g2d.transform = oldTransform
  }

  private fun runCwebp(input: File, output: File): Boolean {
    return try {
      val process = ProcessBuilder("cwebp", "-q", "90", "-alpha_q", "100", input.absolutePath, "-o", output.absolutePath)
        .redirectErrorStream(true)
        .start()
      val exitCode = process.waitFor()
      exitCode == 0
    } catch (_: Exception) {
      false
    }
  }
}
