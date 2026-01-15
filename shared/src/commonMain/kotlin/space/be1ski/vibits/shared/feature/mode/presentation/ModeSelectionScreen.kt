package space.be1ski.vibits.shared.feature.mode.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.mode_offline_desc
import space.be1ski.vibits.shared.mode_offline_title
import space.be1ski.vibits.shared.mode_online_desc
import space.be1ski.vibits.shared.mode_online_title
import space.be1ski.vibits.shared.mode_select_subtitle
import space.be1ski.vibits.shared.mode_select_title

@Composable
fun ModeSelectionScreen(
  onModeSelected: (AppMode) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Indent.l),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = stringResource(Res.string.mode_select_title),
      style = MaterialTheme.typography.headlineMedium
    )
    Spacer(modifier = Modifier.height(Indent.xs))
    Text(
      text = stringResource(Res.string.mode_select_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(Indent.l))

    ModeCard(
      title = stringResource(Res.string.mode_online_title),
      description = stringResource(Res.string.mode_online_desc),
      isPrimary = true,
      onClick = { onModeSelected(AppMode.Online) }
    )

    Spacer(modifier = Modifier.height(Indent.m))

    ModeCard(
      title = stringResource(Res.string.mode_offline_title),
      description = stringResource(Res.string.mode_offline_desc),
      isPrimary = false,
      onClick = { onModeSelected(AppMode.Offline) }
    )
  }
}

@Composable
private fun ModeCard(
  title: String,
  description: String,
  isPrimary: Boolean,
  onClick: () -> Unit
) {
  OutlinedCard(
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(Indent.m),
      verticalArrangement = Arrangement.spacedBy(Indent.s)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      if (isPrimary) {
        Button(
          onClick = onClick,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(title)
        }
      } else {
        OutlinedButton(
          onClick = onClick,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(title)
        }
      }
    }
  }
}
