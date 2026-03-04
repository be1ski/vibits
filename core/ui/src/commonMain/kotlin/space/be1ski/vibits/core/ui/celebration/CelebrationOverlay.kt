package space.be1ski.vibits.core.ui.celebration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import vibits.core.ui.generated.resources.Res

private const val OVERLAY_ALPHA = 0.6f

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CelebrationOverlay(
  animation: CelebrationAnimation?,
  onFinished: () -> Unit,
  frozenProgress: Float? = null,
) {
  var lastAnimation by remember { mutableStateOf(animation) }
  if (animation != null) lastAnimation = animation

  AnimatedVisibility(
    visible = animation != null,
    enter = EnterTransition.None,
    exit = fadeOut(),
  ) {
    val anim = lastAnimation ?: return@AnimatedVisibility

    val composition by rememberLottieComposition {
      when (anim) {
        CelebrationAnimation.Confetti ->
          LottieCompositionSpec.DotLottie(
            Res.readBytes("files/confetti.lottie"),
          )
      }
    }

    val animatedProgress by animateLottieCompositionAsState(composition)
    val progress = frozenProgress ?: animatedProgress

    if (frozenProgress == null) {
      LaunchedEffect(animatedProgress) {
        if (animatedProgress == 1f) {
          onFinished()
        }
      }
    }

    Box(Modifier.fillMaxSize()) {
      Image(
        painter =
          rememberLottiePainter(
            composition = composition,
            progress = { progress },
          ),
        contentDescription = null,
        modifier = Modifier.fillMaxSize().alpha(OVERLAY_ALPHA),
        contentScale = ContentScale.Crop,
      )
    }
  }
}
