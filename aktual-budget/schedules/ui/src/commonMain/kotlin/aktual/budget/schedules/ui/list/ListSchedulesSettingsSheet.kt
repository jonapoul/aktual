package aktual.budget.schedules.ui.list

import aktual.core.icons.AktualIcons
import aktual.core.icons.ViewHide
import aktual.core.icons.ViewShow
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.prefs.components.BooleanPreferenceItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun ListSchedulesSettingsSheet(
  showCompleted: Boolean,
  onAction: ListSchedulesActionHandler,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
    ListSchedulesSettingsContent(showCompleted = showCompleted, onAction = onAction)
  }
}

@Composable
private fun ListSchedulesSettingsContent(
  showCompleted: Boolean,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(text = Strings.listSchedulesSettingsTitle, style = typography.headlineSmall)

    BooleanPreferenceItem(
      value = showCompleted,
      onValueChange = { onAction(SetShowCompleted(it)) },
      title = Strings.listSchedulesShowCompleted,
      subtitle = null,
      icon = if (showCompleted) AktualIcons.ViewShow else AktualIcons.ViewHide,
      includeBackground = false,
    )
  }

@Preview
@Composable
private fun PreviewListSchedulesSettings(
  @PreviewParameter(ListSchedulesSettingsProvider::class) params: ColorParams<Boolean>,
) =
  PreviewWithColorParams(params) {
    ListSchedulesSettingsContent(showCompleted = this, onAction = {})
  }

private class ListSchedulesSettingsProvider : ColorParamsProvider<Boolean>(true, false)
