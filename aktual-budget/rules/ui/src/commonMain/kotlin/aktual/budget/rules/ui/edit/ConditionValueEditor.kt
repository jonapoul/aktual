package aktual.budget.rules.ui.edit

import aktual.budget.model.Field
import aktual.budget.rules.ui.edit.pickers.AmountTextField
import aktual.budget.rules.ui.edit.pickers.DateTextField
import aktual.budget.rules.ui.edit.pickers.EntityIdPicker
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
internal fun ConditionValueEditor(
  field: Field,
  value: JsonElement,
  index: Int,
  isEnabled: Boolean,
  onAction: EditRuleActionHandler,
  modifier: Modifier = Modifier,
) {
  val emit: (JsonElement) -> Unit = { newValue -> onAction(SetConditionValue(newValue, index)) }

  when (field) {
    Field.Payee,
    Field.Description ->
      EntityIdPicker(
        modifier = modifier,
        field = field,
        value = value,
        isEnabled = isEnabled,
        onValueChange = emit,
        fetchEntities = { it.payees() },
      )

    Field.Account,
    Field.Acct ->
      EntityIdPicker(
        modifier = modifier,
        field = field,
        value = value,
        isEnabled = isEnabled,
        onValueChange = emit,
        fetchEntities = { it.accounts() },
      )

    Field.Category ->
      EntityIdPicker(
        modifier = modifier,
        field = field,
        value = value,
        isEnabled = isEnabled,
        onValueChange = emit,
        fetchEntities = { it.categories() },
      )

    Field.CategoryGroup ->
      EntityIdPicker(
        modifier = modifier,
        field = field,
        value = value,
        isEnabled = isEnabled,
        onValueChange = emit,
        fetchEntities = { it.categoryGroups() },
      )

    Field.Amount ->
      AmountTextField(
        modifier = modifier,
        value = value,
        isEnabled = isEnabled,
        onValueChange = emit,
      )

    Field.Date ->
      DateTextField(modifier = modifier, value = value, isEnabled = isEnabled, onValueChange = emit)

    Field.Notes,
    Field.PayeeName,
    Field.ImportedPayee,
    Field.ImportedDescription,
    Field.Saved,
    Field.Transfer,
    Field.Parent,
    Field.Cleared,
    Field.Reconciled ->
      // key(field) resets TextFieldState when switching between text-type fields (e.g. Notes →
      // PayeeName)
      key(field) {
        val textState = rememberTextFieldState(initialText = value.asEditableString())
        LaunchedEffect(textState) {
          snapshotFlow { textState.text.toString() }
            .collect { newText ->
              emit(if (newText.isEmpty()) JsonNull else JsonPrimitive(newText))
            }
        }
        AktualTextField(
          modifier = modifier.fillMaxWidth(),
          state = textState,
          placeholderText = Strings.editRuleConditionNothing,
          isEnabled = isEnabled,
          textStyle = typography.bodySmall,
        )
      }
  }
}

private fun JsonElement.asEditableString(): String =
  when (this) {
    is JsonPrimitive -> content
    JsonNull -> ""
    is JsonArray,
    is JsonObject -> toString()
  }
