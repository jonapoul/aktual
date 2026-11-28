package aktual.budget.navrail.ui

import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.DialogTextButton
import aktual.core.ui.PreviewWithColors
import aktual.theme.Colors
import aktual.updates.vm.NightlyUpdateState
import aktual.updates.vm.Version
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun NightlyUpdateDialog(
  state: NightlyUpdateState.Available,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val uriHandler = LocalUriHandler.current
  AktualAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = {
      NightlyUpdateDialogContent(
        state = state,
        onDownload = {
          uriHandler.openUri(state.apkUrl)
          onDismiss()
        },
        onDismiss = onDismiss,
      )
    },
  )
}

@Composable
private fun NightlyUpdateDialogContent(
  state: NightlyUpdateState.Available,
  onDownload: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.updatesNightlyTitle,
    content = {
      Column(verticalArrangement = spacedBy(12.dp)) {
        Text(text = Strings.updatesNightlyMessage)
        Column(verticalArrangement = spacedBy(8.dp)) {
          VersionRow(label = Strings.updatesNightlyInstalled, version = state.installed)
          VersionRow(label = Strings.updatesNightlyLatest, version = state.nightly)
          Text(
            text = Plurals.updatesNightlyCommits(state.commitsAhead, state.commitsAhead),
            style = typography.bodySmall,
            color = colors.textTertiary,
          )
        }
      }
    },
    buttons = {
      DialogTextButton(text = Strings.updatesNightlyLater, onClick = onDismiss)
      DialogTextButton(kind = Primary, text = Strings.updatesNightlyDownload, onClick = onDownload)
    },
  )
}

@Composable
private fun VersionRow(label: String, version: Version, modifier: Modifier = Modifier) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = spacedBy(8.dp)) {
    Text(modifier = Modifier.weight(1f), text = label, color = colors.textTertiary)
    Text(text = Strings.updatesNightlyVersion(version.name, version.gitHash))
  }
}

@Preview
@Composable
private fun PreviewNightlyUpdateDialog(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    NightlyUpdateDialogContent(state = PreviewState, onDownload = {}, onDismiss = {})
  }

private val PreviewState =
  NightlyUpdateState.Available(
    installed = Version(name = "2026.11.18", gitHash = "dbc10893"),
    nightly = Version(name = "2026.11.19", gitHash = "44ef1453"),
    commitsAhead = 7,
    apkUrl = "",
  )
