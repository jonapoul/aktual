package aktual.budget.transactions.ui.edit

import aktual.budget.model.TransactionId
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Only one field is edited at a time, as upstream's SingleActiveEditForm has it
internal enum class EditField {
  Amount,
  Payee,
  Category,
  Account,
  Date,
  Notes,
  ScheduleLink,
}

// A field of the transaction, or of one of its split's parts
internal data class ActiveField(val field: EditField, val part: TransactionId? = null)

// What's being edited, and the text typed for it if that's an amount. They're kept together so a
// key can't land on the amount that was being typed before
@Stable
internal class EditFieldState {
  var active by mutableStateOf<ActiveField?>(null)
    private set

  val amountText = TextFieldState()

  val isTypingAmount: Boolean
    get() = active?.field == Amount

  fun edit(field: EditField, part: TransactionId? = null) {
    active = ActiveField(field, part)
  }

  fun typeAmount(text: String, part: TransactionId? = null) {
    amountText.setTextAndPlaceCursorAtEnd(text)
    active = ActiveField(Amount, part)
  }

  fun close() {
    active = null
  }
}
