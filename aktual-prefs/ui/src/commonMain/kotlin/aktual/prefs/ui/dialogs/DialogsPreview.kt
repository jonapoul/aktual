package aktual.prefs.ui.dialogs

import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.DialogTextButton
import aktual.core.ui.Dimens
import aktual.core.ui.HazeConfig
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.dialogBlur
import aktual.prefs.ui.SampleTransactionList
import aktual.theme.Colors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

// A dialog always shown over made-up transactions, blurred the same way the app blurs behind real
// dialogs
@Composable
internal fun DialogsPreview(config: HazeConfig, modifier: Modifier = Modifier) {
  val hazeState = rememberHazeState()
  Box(
    modifier = modifier.fillMaxWidth().height(PreviewHeight).clip(CardShape),
    contentAlignment = Center,
  ) {
    SampleTransactionList(
      modifier = Modifier.matchParentSize().hazeSource(hazeState).background(colors.backgroundPage),
    )

    if (config.dialogs) {
      Box(modifier = Modifier.matchParentSize().dialogBlur(hazeState, config))
    }

    AktualAlertDialogContent(
      modifier = Modifier.padding(Dimens.Large),
      title = Strings.settingsUiTestDialogTitle,
      buttons = { DialogTextButton(text = Strings.settingsUiTestDialogDismiss, onClick = {}) },
    ) {
      Text(Strings.settingsUiTestDialogMessage)
    }
  }
}

private val PreviewHeight = 260.dp

@Preview
@Composable
private fun PreviewDialogsPreview(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { DialogsPreview(HazeConfig()) }
