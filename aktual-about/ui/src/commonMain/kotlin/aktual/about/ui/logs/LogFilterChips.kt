package aktual.about.ui.logs

import aktual.about.vm.logs.LogFilters
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.icons.Delete
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.CardColumn
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParameters
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.Rectangle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun LogFilterChips(
  filters: LogFilters,
  onAction: OnAction<LogsAction>,
  onOpenLevels: () -> Unit,
  modifier: Modifier = Modifier,
) {
  CardColumn(
    modifier = modifier.fillMaxWidth(),
    shape = Rectangle,
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
  ) {
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(2.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Chip(
        text = filters.levelsSummary(),
        colors = ChipDefaults.selectableColors(isSelected = true),
        trailingIcon = AktualIcons.CheveronDown,
        onClick = onOpenLevels,
        role = Button,
      )

      for (tag in filters.tags) {
        Chip(
          text = tag,
          colors = ChipDefaults.selectableColors(isSelected = true),
          trailingIcon = AktualIcons.Delete,
          trailingIconDescription = Strings.logsTagRemove(tag),
          onClick = { onAction(RemoveTag(tag)) },
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewLogFilterChips(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LogFilterChips(
      filters =
        LogFilters(
          query = "something",
          priorities = persistentSetOf(INFO, WARN),
          tags =
            persistentSetOf(
              "holiday",
              "shopping",
              "something-else-really-long-like-this-to-test-ellipsizing-of-pills",
            ),
        ),
      onAction = {},
      onOpenLevels = {},
    )
  }
