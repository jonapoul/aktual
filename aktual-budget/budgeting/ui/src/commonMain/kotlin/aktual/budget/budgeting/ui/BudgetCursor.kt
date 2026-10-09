package aktual.budget.budgeting.ui

import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryId
import aktual.budget.model.evaluateAmountInput
import aktual.budget.model.toSignedInputText
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.l10n.Strings
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.YearMonth

internal data class BudgetCell(val month: YearMonth, val category: CategoryId) {
  val tag: String
    get() = "budgeted-$month-${category.value}"
}

/**
 * The selected Budgeted cell of the expanded table, and whether it's being edited. Mirrors upstream
 * desktop's useSheetNavigation in packages/desktop-client/src/components/budget/BudgetTable.tsx:
 * Enter and Tab save and move down, with Shift up, Escape cancels, and the arrow keys move between
 * cells while not editing.
 */
@Stable
internal class BudgetCursor {
  var selected by mutableStateOf<BudgetCell?>(null)
    private set

  var isEditing by mutableStateOf(false)
    private set

  var menu by mutableStateOf<BudgetCell?>(null)
    private set

  fun edit(cell: BudgetCell) {
    selected = cell
    isEditing = true
  }

  fun select(cell: BudgetCell?) {
    selected = cell
    isEditing = false
  }

  // Clicking another cell moves the cursor before this one's editor loses focus, so that move wins
  fun finishEditing(cell: BudgetCell, next: BudgetCell) {
    if (selected == cell && isEditing) select(next)
  }

  fun openMenu(cell: BudgetCell) {
    select(cell)
    menu = cell
  }

  fun closeMenu() {
    menu = null
  }
}

// The editable cells in table order, so the cursor can move between them
@Stable
internal class BudgetGrid(
  private val months: ImmutableList<YearMonth>,
  private val rows: ImmutableList<CategoryId>,
) {
  fun move(from: BudgetCell, rows: Int = 0, months: Int = 0): BudgetCell? {
    val row = this.rows.indexOf(from.category) + rows
    val column = this.months.indexOf(from.month) + months
    val category = this.rows.getOrNull(row) ?: return null
    val month = this.months.getOrNull(column) ?: return null
    return BudgetCell(month, category)
  }

  fun first(): BudgetCell? {
    val month = months.firstOrNull() ?: return null
    val category = rows.firstOrNull() ?: return null
    return BudgetCell(month, category)
  }

  operator fun contains(cell: BudgetCell): Boolean = cell.month in months && cell.category in rows
}

// The arrow keys, Enter and Escape on the table while no cell is being edited
internal fun KeyEvent.moveCursor(cursor: BudgetCursor, grid: BudgetGrid): Boolean {
  if (type != KeyDown || cursor.isEditing || hasModifier()) return false
  val selected = cursor.selected?.takeIf { it in grid }
  val target =
    when {
      selected != null -> cursorTarget(selected, grid)
      key in ArrowKeys -> grid.first()?.let(CursorTarget::Select)
      else -> null
    }
  when (target) {
    null -> return false
    is Select -> cursor.select(target.cell)
    is Edit -> cursor.edit(target.cell)
    Stay -> Unit
  }
  return true
}

private fun KeyEvent.hasModifier() = isCtrlPressed || isAltPressed

private sealed interface CursorTarget {
  data class Select(val cell: BudgetCell?) : CursorTarget

  data class Edit(val cell: BudgetCell) : CursorTarget

  // Handled, but at the edge of the table
  data object Stay : CursorTarget
}

private fun KeyEvent.cursorTarget(selected: BudgetCell, grid: BudgetGrid): CursorTarget? {
  val moved =
    when (key) {
      DirectionUp -> grid.move(selected, rows = -1)
      DirectionDown -> grid.move(selected, rows = 1)
      DirectionLeft -> grid.move(selected, months = -1)
      DirectionRight -> grid.move(selected, months = 1)
      Enter,
      NumPadEnter -> return CursorTarget.Edit(selected)
      Escape -> return CursorTarget.Select(null)
      else -> return null
    }
  return if (moved == null) Stay else CursorTarget.Select(moved)
}

private val ArrowKeys =
  setOf(Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight)

// packages/desktop-client/src/components/budget/envelope/EnvelopeBudgetComponents.tsx BudgetedCell
@Composable
internal fun RowScope.BudgetedCell(
  cell: BudgetCell,
  amount: Amount,
  name: String,
  type: BudgetType,
  cursor: BudgetCursor,
  grid: BudgetGrid,
  onAction: BudgetActionHandler,
) {
  val isSelected = cursor.selected == cell
  val isEditing = isSelected && cursor.isEditing
  val editLabel = Strings.budgetingEditBudget(name)

  Box(
    modifier =
      Modifier.weight(1f)
        .testTag(cell.tag)
        .semantics { selected = isSelected }
        .height(BudgetDS.rowHeight - CellDS.verticalInset * 2)
        .then(
          if (isSelected) {
            Modifier.background(colors.tableRowBackgroundHover, CellShape)
              .border(1.dp, colors.formInputBorderSelected, CellShape)
          } else {
            Modifier
          },
        )
        .pointerInput(cell) {
          awaitPointerEventScope {
            while (true) {
              val event = awaitPointerEvent()
              if (event.type == Press && event.buttons.isSecondaryPressed) {
                event.changes.forEach { it.consume() }
                cursor.openMenu(cell)
              }
            }
          }
        }
        .clickable(onClickLabel = editLabel, role = Button) { cursor.edit(cell) },
    contentAlignment = CenterEnd,
  ) {
    if (isEditing) {
      InlineAmountEditor(
        amount = amount,
        onCommit = { input -> onAction(SetBudget(cell.month, cell.category, input)) },
        onFinish = { rows -> cursor.finishEditing(cell, grid.move(cell, rows) ?: cell) },
      )
    } else {
      AmountText(amount = amount, modifier = Modifier.padding(end = CellDS.textInset))
    }

    if (isSelected && !isEditing) {
      Icon(
        modifier =
          Modifier.align(CenterStart).padding(start = 2.dp).size(CellDS.menuIconSize).clickable(
            onClickLabel = Strings.budgetingEditOptions(name),
            role = Button,
          ) {
            cursor.openMenu(cell)
          },
        imageVector = MaterialIcons.MoreVert,
        contentDescription = null,
        tint = colors.pageTextSubdued,
      )
    }

    AktualDropdownMenu(expanded = cursor.menu == cell, onDismissRequest = cursor::closeMenu) {
      for (action in QuickAction.available(type)) {
        AktualDropdownMenuItem(
          text = action.string(),
          onClick = {
            cursor.closeMenu()
            onAction(ApplyQuickAction(cell.month, cell.category, action))
          },
        )
      }
    }
  }
}

/**
 * [onFinish] gets how many rows to move: one down for Enter or Tab, one up with Shift, or none when
 * cancelled with Escape or when focus leaves the field.
 */
@Composable
private fun InlineAmountEditor(
  amount: Amount,
  onCommit: (String) -> Unit,
  onFinish: (rows: Int) -> Unit,
) {
  val initial = remember(amount) { amount.toSignedInputText() }
  val state =
    rememberTextFieldState(initialText = initial, initialSelection = TextRange(0, initial.length))
  val focusRequester = remember { FocusRequester() }
  var isDone by remember { mutableStateOf(false) }
  var hadFocus by remember { mutableStateOf(false) }

  // Unchanged or unreadable text saves nothing, as upstream
  fun finish(save: Boolean, rows: Int) {
    if (isDone) return
    isDone = true
    val text = state.text.toString()
    if (save && text != initial && evaluateAmountInput(text) != null) onCommit(text)
    onFinish(rows)
  }

  LaunchedEffect(focusRequester) { focusRequester.requestFocus() }

  BasicTextField(
    modifier =
      Modifier.fillMaxWidth()
        .padding(horizontal = CellDS.textInset)
        .focusRequester(focusRequester)
        .onFocusChanged { focus ->
          if (focus.isFocused) hadFocus = true else if (hadFocus) finish(save = true, rows = 0)
        }
        .onPreviewKeyEvent { event ->
          if (event.type != KeyDown) return@onPreviewKeyEvent false
          val step = if (event.isShiftPressed) -1 else 1
          when (event.key) {
            Enter,
            NumPadEnter,
            Tab -> {
              finish(save = true, rows = step)
              true
            }
            Escape -> {
              finish(save = false, rows = 0)
              true
            }
            else -> {
              false
            }
          }
        },
    state = state,
    lineLimits = SingleLine,
    keyboardOptions = KeyboardOptions(keyboardType = Decimal),
    cursorBrush = SolidColor(colors.pageText),
    textStyle =
      TextStyle(
        fontSize = 14.sp,
        color = colors.tableText,
        textAlign = End,
        fontFeatureSettings = "tnum",
      ),
  )
}

private val CellShape = RoundedCornerShape(4.dp)

private object CellDS {
  val verticalInset = 6.dp
  val textInset = 6.dp
  val menuIconSize = 16.dp
}
