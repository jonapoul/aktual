package aktual.budget.schedules.ui.edit

import aktual.budget.model.RecurConfig
import aktual.budget.model.RecurEndMode
import aktual.budget.model.RecurFrequency
import aktual.budget.model.WeekendSolveMode
import aktual.budget.schedules.vm.edit.ScheduleDate
import aktual.budget.schedules.vm.edit.defaultRecurConfig
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Subtract
import aktual.core.icons.material.CalendarToday
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.formatted
import aktual.core.ui.frequencyDescription
import aktual.core.ui.switch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate

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
    SwitchRow(
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
        DateField(
          label = Strings.editScheduleWhenDate,
          date = current.date,
          onDateChange = { working = ScheduleDate.Once(it) },
        )

      is Recurring ->
        RecurringFields(
          config = current.config,
          onConfigChange = { working = ScheduleDate.Recurring(it) },
        )
    }
  }
}

@Composable
private fun RecurringFields(
  config: RecurConfig,
  onConfigChange: (RecurConfig) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(EditScheduleDS.sheetSpacing),
  ) {
    DateField(
      label = Strings.editScheduleWhenStarts,
      date = config.start,
      onDateChange = { onConfigChange(config.copy(start = it)) },
    )

    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      selected = config.frequency,
      options = FREQUENCIES,
      onSelect = { onConfigChange(config.copy(frequency = it)) },
      string = { it.string() },
    )

    val interval = (config.interval ?: 1).coerceAtLeast(1)
    Stepper(
      text = config.frequency.intervalText(interval),
      canDecrease = interval > 1,
      onDecrease = { onConfigChange(config.copy(interval = interval - 1)) },
      onIncrease = { onConfigChange(config.copy(interval = interval + 1)) },
    )

    // Specific days of the month can't be edited here yet, but they're kept as they are
    if (config.frequency == Monthly && !config.patterns.isNullOrEmpty()) {
      Text(
        text = Strings.editSchedulePatternsNote(config.frequencyDescription()),
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }

    val skipWeekend = config.skipWeekend == true
    SwitchRow(
      text = Strings.editScheduleWeekendMove,
      checked = skipWeekend,
      onCheckedChange = { onConfigChange(config.copy(skipWeekend = it)) },
    )

    if (skipWeekend) {
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = if (config.weekendSolveMode == Before) Before else After,
        options = WEEKEND_MODES,
        onSelect = { onConfigChange(config.copy(weekendSolveMode = it)) },
        string = { mode ->
          if (mode == Before) {
            Strings.editScheduleWeekendBefore
          } else {
            Strings.editScheduleWeekendAfter
          }
        },
      )
    }

    EndFields(config, onConfigChange)
  }

@Composable
private fun EndFields(
  config: RecurConfig,
  onConfigChange: (RecurConfig) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(EditScheduleDS.sheetSpacing),
  ) {
    Text(text = Strings.editScheduleEnds, style = typography.labelLarge)

    val endMode: RecurEndMode =
      when (config.endMode) {
        AfterNOccurrences -> AfterNOccurrences
        OnDate -> OnDate
        Never,
        Unknown,
        null -> Never
      }

    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      selected = endMode,
      options = END_MODES,
      // Saves the count and date shown below, so a limited schedule never ends up unlimited
      onSelect = { mode ->
        onConfigChange(
          config.copy(
            endMode = mode,
            endOccurrences = (config.endOccurrences ?: 1).coerceAtLeast(1),
            endDate = config.endDate ?: config.start,
          )
        )
      },
      string = { it.string() },
    )

    when (endMode) {
      AfterNOccurrences -> {
        val count = (config.endOccurrences ?: 1).coerceAtLeast(1)
        Stepper(
          text = Plurals.editScheduleSentenceTimes(count, count),
          canDecrease = count > 1,
          onDecrease = { onConfigChange(config.copy(endOccurrences = count - 1)) },
          onIncrease = { onConfigChange(config.copy(endOccurrences = count + 1)) },
        )
      }

      OnDate -> {
        DateField(
          label = Strings.editScheduleEndsDate,
          date = config.endDate ?: config.start,
          onDateChange = { onConfigChange(config.copy(endDate = it)) },
        )
      }

      Never,
      Unknown -> {}
    }
  }

@Composable
private fun SwitchRow(
  text: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.fieldSpacing),
  ) {
    Text(modifier = Modifier.weight(1f), text = text, style = typography.bodyLarge)
    Switch(checked = checked, onCheckedChange = onCheckedChange, colors = colors.switch())
  }
}

@Composable
private fun Stepper(
  text: String,
  canDecrease: Boolean,
  onDecrease: () -> Unit,
  onIncrease: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.labelSpacing),
  ) {
    Text(modifier = Modifier.weight(1f), text = text, style = typography.bodyLarge)
    BareIconButton(
      imageVector = AktualIcons.Subtract,
      contentDescription = Strings.editScheduleIntervalDecrease,
      enabled = canDecrease,
      onClick = onDecrease,
    )
    BareIconButton(
      imageVector = AktualIcons.Add,
      contentDescription = Strings.editScheduleIntervalIncrease,
      onClick = onIncrease,
    )
  }
}

@Composable
private fun DateField(
  label: String,
  date: LocalDate,
  onDateChange: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(EditScheduleDS.labelSpacing),
  ) {
    Text(text = label, style = typography.labelLarge)
    NormalTextButton(
      modifier = Modifier.fillMaxWidth(),
      text = date.formatted(),
      prefix = { Icon(imageVector = MaterialIcons.CalendarToday, contentDescription = null) },
      onClick = { showDialog = true },
    )
  }

  if (showDialog) {
    val state = rememberDatePickerState(date.toJavaLocalDate())
    DatePickerDialog(
      onDismissRequest = { showDialog = false },
      confirmButton = {
        TextButton(
          enabled = state.selectedDateMillis != null,
          onClick = {
            state.getSelectedDate()?.toKotlinLocalDate()?.let(onDateChange)
            showDialog = false
          },
          content = { Text(Strings.editScheduleDatePickerOk) },
        )
      },
      dismissButton = {
        TextButton(
          onClick = { showDialog = false },
          content = { Text(Strings.editScheduleDatePickerCancel) },
        )
      },
      content = { DatePicker(state = state) },
    )
  }
}

@Composable
private fun RecurFrequency.string(): String =
  when (this) {
    Daily -> Strings.editScheduleFrequencyDaily
    Weekly -> Strings.editScheduleFrequencyWeekly
    Yearly -> Strings.editScheduleFrequencyYearly
    Monthly,
    Unknown -> Strings.editScheduleFrequencyMonthly
  }

@Composable
private fun RecurFrequency.intervalText(interval: Int): String =
  when (this) {
    Daily -> Plurals.editScheduleIntervalDays(interval, interval)
    Weekly -> Plurals.editScheduleIntervalWeeks(interval, interval)
    Yearly -> Plurals.editScheduleIntervalYears(interval, interval)
    Monthly,
    Unknown -> Plurals.editScheduleIntervalMonths(interval, interval)
  }

@Composable
private fun RecurEndMode.string(): String =
  when (this) {
    AfterNOccurrences -> Strings.editScheduleEndsAfter
    OnDate -> Strings.editScheduleEndsOnDate
    Never,
    Unknown -> Strings.editScheduleEndsNever
  }

private val FREQUENCIES: PersistentList<RecurFrequency> =
  persistentListOf(Daily, Weekly, Monthly, Yearly)

private val WEEKEND_MODES: PersistentList<WeekendSolveMode> = persistentListOf(Before, After)

private val END_MODES: PersistentList<RecurEndMode> =
  persistentListOf(Never, AfterNOccurrences, OnDate)
