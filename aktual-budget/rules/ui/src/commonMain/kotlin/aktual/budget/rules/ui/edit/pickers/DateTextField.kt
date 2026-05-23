package aktual.budget.rules.ui.edit.pickers

import aktual.budget.model.RecurConfig
import aktual.budget.model.RecurEndMode
import aktual.budget.model.RecurFrequency
import aktual.core.icons.material.CalendarToday
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.DarkTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTextField
import aktual.core.ui.BareIconButton
import aktual.core.ui.LocalDateFormatter
import aktual.core.ui.PreviewWithTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

@Composable
internal fun DateTextField(
  value: JsonElement,
  isEnabled: Boolean,
  onValueChange: (JsonElement) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  require(value is JsonNull || value is JsonObject) { "Need object or null, got $value" }

  var showDialog by remember { mutableStateOf(false) }

  val recurConfig =
    remember(value) {
      if (value is JsonNull) {
        null
      } else {
        Json.decodeFromJsonElement(RecurConfig.serializer(), value)
      }
    }

  val localDate = recurConfig?.start
  val formatter = LocalDateFormatter.current
  val displayText = remember(localDate) { localDate?.let(formatter::format).orEmpty() }

  val textState = rememberTextFieldState(initialText = displayText)
  LaunchedEffect(displayText) {
    if (textState.text.toString() != displayText) textState.edit { replace(0, length, displayText) }
  }

  Box(modifier = modifier) {
    AktualTextField(
      modifier = Modifier.fillMaxWidth().border(Dp.Hairline, theme.buttonNormalBorder),
      state = textState,
      placeholderText = Strings.editRuleConditionNothing,
      readOnly = true,
      isEnabled = isEnabled,
      singleLine = true,
      leadingIcon = { Icon(imageVector = MaterialIcons.CalendarToday, contentDescription = null) },
      trailingIcon =
        if (value != JsonNull) {
          {
            BareIconButton(
              enabled = isEnabled,
              imageVector = MaterialIcons.Clear,
              contentDescription = Strings.editRuleDateClear,
              onClick = { onValueChange(JsonNull) },
            )
          }
        } else {
          null
        },
      colors = theme.pickerField(),
    )
    Box(modifier = Modifier.matchParentSize().clickable(isEnabled) { showDialog = true })
  }

  if (showDialog) {
    val state = rememberDatePickerState(localDate?.toJavaLocalDate())

    DatePickerDialog(
      onDismissRequest = { showDialog = false },
      confirmButton = {
        TextButton(
          enabled = state.selectedDateMillis != null,
          onClick = {
            val date = state.getSelectedDate()?.toKotlinLocalDate()
            if (date != null) {
              onValueChange(recurConfig.serializeWith(date))
            } else {
              onValueChange(JsonNull)
            }
            showDialog = false
          },
          content = { Text(Strings.editRuleDatePickerOk) },
        )
      },
      dismissButton = {
        TextButton(
          onClick = { showDialog = false },
          content = { Text(Strings.editRuleDatePickerCancel) },
        )
      },
      content = { DatePicker(state = state) },
    )
  }
}

private fun RecurConfig?.serializeWith(date: LocalDate?): JsonElement {
  date ?: return JsonNull
  val config = this ?: RecurConfig(frequency = RecurFrequency.Monthly, start = date)
  return Json.encodeToJsonElement(RecurConfig.serializer(), config.copy(start = date))
}

@Preview
@Composable
private fun PreviewDateTextField(
  @PreviewParameter(DateTextFieldProvider::class) params: DateTextFieldParams
) {
  PreviewWithTheme(DarkTheme) {
    with(params) {
      var element by remember { mutableStateOf(value) }
      DateTextField(value = element, isEnabled = isEnabled, onValueChange = { element = it })
    }
  }
}

private data class DateTextFieldParams(val value: JsonElement, val isEnabled: Boolean = true)

private val PreviewRecurConfig =
  Json.encodeToJsonElement(
    serializer = RecurConfig.serializer(),
    value =
      RecurConfig(
        frequency = RecurFrequency.Monthly,
        start = LocalDate.parse("2025-03-02"),
        interval = 1,
        patterns = emptyList(),
        skipWeekend = false,
        endMode = RecurEndMode.Never,
        endOccurrences = 1,
        endDate = LocalDate.parse("2025-03-06"),
      ),
  )

private class DateTextFieldProvider :
  CollectionPreviewParameterProvider<DateTextFieldParams>(
    listOf(
      DateTextFieldParams(value = JsonNull),
      DateTextFieldParams(value = PreviewRecurConfig),
      DateTextFieldParams(value = PreviewRecurConfig, isEnabled = false),
    )
  )
