package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetState
import aktual.core.icons.material.ExpandMore
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.stringShort
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

// packages/desktop-client/src/components/mobile/budget/BudgetPage.tsx MonthSelector
@Composable
internal fun MonthSelector(
  state: BudgetState.Loaded,
  onAction: BudgetActionHandler,
  onPickMonth: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier, verticalAlignment = CenterVertically) {
    BareIconButton(
      modifier = Modifier.rotate(PREVIOUS_ROTATION),
      imageVector = MaterialIcons.ExpandMore,
      contentDescription = Strings.budgetingPreviousMonth,
      enabled = state.canGoBack,
      onClick = { onAction(PreviousMonth) },
    )

    Column(
      modifier =
        Modifier.weight(1f, fill = false)
          .clip(CardShape)
          .clickable(
            onClickLabel = Strings.budgetingPickMonth,
            role = Button,
            onClick = onPickMonth,
          )
          .padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(text = state.title(), maxLines = 1, overflow = Ellipsis)

      // Marks the current month
      Box(
        modifier =
          Modifier.width(24.dp)
            .height(2.dp)
            .background(
              color = if (state.showsOnlyCurrent) colors.pageTextLink else Color.Transparent,
              shape = CardShape,
            ),
      )
    }

    BareIconButton(
      modifier = Modifier.rotate(NEXT_ROTATION),
      imageVector = MaterialIcons.ExpandMore,
      contentDescription = Strings.budgetingNextMonth,
      enabled = state.canGoForward,
      onClick = { onAction(NextMonth) },
    )
  }
}

private val BudgetState.Loaded.showsOnlyCurrent: Boolean
  get() = monthCount == 1 && month == current

@Composable
private fun BudgetState.Loaded.title(): String {
  val last = lastMonth
  return when {
    monthCount == 1 -> month.stringShort()
    month.year == last.year ->
      Strings.budgetingMonthRange(month.month.stringShort(), last.stringShort())
    else -> Strings.budgetingMonthRange(month.stringShort(), last.stringShort())
  }
}

// packages/desktop-client/src/components/budget/MonthCountSelector.tsx
@Composable
internal fun MonthCountSelector(
  count: Int,
  max: Int,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  val label = Strings.budgetingMonthCount
  AktualSlidingToggleButton(
    modifier = modifier.width(MonthCountWidth * max).semantics { contentDescription = label },
    selected = count,
    options = remember(max) { (1..max).toImmutableList() },
    onSelect = { onAction(SetMonthCount(it)) },
    fontSize = 14.sp,
  )
}

@Composable
internal fun MonthPickerSheet(
  state: BudgetState.Loaded,
  onAction: BudgetActionHandler,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
    MonthPickerContent(
      state = state,
      onSelect = { month ->
        onAction(ShowMonth(month))
        onDismiss()
      },
    )
  }
}

@Composable
private fun MonthPickerContent(
  state: BudgetState.Loaded,
  onSelect: (YearMonth) -> Unit,
  modifier: Modifier = Modifier,
) {
  var year by remember { mutableIntStateOf(state.month.year) }

  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = CenterVertically) {
      BareIconButton(
        modifier = Modifier.rotate(PREVIOUS_ROTATION),
        imageVector = MaterialIcons.ExpandMore,
        contentDescription = Strings.budgetingPreviousYear,
        enabled = year > state.earliest.year,
        onClick = { year-- },
      )
      Text(
        modifier = Modifier.weight(1f),
        text = year.toString(),
        style = typography.headlineSmall,
        textAlign = Center,
      )
      BareIconButton(
        modifier = Modifier.rotate(NEXT_ROTATION),
        imageVector = MaterialIcons.ExpandMore,
        contentDescription = Strings.budgetingNextYear,
        enabled = year < state.latest.year,
        onClick = { year++ },
      )
    }

    MonthRows.fastForEach { row ->
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        row.fastForEach { month ->
          val yearMonth = YearMonth(year, month)
          MonthCell(
            modifier = Modifier.weight(1f),
            month = month,
            isSelected = yearMonth == state.month,
            isCurrent = yearMonth == state.current,
            isEnabled = yearMonth in state.earliest..state.latest,
            onClick = { onSelect(yearMonth) },
          )
        }
      }
    }
  }
}

@Composable
private fun MonthCell(
  month: Month,
  isSelected: Boolean,
  isCurrent: Boolean,
  isEnabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val text =
    when {
      !isEnabled -> colors.buttonNormalDisabledText
      isSelected -> colors.buttonPrimaryText
      else -> colors.pageText
    }

  Box(
    modifier =
      modifier
        .height(44.dp)
        .clip(CardShape)
        .background(if (isSelected) colors.buttonPrimaryBackground else colors.tableBackground)
        .border(1.dp, if (isCurrent) colors.pageTextLink else colors.tableBorder, CardShape)
        .clickable(enabled = isEnabled, role = Button, onClick = onClick)
        .semantics { selected = isSelected },
    contentAlignment = Center,
  ) {
    Text(text = month.stringShort(), fontSize = 14.sp, color = text, maxLines = 1)
  }
}

private class MonthPickerProvider :
  ColoredParameterProvider<BudgetState.Loaded>(
    PREVIEW_ENVELOPE,
    PREVIEW_LATER,
  )

@Preview
@Composable
private fun PreviewMonthPicker(
  @PreviewParameter(MonthPickerProvider::class) params: ColoredParams<BudgetState.Loaded>,
) = PreviewWithColoredParams(params) { MonthPickerContent(state = this, onSelect = {}) }

private val MonthRows = Month.entries.chunked(size = 3)
private val MonthCountWidth = 40.dp
private const val PREVIOUS_ROTATION = 90f
private const val NEXT_ROTATION = -90f
