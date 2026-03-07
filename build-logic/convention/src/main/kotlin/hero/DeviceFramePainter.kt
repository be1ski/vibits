package hero

import java.awt.Color
import java.awt.GradientPaint
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage

object DeviceFramePainter {

  private val TRAFFIC_RED = Color(0xFF, 0x5F, 0x57)
  private val TRAFFIC_YELLOW = Color(0xFE, 0xBC, 0x2E)
  private val TRAFFIC_GREEN = Color(0x28, 0xC8, 0x40)

  fun paintMacbook(screenshot: BufferedImage, screenWidth: Int, theme: String, scale: Int): BufferedImage {
    val s = scale
    val isLight = theme == "light"
    val borderColor = if (isLight) Color(0xC0, 0xBC, 0xC8) else Color(0x2A, 0x2A, 0x2E)
    val bgColor = if (isLight) Color(0xF5, 0xF5, 0xF7) else Color(0x1A, 0x16, 0x25)
    val titlebarColor = if (isLight) Color(0xE8, 0xE6, 0xEC) else Color(0x1E, 0x1E, 0x22)

    val borderW = 3 * s
    val titlebarH = 26 * s
    val dotSize = 9 * s
    val dotGap = 7 * s
    val dotPadLeft = 11 * s

    // Calculate dimensions — all in scaled pixels
    val totalW = screenWidth * s
    val innerW = totalW - borderW * 2
    val imgH = (screenshot.height.toLong() * innerW / screenshot.width).toInt()
    val screenH = titlebarH + imgH + borderW * 2
    val baseOverhang = 5 * s
    val baseWidth = totalW + baseOverhang * 2
    val baseHeight = 11 * s
    val hingeInset = 20 * s
    val hingeHeight = 4 * s
    val totalHeight = screenH + baseHeight + hingeHeight

    val result = BufferedImage(baseWidth, totalHeight, BufferedImage.TYPE_INT_ARGB)
    val g = result.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

    val screenX = baseOverhang

    // Screen container (rounded top corners only)
    val arcSize = 20.0 * s
    val screenRect = RoundRectangle2D.Double(
      screenX.toDouble(), 0.0,
      totalW.toDouble(), screenH.toDouble(),
      arcSize, arcSize,
    )
    g.color = borderColor
    g.fill(screenRect)
    g.fillRect(screenX, screenH / 2, totalW, screenH / 2)

    // Background inside border
    val innerX = screenX + borderW
    val innerArc = 14.0 * s
    val innerRect = RoundRectangle2D.Double(
      innerX.toDouble(), borderW.toDouble(),
      innerW.toDouble(), (screenH - borderW * 2).toDouble(),
      innerArc, innerArc,
    )
    g.color = bgColor
    g.fill(innerRect)
    g.fillRect(innerX, screenH / 2, innerW, screenH / 2 - borderW)

    // Titlebar
    g.color = titlebarColor
    g.fillRect(innerX, borderW, innerW, titlebarH)

    // Traffic light dots
    val dotY = borderW + (titlebarH - dotSize) / 2
    var dotX = innerX + dotPadLeft
    g.color = TRAFFIC_RED
    g.fillOval(dotX, dotY, dotSize, dotSize)
    dotX += dotSize + dotGap
    g.color = TRAFFIC_YELLOW
    g.fillOval(dotX, dotY, dotSize, dotSize)
    dotX += dotSize + dotGap
    g.color = TRAFFIC_GREEN
    g.fillOval(dotX, dotY, dotSize, dotSize)

    // Screenshot image — drawn at native 2x resolution
    val imgY = borderW + titlebarH
    g.drawImage(screenshot, innerX, imgY, innerW, imgH, null)

    // Base
    val baseY = screenH
    g.paint = GradientPaint(
      0f, baseY.toFloat(), Color(0xC8, 0xC8, 0xCD),
      0f, (baseY + baseHeight).toFloat(), Color(0xB0, 0xB0, 0xB5),
    )
    val baseArc = 10.0 * s
    val baseRect = RoundRectangle2D.Double(
      0.0, baseY.toDouble(),
      baseWidth.toDouble(), baseHeight.toDouble(),
      baseArc, baseArc,
    )
    g.fill(baseRect)
    g.fillRect(0, baseY, baseWidth, baseHeight / 2)

    // Hinge
    val hingeY = baseY + baseHeight
    val hingeX = hingeInset
    val hingeW = baseWidth - hingeInset * 2
    g.paint = GradientPaint(
      0f, hingeY.toFloat(), Color(0xD8, 0xD8, 0xDC),
      0f, (hingeY + hingeHeight).toFloat(), Color(0xC0, 0xC0, 0xC5),
    )
    val hingeArc = 4.0 * s
    val hingeRect = RoundRectangle2D.Double(
      hingeX.toDouble(), hingeY.toDouble(),
      hingeW.toDouble(), hingeHeight.toDouble(),
      hingeArc, hingeArc,
    )
    g.fill(hingeRect)
    g.fillRect(hingeX, hingeY, hingeW, hingeHeight / 2)

    g.dispose()
    return result
  }

  fun paintIphone(screenshot: BufferedImage, bodyWidth: Int, theme: String, scale: Int): BufferedImage {
    val s = scale
    val bgColor = if (theme == "dark") Color(0x2A, 0x2A, 0x2E) else Color(0xD0, 0xCC, 0xD8)
    val padding = 5 * s
    val cornerRadius = 24 * s
    val innerRadius = 19 * s

    val innerW = bodyWidth * s - padding * 2
    val innerH = (screenshot.height.toLong() * innerW / screenshot.width).toInt()
    val totalW = bodyWidth * s
    val totalH = innerH + padding * 2

    val result = BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_ARGB)
    val g = result.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

    // Body background
    g.color = bgColor
    g.fill(RoundRectangle2D.Double(0.0, 0.0, totalW.toDouble(), totalH.toDouble(), cornerRadius * 2.0, cornerRadius * 2.0))

    // Clip screenshot to inner rounded rect
    val clip = RoundRectangle2D.Double(
      padding.toDouble(), padding.toDouble(),
      innerW.toDouble(), innerH.toDouble(),
      innerRadius * 2.0, innerRadius * 2.0,
    )
    g.clip = clip
    g.drawImage(screenshot, padding, padding, innerW, innerH, null)

    g.dispose()
    return result
  }
}
