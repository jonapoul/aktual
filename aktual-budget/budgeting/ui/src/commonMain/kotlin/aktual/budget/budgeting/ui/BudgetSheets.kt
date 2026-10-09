package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetState
import aktual.budget.budgeting.vm.BudgetSummary
import aktual.budget.budgeting.vm.CategoryRow
import aktual.budget.budgeting.vm.MonthBudget
import aktual.budget.budgeting.vm.category
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryId
import aktual.budget.model.evaluateAmountInput
import aktual.budget.model.toInputText
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AmountField
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.EditorSheet
import aktual.core.ui.formattedString
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.stringLong
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth

/** Which sheet the budget screen is showing, for one month and maybe one category */
@Immutable
internal sealed interface SheetRequest {
  val month: YearMonth

  data class EditSheet(override val month: YearMonth, val category: CategoryId) : SheetRequest

  // The balance actions on compact widths. Expanded widths use a popup menu instead
  data class BalanceSheet(override val month: YearMonth, val category: CategoryId) : SheetRequest

  data class TransferSheet(override val month: YearMonth, val category: CategoryId) : SheetRequest

  data class CoverSheet(override val month: YearMonth, val category: CategoryId) : SheetRequest

  data class SummarySheet(override val month: YearMonth) : SheetRequest

  data class HoldSheet(override val month: YearMonth) : SheetRequest

  data class TransferAvailableSheet(override val month: YearMonth) : SheetRequest

  data class CoverOverbudgetedSheet(override val month: YearMonth) : SheetRequest

  // Picks which overspent category to cover, from the overspending banner
  data class OverspentSheet(override val month: YearMonth) : SheetRequest
}

@Composable
internal fun BudgetSheets(
  request: SheetRequest,
  state: BudgetState.Loaded,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
) {
  val budget = state[request.month] ?: return
  val month = request.month
  val envelope = budget.summary as? BudgetSummary.Envelope

  when (request) {
    is EditSheet ->
      budget.category(request.category)?.let { category ->
        BudgetSheet(month, category, state.type, onAction, onDismiss)
      }

    is BalanceSheet ->
      budget.category(request.category)?.let { category ->
        MenuSheet(
          title = Strings.budgetingEditTitle(category.name, month.stringLong()),
          items = balanceActions(month, category, state.type, onAction),
          onDismiss = onDismiss,
        )
      }

    is TransferSheet ->
      budget.category(request.category)?.let { category ->
        TransferBalanceSheet(month, budget, category, onAction, onDismiss)
      }

    is CoverSheet ->
      budget.category(request.category)?.let { category ->
        CoverBalanceSheet(month, budget, category, onAction, onDismiss)
      }

    is SummarySheet -> SummarySheet(month, budget.summary, onAction, onDismiss)

    is HoldSheet -> envelope?.let { HoldBudgetSheet(month, it, onAction, onDismiss) }

    is TransferAvailableSheet ->
      envelope?.let { TransferAvailableBudgetSheet(month, budget, it, onAction, onDismiss) }

    is CoverOverbudgetedSheet ->
      envelope?.let { CoverOverbudgetedBudgetSheet(month, budget, it, onAction, onDismiss) }

    is OverspentSheet -> OverspentMenuSheet(month, budget, onAction, onDismiss)
  }
}

@Composable
private fun TransferBalanceSheet(
  month: YearMonth,
  budget: MonthBudget,
  category: CategoryRow,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
) =
  MoneySheet(
    title = Strings.budgetingTransferTitle(category.name),
    initial = category.balance,
    optionsLabel = Strings.budgetingTransferTo,
    options = budget.pickOptions(withToBudget = true) { it.id != category.id },
    confirmText = Strings.budgetingTransferConfirm,
    onDismiss = onDismiss,
    onConfirm = { input, to -> onAction(TransferBudget(month, input, category.id, to?.id)) },
  )

@Composable
private fun CoverBalanceSheet(
  month: YearMonth,
  budget: MonthBudget,
  category: CategoryRow,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
) =
  MoneySheet(
    title = Strings.budgetingCoverTitle(category.name),
    initial = -category.balance,
    optionsLabel = Strings.budgetingCoverFrom,
    options = budget.pickOptions(withToBudget = true) { it.id != category.id && it.balance > Zero },
    confirmText = Strings.budgetingCoverConfirm,
    onDismiss = onDismiss,
    onConfirm = { input, from -> onAction(CoverOverspending(month, category.id, from?.id, input)) },
  )

@Composable
private fun HoldBudgetSheet(
  month: YearMonth,
  summary: BudgetSummary.Envelope,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
) =
  MoneySheet(
    title = Strings.budgetingHold,
    initial = summary.toBudget,
    optionsLabel = null,
    options = null,
    confirmText = Strings.budgetingHoldConfirm,
    onDismiss = onDismiss,
    onConfirm = { input, _ -> onAction(HoldBudget(month, input)) },
  )

@Composable
private fun TransferAvailableBudgetSheet(
  month: YearMonth,
  budget: MonthBudget,
  summary: BudgetSummary.Envelope,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
) =
  MoneySheet(
    title = Strings.budgetingTransferAvailable,
    initial = summary.toBudget,
    optionsLabel = Strings.budgetingTransferTo,
    options = budget.pickOptions(withToBudget = false) { true },
    confirmText = Strings.budgetingTransferConfirm,
    onDismiss = onDismiss,
    onConfirm = { input, to ->
      to?.id?.let { category -> onAction(TransferAvailable(month, input, category)) }
    },
  )

@Composable
private fun CoverOverbudgetedBudgetSheet(
  month: YearMonth,
  budget: MonthBudget,
  summary: BudgetSummary.Envelope,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
) =
  MoneySheet(
    title = Strings.budgetingCoverOverbudgetedTitle,
    initial = -summary.toBudget,
    optionsLabel = Strings.budgetingCoverFrom,
    options = budget.pickOptions(withToBudget = false) { it.balance > Zero },
    confirmText = Strings.budgetingCoverConfirm,
    onDismiss = onDismiss,
    onConfirm = { input, from ->
      from?.id?.let { category -> onAction(CoverOverbudgeted(month, category, input)) }
    },
  )

@Composable
private fun OverspentMenuSheet(
  month: YearMonth,
  budget: MonthBudget,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
) =
  MenuSheet(
    title = Strings.budgetingCoverPick,
    items =
      budget
        .expenseCategories()
        .filter { it.balance < Zero && !it.carryover }
        .map { category ->
          MenuItem(
            label = category.name,
            trailing = category.balance.formattedString(includeSign = true),
            onClick = { onAction(OpenSheet(SheetRequest.CoverSheet(month, category.id))) },
          )
        }
        .toImmutableList(),
    onDismiss = onDismiss,
  )

@Immutable
internal data class MenuItem(
  val label: String,
  val onClick: () -> Unit,
  val trailing: String? = null,
)

// packages/desktop-client/src/components/budget/envelope/BalanceMenu.tsx and
// tracking/BalanceMenu.tsx. Tracking budgets can only roll overspending over
@Composable
internal fun balanceActions(
  month: YearMonth,
  category: CategoryRow,
  type: BudgetType,
  onAction: BudgetActionHandler,
): ImmutableList<MenuItem> = buildList {
  if (type == Envelope && category.balance > Zero) {
    add(
      MenuItem(
        label = Strings.budgetingBalanceTransfer,
        onClick = { onAction(OpenSheet(SheetRequest.TransferSheet(month, category.id))) },
      ),
    )
  }
  if (type == Envelope && category.balance < Zero) {
    add(
      MenuItem(
        label = Strings.budgetingBalanceCover,
        onClick = { onAction(OpenSheet(SheetRequest.CoverSheet(month, category.id))) },
      ),
    )
  }
  add(
    MenuItem(
      label =
        if (category.carryover) {
          Strings.budgetingBalanceRolloverOff
        } else {
          Strings.budgetingBalanceRolloverOn
        },
      onClick = { onAction(ToggleCarryover(month, category.id, !category.carryover)) },
    ),
  )
}
  .toImmutableList()

@Composable
private fun MenuSheet(
  title: String,
  items: ImmutableList<MenuItem>,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
    Text(
      modifier = Modifier.padding(horizontal = SheetsDS.padding).padding(bottom = 8.dp),
      text = title,
      style = typography.headlineSmall,
    )
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      for (item in items) {
        BottomSheetListItem(
          label = item.label,
          isSelected = false,
          onClick = {
            onDismiss()
            item.onClick()
          },
          trailingContent = item.trailing?.let { trailing -> { AmountLabel(trailing) } },
        )
      }
    }
  }
}

// A category to move money to or from, or To Budget when it has no id
@Immutable
internal data class PickOption(val id: CategoryId?, val name: String, val balance: Amount)

@Composable
private fun MonthBudget.pickOptions(
  withToBudget: Boolean,
  filter: (CategoryRow) -> Boolean,
): ImmutableList<PickOption> = buildList {
  val toBudget = (summary as? BudgetSummary.Envelope)?.toBudget
  if (withToBudget && toBudget != null) {
    add(PickOption(id = null, name = Strings.budgetingToBudget, balance = toBudget))
  }
  expenseCategories().filter(filter).forEach { add(PickOption(it.id, it.name, it.balance)) }
}
  .toImmutableList()

private fun MonthBudget.expenseCategories(): List<CategoryRow> = groups.flatMap { it.categories }

/**
 * An amount, defaulting to [initial] when that's positive, with an optional list to pick where the
 * money comes from or goes to. Confirming needs a positive amount, and a pick when there's a list.
 */
@Composable
private fun MoneySheet(
  title: String,
  initial: Amount,
  optionsLabel: String?,
  options: ImmutableList<PickOption>?,
  confirmText: String,
  onDismiss: () -> Unit,
  onConfirm: (input: String, option: PickOption?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val initialText = if (initial > Zero) initial.toInputText() else ""
  val input =
    rememberTextFieldState(
      initialText = initialText,
      initialSelection = TextRange(0, initialText.length),
    )
  var picked by remember { mutableStateOf(options?.firstOrNull()) }
  val amount = evaluateAmountInput(input.text.toString())
  val canConfirm = amount != null && amount > Zero && (options == null || picked != null)

  EditorSheet(
    modifier = modifier,
    title = title,
    cancelText = Strings.budgetingEditCancel,
    confirmText = confirmText,
    canConfirm = canConfirm,
    onDismiss = onDismiss,
    onConfirm = { onConfirm(input.text.toString(), picked) },
  ) {
    AmountField(
      modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
      state = input,
      arithmetic = true,
    )

    if (options != null) {
      if (optionsLabel != null) Text(text = optionsLabel, style = typography.labelLarge)
      Column {
        for (option in options) {
          BottomSheetListItem(
            label = option.name,
            isSelected = option == picked,
            onClick = { picked = option },
            trailingContent = {
              AmountLabel(option.balance.formattedString(includeSign = true))
            },
          )
        }
      }
    }
  }
}

// packages/desktop-client/src/components/budget/envelope/budgetsummary/ToBudgetMenu.tsx and
// TotalsList.tsx, and tracking/budgetsummary for tracking budgets, which have no actions
@Composable
private fun SummarySheet(
  month: YearMonth,
  summary: BudgetSummary,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
    Column(
      modifier = Modifier.padding(horizontal = SheetsDS.padding).padding(bottom = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(text = month.stringLong(), style = typography.headlineSmall)
      for ((label, amount) in summary.rows()) SummaryRow(label, amount)
      HorizontalDivider(color = colors.tableBorder)
      val headline = summary.headline()
      SummaryRow(headline.label, headline.amount, bold = true)
    }

    if (summary is Envelope) {
      val actions = buildList {
        if (summary.toBudget > Zero) {
          add(Strings.budgetingHold to SheetRequest.HoldSheet(month))
          add(Strings.budgetingTransferAvailable to SheetRequest.TransferAvailableSheet(month))
        }
        if (summary.toBudget < Zero) {
          add(Strings.budgetingCoverOverbudgeted to SheetRequest.CoverOverbudgetedSheet(month))
        }
      }
      for ((label, sheet) in actions) {
        BottomSheetListItem(
          label = label,
          isSelected = false,
          onClick = {
            onDismiss()
            onAction(OpenSheet(sheet))
          },
        )
      }
      if (summary.held != Zero) {
        BottomSheetListItem(
          label = Strings.budgetingHoldReset,
          isSelected = false,
          onClick = {
            onDismiss()
            onAction(ResetHold(month))
          },
        )
      }
    }
  }
}

@Composable
private fun BudgetSummary.rows(): List<Pair<String, Amount>> =
  when (this) {
    is Envelope ->
      listOf(
        Strings.budgetingSummaryAvailable to available,
        Strings.budgetingSummaryOverspent to overspentLastMonth,
        Strings.budgetingBudgeted to -budgeted,
        Strings.budgetingSummaryHeld to -held,
      )

    is Tracking ->
      listOf(
        "${Strings.budgetingSummaryIncome} · ${Strings.budgetingBudgeted}" to incomeBudgeted,
        "${Strings.budgetingSummaryIncome} · ${Strings.budgetingSummaryReceived}" to received,
        "${Strings.budgetingSummaryExpenses} · ${Strings.budgetingBudgeted}" to budgeted,
        "${Strings.budgetingSummaryExpenses} · ${Strings.budgetingSpent}" to spent,
      )
  }

@Composable
private fun SummaryRow(label: String, amount: Amount, bold: Boolean = false) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(text = label, fontSize = 14.sp, color = colors.pageTextSubdued)
    Text(
      text = amount.formattedString(includeSign = true),
      fontSize = 14.sp,
      fontWeight = if (bold) SemiBold else Normal,
      style = TextStyle.Default.tabularFigures(),
      color = colors.pageText,
    )
  }
}

@Composable
private fun AmountLabel(text: String) =
  Text(
    text = text,
    fontSize = 13.sp,
    style = TextStyle.Default.tabularFigures(),
    color = colors.pageTextSubdued,
  )

private object SheetsDS {
  val padding = 20.dp
}
