package aktual.budget.rules.ui.edit

import aktual.budget.model.Field
import aktual.budget.rules.ui.edit.pickers.AmountTextField
import aktual.budget.rules.ui.edit.pickers.DateTextField
import aktual.budget.rules.ui.edit.pickers.EntityIdPicker
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTypography
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
      AktualTextField(
        modifier = modifier.fillMaxWidth(),
        value = value.asEditableString(),
        onValueChange = { newText ->
          emit(if (newText.isEmpty()) JsonNull else JsonPrimitive(newText))
        },
        placeholderText = Strings.editRuleConditionNothing,
        isEnabled = isEnabled,
        textStyle = AktualTypography.bodySmall,
      )
  }
}

private fun JsonElement.asEditableString(): String =
  when (this) {
    is JsonPrimitive -> content
    JsonNull -> ""
    is JsonArray,
    is JsonObject -> toString()
  }
