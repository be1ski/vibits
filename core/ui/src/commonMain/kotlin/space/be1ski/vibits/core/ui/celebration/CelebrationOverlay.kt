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

  val progress by animateLottieCompositionAsState(composition)

  LaunchedEffect(progress) {
    if (progress == 1f) {
      onFinished()
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
