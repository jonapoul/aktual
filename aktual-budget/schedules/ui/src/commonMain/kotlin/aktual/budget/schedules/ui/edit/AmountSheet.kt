package aktual.budget.schedules.ui.edit

import aktual.budget.model.Amount
import aktual.budget.model.parseAmountInput
import aktual.budget.model.toInputText
import aktual.budget.schedules.vm.edit.ScheduleAmount
import aktual.budget.schedules.vm.edit.isDeposit
import aktual.budget.schedules.vm.edit.withDeposit
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private enum class AmountOp {
  Exactly,
  Approximately,
  Between,
}

@Composable
internal fun AmountSheet(
  amount: ScheduleAmount,
  onDismiss: () -> Unit,
  onConfirm: (ScheduleAmount) -> Unit,
  modifier: Modifier = Modifier,
) {
  var isDeposit by remember { mutableStateOf(amount.isDeposit) }
  var op by remember { mutableStateOf(amount.op) }
  val firstText = amount.first.toInputText()
  val secondText = amount.second?.toInputText().orEmpty()
  val first = rememberTextFieldState(initialText = firstText)
  val second = rememberTextFieldState(initialText = secondText)

  // Typed amounts are unsigned, and pick up their sign from the payment/deposit toggle
  val firstAmount = parseAmountInput(first.text.toString())
  val secondAmount = parseAmountInput(second.text.toString())
  val result =
    when (op) {
      Exactly -> firstAmount?.let(ScheduleAmount::Exactly)
      Approximately -> firstAmount?.let(ScheduleAmount::Approximately)
      Between ->
        if (firstAmount != null && secondAmount != null) {
          ScheduleAmount.Between(firstAmount, secondAmount)
        } else {
          null
        }
    }?.withDeposit(isDeposit)

  // The sheet can't show a range that crosses zero, so an untouched amount is kept exactly as it
  // was
  val isUnchanged =
    op == amount.op &&
      isDeposit == amount.isDeposit &&
      first.text.toString() == firstText &&
      second.text.toString() == secondText

  EditorSheet(
    modifier = modifier,
    title = Strings.editScheduleAmountTitle,
    canConfirm = isUnchanged || result != null,
    onDismiss = onDismiss,
    onConfirm = { (if (isUnchanged) amount else result)?.let(onConfirm) },
  ) {
    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      selected = isDeposit,
      options = persistentListOf(false, true),
      onSelect = { isDeposit = it },
      string = { deposit ->
        if (deposit) Strings.editScheduleAmountDeposit else Strings.editScheduleAmountPayment
      },
    )

    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      selected = op,
      options = AmountOp.entries.toImmutableList(),
      onSelect = { op = it },
      string = { it.string() },
    )

    if (op == Between) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.fieldSpacing),
        verticalAlignment = CenterVertically,
      ) {
        AmountField(
          modifier = Modifier.weight(1f),
          state = first,
          label = Strings.editScheduleAmountFrom,
        )
        AmountField(
          modifier = Modifier.weight(1f),
          state = second,
          label = Strings.editScheduleAmountTo,
        )
      }
    } else {
      AmountField(modifier = Modifier.fillMaxWidth(), state = first, label = null)
    }

    Text(
      text =
        when (op) {
          Exactly -> Strings.editScheduleAmountHintExactly
          Approximately -> Strings.editScheduleAmountHintApprox
          Between -> Strings.editScheduleAmountHintBetween
        },
      style = typography.bodySmall,
      color = colors.pageTextSubdued,
    )
  }
}

@Composable
private fun AmountField(state: TextFieldState, label: String?, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(EditScheduleDS.labelSpacing),
  ) {
    if (label != null) {
      Text(text = label, style = typography.labelLarge)
    }

    AktualTextField(
      modifier = Modifier.fillMaxWidth(),
      state = state,
      placeholderText = ZERO_PLACEHOLDER,
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = Decimal),
      textStyle = typography.headlineSmall.copy(color = colors.pageText),
    )
  }
}

@Composable
private fun AmountOp.string(): String =
  when (this) {
    Exactly -> Strings.editScheduleAmountOpExactly
    Approximately -> Strings.editScheduleAmountOpApprox
    Between -> Strings.editScheduleAmountOpBetween
  }

private val ScheduleAmount.op: AmountOp
  get() =
    when (this) {
      is Exactly -> Exactly
      is Approximately -> Approximately
      is Between -> Between
    }

private val ScheduleAmount.first: Amount
  get() =
    when (this) {
      is Exactly -> amount
      is Approximately -> amount
      is Between -> minOf(from.abs(), to.abs())
    }

private val ScheduleAmount.second: Amount?
  get() = (this as? Between)?.let { maxOf(it.from.abs(), it.to.abs()) }

private fun Amount.abs(): Amount = if (this < Zero) -this else this

private const val ZERO_PLACEHOLDER = "0.00"
