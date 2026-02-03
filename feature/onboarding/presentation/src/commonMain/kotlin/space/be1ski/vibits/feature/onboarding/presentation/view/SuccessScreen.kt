package space.be1ski.vibits.feature.onboarding.presentation.view
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_go_to_dashboard
import space.be1ski.vibits.core.strings.generated.action_mark_first_checkin
import space.be1ski.vibits.core.strings.generated.msg_offline_onboarding_success
import space.be1ski.vibits.core.strings.generated.title_offline_onboarding_success
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.theme.AppColors
import space.be1ski.vibits.core.ui.theme.resolve

@Composable
fun SuccessScreen(
  onMarkFirstCheckIn: () -> Unit,
  onGoToDashboard: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val textColor = AppColors.onBackground.resolve()
  Column(
    modifier =
      modifier
        .padding(Indent.xl),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Default.CheckCircle,
      contentDescription = null,
      tint = AppColors.habitGreen.resolve(),
      modifier = Modifier.size(80.dp),
    )

    Spacer(modifier = Modifier.height(Indent.xl))

    Text(
      text = stringResource(Res.string.title_offline_onboarding_success),
      style = MaterialTheme.typography.headlineLarge,
      color = textColor,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(Indent.m))

    Text(
      text = stringResource(Res.string.msg_offline_onboarding_success),
      style = MaterialTheme.typography.bodyLarge,
      color = textColor,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(Indent.x2l))

    Button(
      onClick = onMarkFirstCheckIn,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(stringResource(Res.string.action_mark_first_checkin))
    }

    Spacer(modifier = Modifier.height(Indent.xs))

    TextButton(
      onClick = onGoToDashboard,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(stringResource(Res.string.action_go_to_dashboard))
    }
  }
}
