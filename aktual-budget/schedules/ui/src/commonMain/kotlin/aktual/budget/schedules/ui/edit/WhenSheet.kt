package aktual.budget.schedules.ui.edit

import aktual.budget.schedules.components.RecurConfigFields
import aktual.budget.schedules.components.ScheduleDateField
import aktual.budget.schedules.components.ScheduleSwitchRow
import aktual.budget.schedules.vm.edit.ScheduleDate
import aktual.budget.schedules.vm.edit.defaultRecurConfig
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun WhenSheet(
  date: ScheduleDate,
  onDismiss: () -> Unit,
  onConfirm: (ScheduleDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  var working by remember { mutableStateOf(date) }

  EditorSheet(
    modifier = modifier,
    title = Strings.editScheduleWhenTitle,
    canConfirm = true,
    onDismiss = onDismiss,
    onConfirm = { onConfirm(working) },
  ) {
    ScheduleSwitchRow(
      text = Strings.editScheduleWhenRepeats,
      checked = working is Recurring,
      onCheckedChange = { repeats ->
        working =
          if (repeats) {
            ScheduleDate.Recurring(defaultRecurConfig(start = working.start))
          } else {
            ScheduleDate.Once(working.start)
          }
      },
    )

    when (val current = working) {
      is Once ->
        ScheduleDateField(
          label = Strings.editScheduleWhenDate,
          date = current.date,
          onDateChange = { working = ScheduleDate.Once(it) },
        )

      is Recurring ->
        RecurConfigFields(
          config = current.config,
          onConfigChange = { working = ScheduleDate.Recurring(it) },
        )
    }
  }
}
