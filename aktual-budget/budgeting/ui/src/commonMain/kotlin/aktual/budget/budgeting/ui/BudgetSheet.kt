package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.CategoryRow
import aktual.budget.model.BudgetType
import aktual.budget.model.evaluateAmountInput
import aktual.budget.model.toSignedInputText
import aktual.core.l10n.Strings
import aktual.core.ui.AmountField
import aktual.core.ui.EditorSheet
import aktual.core.ui.NormalTextButton
import aktual.core.ui.formattedString
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.stringLong
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import kotlinx.datetime.YearMonth

// packages/desktop-client/src/components/modals/EnvelopeBudgetMenuModal.tsx and
// TrackingBudgetMenuModal.tsx
@Composable
internal fun BudgetSheet(
  month: YearMonth,
  category: CategoryRow,
  type: BudgetType,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val initial = category.budgeted.toSignedInputText()
  val input =
    rememberTextFieldState(initialText = initial, initialSelection = TextRange(0, initial.length))
  val text = input.text.toString()
  val isValid = evaluateAmountInput(text) != null

  EditorSheet(
    modifier = modifier,
    title = Strings.budgetingEditTitle(category.name, month.stringLong()),
    subtitle =
      Strings.budgetingEditSubtitle(
        category.spent.formattedString(),
        category.balance.formattedString(includeSign = true),
      ),
    cancelText = Strings.budgetingEditCancel,
    confirmText = Strings.budgetingEditSave,
    canConfirm = isValid,
    onDismiss = onDismiss,
    onConfirm = { if (text != initial) onAction(SetBudget(month, category.id, text)) },
  ) {
    AmountField(
      modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
      state = input,
      arithmetic = true,
    )

    val sheet = this
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      for (action in QuickAction.available(type)) {
        NormalTextButton(
          text = action.string(),
          onClick = { sheet.close { onAction(ApplyQuickAction(month, category.id, action)) } },
        )
      }
    }
  }
}

// The per-category entries of upstream's budget menus
internal enum class QuickAction {
  CopyLastMonth,
  Average3,
  Average6,
  Average12,
  CopyToYearEnd;

  companion object {
    // Upstream only offers copying to the year end for tracking budgets
    fun available(type: BudgetType): List<QuickAction> =
      if (type == Tracking) entries else entries - CopyToYearEnd
  }
}

@Composable
internal fun QuickAction.string(): String =
  when (this) {
    CopyLastMonth -> Strings.budgetingCopyLastMonth
    Average3 -> Strings.budgetingAverage3
    Average6 -> Strings.budgetingAverage6
    Average12 -> Strings.budgetingAverage12
    CopyToYearEnd -> Strings.budgetingCopyYearEnd
  }
