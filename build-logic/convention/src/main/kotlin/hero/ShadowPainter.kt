package hero

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

object ShadowPainter {

  fun drawWithShadow(
    g2d: Graphics2D,
    deviceImage: BufferedImage,
    x: Int,
    y: Int,
    shadowOffsetY: Int,
    shadowBlur: Int,
    shadowColor: Color,
  ) {
    val padding = shadowBlur * 2
    val w = deviceImage.width + padding * 2
    val h = deviceImage.height + padding * 2

    // Extract alpha channel from device image for shadow shape
    val alpha = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val ag = alpha.createGraphics()
    ag.composite = AlphaComposite.Src
    ag.drawImage(deviceImage, padding, padding + shadowOffsetY, null)
    ag.dispose()

    // Apply box blur 3 times to approximate Gaussian
    val blurred = boxBlur(alpha, shadowBlur / 3)

    // Tint shadow with desired color
    val shadow = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val sg = shadow.createGraphics()
    for (py in 0 until h) {
      for (px in 0 until w) {
        val a = (blurred.getRGB(px, py) ushr 24) and 0xFF
        if (a > 0) {
          val sa = (a * shadowColor.alpha) / 255
          shadow.setRGB(px, py, (sa shl 24) or (shadowColor.rgb and 0x00FFFFFF))
        }
      }
    }
    sg.dispose()

    // Draw shadow then device
    g2d.drawImage(shadow, x - padding, y - padding, null)
    g2d.drawImage(deviceImage, x, y, null)
  }

  private fun boxBlur(src: BufferedImage, radius: Int): BufferedImage {
    if (radius <= 0) return src
    var current = src
    repeat(3) {
      current = horizontalBlur(current, radius)
      current = verticalBlur(current, radius)
    }
    return current
  }

  private fun horizontalBlur(src: BufferedImage, radius: Int): BufferedImage {
    val w = src.width
    val h = src.height
    val dst = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val size = radius * 2 + 1
    for (y in 0 until h) {
      var sumA = 0
      var sumR = 0
      var sumG = 0
      var sumB = 0
      // Initialize window
      for (kx in -radius..radius) {
        val px = kx.coerceIn(0, w - 1)
        val rgb = src.getRGB(px, y)
        sumA += (rgb ushr 24) and 0xFF
        sumR += (rgb ushr 16) and 0xFF
        sumG += (rgb ushr 8) and 0xFF
        sumB += rgb and 0xFF
      }
      for (x in 0 until w) {
        dst.setRGB(x, y, ((sumA / size) shl 24) or ((sumR / size) shl 16) or ((sumG / size) shl 8) or (sumB / size))
        // Slide window
        val removeX = (x - radius).coerceIn(0, w - 1)
        val addX = (x + radius + 1).coerceIn(0, w - 1)
        val removeRgb = src.getRGB(removeX, y)
        val addRgb = src.getRGB(addX, y)
        sumA += ((addRgb ushr 24) and 0xFF) - ((removeRgb ushr 24) and 0xFF)
        sumR += ((addRgb ushr 16) and 0xFF) - ((removeRgb ushr 16) and 0xFF)
        sumG += ((addRgb ushr 8) and 0xFF) - ((removeRgb ushr 8) and 0xFF)
        sumB += (addRgb and 0xFF) - (removeRgb and 0xFF)
      }
    }
    return dst
  }

  private fun verticalBlur(src: BufferedImage, radius: Int): BufferedImage {
    val w = src.width
    val h = src.height
    val dst = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val size = radius * 2 + 1
    for (x in 0 until w) {
      var sumA = 0
      var sumR = 0
      var sumG = 0
      var sumB = 0
      for (ky in -radius..radius) {
        val py = ky.coerceIn(0, h - 1)
        val rgb = src.getRGB(x, py)
        sumA += (rgb ushr 24) and 0xFF
        sumR += (rgb ushr 16) and 0xFF
        sumG += (rgb ushr 8) and 0xFF
        sumB += rgb and 0xFF
      }
      for (y in 0 until h) {
        dst.setRGB(x, y, ((sumA / size) shl 24) or ((sumR / size) shl 16) or ((sumG / size) shl 8) or (sumB / size))
        val removeY = (y - radius).coerceIn(0, h - 1)
        val addY = (y + radius + 1).coerceIn(0, h - 1)
        val removeRgb = src.getRGB(x, removeY)
        val addRgb = src.getRGB(x, addY)
        sumA += ((addRgb ushr 24) and 0xFF) - ((removeRgb ushr 24) and 0xFF)
        sumR += ((addRgb ushr 16) and 0xFF) - ((removeRgb ushr 16) and 0xFF)
        sumG += ((addRgb ushr 8) and 0xFF) - ((removeRgb ushr 8) and 0xFF)
        sumB += (addRgb and 0xFF) - (removeRgb and 0xFF)
      }
    }
    return dst
  }
}
