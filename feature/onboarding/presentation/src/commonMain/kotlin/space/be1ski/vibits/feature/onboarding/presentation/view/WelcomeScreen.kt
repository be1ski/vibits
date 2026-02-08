package space.be1ski.vibits.feature.onboarding.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_create_first_habit
import space.be1ski.vibits.core.strings.generated.action_maybe_later
import space.be1ski.vibits.core.strings.generated.msg_offline_onboarding_welcome
import space.be1ski.vibits.core.strings.generated.title_offline_onboarding_welcome
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.theme.AppColors
import space.be1ski.vibits.core.ui.theme.resolve

@Composable
fun WelcomeScreen(
  onContinue: () -> Unit,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val textColor = AppColors.onBackground.resolve()
  Column(
    modifier =
      modifier
        .testTag(OnboardingTestTags.WELCOME_SCREEN)
        .padding(Indent.xl),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Outlined.PhoneAndroid,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(80.dp),
    )

    Spacer(modifier = Modifier.height(Indent.xl))

    Text(
      text = stringResource(Res.string.title_offline_onboarding_welcome),
      style = MaterialTheme.typography.headlineLarge,
      color = textColor,
    )

    Spacer(modifier = Modifier.height(Indent.m))

    Text(
      text = stringResource(Res.string.msg_offline_onboarding_welcome),
      style = MaterialTheme.typography.bodyLarge,
      color = textColor,
    )

    Spacer(modifier = Modifier.height(Indent.x2l))

    Button(
      onClick = onContinue,
      modifier = Modifier.fillMaxWidth().testTag(OnboardingTestTags.WELCOME_CONTINUE_BUTTON),
    ) {
      Text(stringResource(Res.string.action_create_first_habit))
    }

    Spacer(modifier = Modifier.height(Indent.xs))

    TextButton(
      onClick = onSkip,
      modifier = Modifier.fillMaxWidth().testTag(OnboardingTestTags.WELCOME_SKIP_BUTTON),
    ) {
      Text(stringResource(Res.string.action_maybe_later))
    }
  }
}
