package aktual.about.ui.logs

import aktual.about.vm.logs.LogFilters
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentSetOf
import logcat.LogPriority

@Composable
internal fun LogFilterChips(
  filters: LogFilters,
  onAction: LogsActionHandler,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    for (priority in LogPriority.entries) {
      LogChip(
        text = priority.label(),
        isSelected = priority in filters.priorities,
        onClick = { onAction(TogglePriority(priority)) },
      )
    }

    for (tag in filters.tags) {
      LogChip(
        text = "$tag ×",
        isSelected = true,
        onClick = { onAction(RemoveTag(tag)) },
        contentDescription = Strings.logsTagRemove(tag),
      )
    }
  }
}

@Preview
@Composable
private fun PreviewLogFilterChips(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LogFilterChips(
      filters =
        LogFilters(
          query = "something",
          priorities = persistentSetOf(INFO, WARN),
          tags = persistentSetOf("holiday", "shopping"),
        ),
      onAction = {},
    )
  }
