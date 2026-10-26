package aktual.about.ui.logs

import aktual.about.vm.logs.LogFilters
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import logcat.LogPriority

/**
 * Selected levels as letters, e.g. "Levels: D, I, W", for the button that opens [LogLevelSheet].
 */
@Composable
internal fun LogFilters.levelsSummary(): String =
  Strings.logsLevelsSummary(
    when {
      priorities.containsAll(LogPriority.entries) -> Strings.logsLevelsAll
      priorities.isEmpty() -> Strings.logsLevelsNone
      else -> LogPriority.entries.filter { it in priorities }.joinToString { it.letter.toString() }
    },
  )

@Composable
internal fun LogLevelSheet(
  filters: LogFilters,
  counts: ImmutableMap<LogPriority, Int>,
  onAction: LogsActionHandler,
  onDismiss: () -> Unit,
) {
  // Toggles go straight to the VM, so the list behind the sheet filters live
  AktualModalBottomSheet(onDismissRequest = onDismiss, blurBackground = false) {
    LogLevelSheetContent(filters, counts, onAction)
  }
}

@Composable
private fun LogLevelSheetContent(
  filters: LogFilters,
  counts: ImmutableMap<LogPriority, Int>,
  onAction: LogsActionHandler,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = Dimens.Huge).padding(bottom = 24.dp),
    verticalArrangement = spacedBy(Dimens.Large),
  ) {
    Text(text = Strings.logsLevels, style = typography.headlineSmall)
    Column {
      for (priority in LogPriority.entries) {
        LogLevelRow(
          priority = priority,
          isChecked = priority in filters.priorities,
          count = counts[priority] ?: 0,
          onAction = onAction,
        )
      }
    }
  }

@Composable
internal fun LogLevelRow(
  priority: LogPriority,
  isChecked: Boolean,
  count: Int,
  onAction: LogsActionHandler,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 32.dp)
        .toggleable(
          value = isChecked,
          role = Role.Checkbox,
          onValueChange = { onAction(TogglePriority(priority)) },
        )
        .padding(end = 8.dp),
    verticalAlignment = CenterVertically,
    horizontalArrangement = spacedBy(8.dp),
  ) {
    Checkbox(
      checked = isChecked,
      onCheckedChange = null,
      colors = CheckboxDefaults.colors(checkedColor = colors.checkboxBackgroundSelected),
    )
    Box(modifier = Modifier.size(8.dp).background(priority.foreground(colors), CircleShape))
    Text(
      modifier = Modifier.weight(1f),
      text = priority.label(),
      style = typography.bodyMedium,
      color = colors.pageText,
    )
    Text(text = count.toString(), style = typography.labelSmall, color = colors.pageTextLight)
  }

@Preview
@Composable
private fun PreviewLogLevelSheetContent(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    LogLevelSheetContent(
      filters =
        LogFilters(query = "", priorities = persistentSetOf(INFO, WARN), tags = persistentSetOf()),
      counts =
        persistentMapOf(LogPriority.INFO to 12, LogPriority.WARN to 3, LogPriority.ERROR to 1),
      onAction = {},
    )
  }
