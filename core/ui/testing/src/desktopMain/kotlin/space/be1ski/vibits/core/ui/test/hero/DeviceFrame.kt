package space.be1ski.vibits.core.ui.test.hero

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val TRAFFIC_RED = Color(0xFFFF5F57)
private val TRAFFIC_YELLOW = Color(0xFFFEBC2E)
private val TRAFFIC_GREEN = Color(0xFF28C840)

private val BORDER_W = 3.dp
private val BASE_OVERHANG = 5.dp
private val BASE_HEIGHT = 11.dp
private val HINGE_INSET = 20.dp
private val HINGE_HEIGHT = 4.dp
private val SCREEN_CORNER = 10.dp
private val INNER_CORNER = 7.dp
private val BASE_CORNER = 5.dp
private val HINGE_CORNER = 2.dp

private val IPHONE_PADDING = 5.dp
private val IPHONE_CORNER = 24.dp
private val IPHONE_INNER_CORNER = 19.dp

@Composable
fun MacbookFrame(
  screenshot: ImageBitmap,
  screenWidth: Dp,
  theme: String,
) {
  val isLight = theme == "light"
  val borderColor = if (isLight) Color(0xFFC0BCC8) else Color(0xFF2A2A2E)
  val bgColor = if (isLight) Color(0xFFF5F5F7) else Color(0xFF1A1625)
  val titlebarColor = if (isLight) Color(0xFFE8E6EC) else Color(0xFF1E1E22)

  val innerW = screenWidth - BORDER_W * 2
  val aspectRatio = screenshot.width.toFloat() / screenshot.height.toFloat()
  val imgH = innerW / aspectRatio
  val totalW = screenWidth + BASE_OVERHANG * 2

  Column(modifier = Modifier.width(totalW)) {
    MacbookScreen(screenshot, innerW, imgH, borderColor, bgColor, titlebarColor)
    MacbookBase()
  }
}

@Composable
private fun MacbookScreen(
  screenshot: ImageBitmap,
  innerW: Dp,
  imgH: Dp,
  borderColor: Color,
  bgColor: Color,
  titlebarColor: Color,
) {
  // Scale titlebar elements proportionally to screen width
  // Real MacBook: 12px dots, 22px titlebar on ~1440px screen
  val scale = innerW / 1440.dp
  val titlebarH = 22.dp * scale
  val dotSize = 12.dp * scale
  val dotGap = 8.dp * scale
  val dotPadLeft = 14.dp * scale

  Box(
    modifier =
      Modifier
        .padding(horizontal = BASE_OVERHANG)
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = SCREEN_CORNER, topEnd = SCREEN_CORNER))
        .background(borderColor),
  ) {
    Column(
      modifier =
        Modifier
          .padding(BORDER_W)
          .fillMaxWidth()
          .clip(RoundedCornerShape(topStart = INNER_CORNER, topEnd = INNER_CORNER))
          .background(bgColor),
    ) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(titlebarH)
            .background(titlebarColor),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(Modifier.width(dotPadLeft))
        Box(Modifier.size(dotSize).clip(CircleShape).background(TRAFFIC_RED))
        Spacer(Modifier.width(dotGap))
        Box(Modifier.size(dotSize).clip(CircleShape).background(TRAFFIC_YELLOW))
        Spacer(Modifier.width(dotGap))
        Box(Modifier.size(dotSize).clip(CircleShape).background(TRAFFIC_GREEN))
      }
      Image(
        bitmap = screenshot,
        contentDescription = null,
        modifier = Modifier.width(innerW).height(imgH),
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.High,
      )
    }
  }
}

@Composable
private fun MacbookBase() {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(BASE_HEIGHT)
        .clip(RoundedCornerShape(bottomStart = BASE_CORNER, bottomEnd = BASE_CORNER))
        .background(
          Brush.verticalGradient(listOf(Color(0xFFC8C8CD), Color(0xFFB0B0B5))),
        ),
  )
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = HINGE_INSET)
        .height(HINGE_HEIGHT)
        .clip(RoundedCornerShape(bottomStart = HINGE_CORNER, bottomEnd = HINGE_CORNER))
        .background(
          Brush.verticalGradient(listOf(Color(0xFFD8D8DC), Color(0xFFC0C0C5))),
        ),
  )
}

fun macbookSize(
  screenWidth: Int,
  screenshotAspectRatio: Float,
): Pair<Dp, Dp> {
  val innerW = screenWidth.dp - BORDER_W * 2
  val imgH = innerW / screenshotAspectRatio
  val totalW = screenWidth.dp + BASE_OVERHANG * 2
  val scale = innerW / 1440.dp
  val titlebarH = 22.dp * scale
  val totalH = BORDER_W * 2 + titlebarH + imgH + BASE_HEIGHT + HINGE_HEIGHT
  return totalW to totalH
}

@Composable
fun IphoneFrame(
  screenshot: ImageBitmap,
  bodyWidth: Dp,
  theme: String,
) {
  val bgColor = if (theme == "dark") Color(0xFF2A2A2E) else Color(0xFFD0CCD8)

  val innerW = bodyWidth - IPHONE_PADDING * 2
  val aspectRatio = screenshot.width.toFloat() / screenshot.height.toFloat()
  val imgH = innerW / aspectRatio

  Box(
    modifier =
      Modifier
        .width(bodyWidth)
        .height(imgH + IPHONE_PADDING * 2)
        .clip(RoundedCornerShape(IPHONE_CORNER))
        .background(bgColor),
  ) {
    Image(
      bitmap = screenshot,
      contentDescription = null,
      modifier =
        Modifier
          .padding(IPHONE_PADDING)
          .width(innerW)
          .height(imgH)
          .clip(RoundedCornerShape(IPHONE_INNER_CORNER)),
      contentScale = ContentScale.FillBounds,
      filterQuality = FilterQuality.High,
    )
  }
}

fun iphoneSize(
  bodyWidth: Int,
  screenshotAspectRatio: Float,
): Pair<Dp, Dp> {
  val innerW = bodyWidth.dp - IPHONE_PADDING * 2
  val imgH = innerW / screenshotAspectRatio
  return bodyWidth.dp to (imgH + IPHONE_PADDING * 2)
}
