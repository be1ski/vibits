package space.be1ski.vibits.core.ui.celebration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import vibits.core.ui.generated.resources.Res

private const val OVERLAY_ALPHA = 0.6f
private const val FADE_OUT_START = 0.85f

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CelebrationOverlay(
  animation: CelebrationAnimation?,
  onFinished: () -> Unit,
  frozenProgress: Float? = null,
) {
  if (animation == null) return

  val composition by rememberLottieComposition {
    when (animation) {
      CelebrationAnimation.Confetti ->
        LottieCompositionSpec.DotLottie(
          Res.readBytes("files/confetti.lottie"),
        )
    }
  }

  val animatedProgress by animateLottieCompositionAsState(composition)

  if (frozenProgress == null) {
    LaunchedEffect(animatedProgress) {
      if (animatedProgress == 1f) {
        onFinished()
      }
    }
  }

  val currentAlpha =
    when {
      frozenProgress != null -> OVERLAY_ALPHA
      animatedProgress > FADE_OUT_START ->
        OVERLAY_ALPHA * (1f - animatedProgress) / (1f - FADE_OUT_START)
      else -> OVERLAY_ALPHA
    }

  Box(Modifier.fillMaxSize().then(if (composition != null) Modifier.testTag(CELEBRATION_OVERLAY_TEST_TAG) else Modifier)) {
    Image(
      painter =
        rememberLottiePainter(
          composition = composition,
          progress = { frozenProgress ?: animatedProgress },
        ),
      contentDescription = null,
      modifier = Modifier.fillMaxSize().alpha(currentAlpha),
      contentScale = ContentScale.Crop,
    )
  }
}

const val CELEBRATION_OVERLAY_TEST_TAG = "celebration_overlay"
