package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetState
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.ShimmerLine
import aktual.core.ui.stringShort
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.util.fastForEach
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

/**
 * packages/desktop-client/src/components/mobile/budget/BudgetPage.tsx MonthSelector
 *
 * Keep in sync with [ShimmerMonthSelector]
 */
@Composable
internal fun MonthSelector(
  state: BudgetState.Loaded,
  onAction: OnAction<BudgetAction>,
  onPickMonth: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier, verticalAlignment = CenterVertically) {
    AktualIconButton(
      modifier = Modifier.rotate(PREVIOUS_ROTATION),
      imageVector = AktualIcons.CheveronDown,
      contentDescription = Strings.budgetingPreviousMonth,
      isEnabled = state.canGoBack,
      onClick = { onAction(PreviousMonth) },
    )

    Column(
      modifier =
        Modifier.weight(1f)
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

    AktualIconButton(
      modifier = Modifier.rotate(NEXT_ROTATION),
      imageVector = AktualIcons.CheveronDown,
      contentDescription = Strings.budgetingNextMonth,
      isEnabled = state.canGoForward,
      onClick = { onAction(NextMonth) },
    )
  }
}

/** Keep in sync with [MonthSelector] */
@Composable
internal fun ShimmerMonthSelector(modifier: Modifier = Modifier) {
  Row(modifier = modifier, verticalAlignment = CenterVertically) {
    AktualIconButton(
      modifier = Modifier.rotate(PREVIOUS_ROTATION),
      imageVector = AktualIcons.CheveronDown,
      contentDescription = Strings.budgetingPreviousMonth,
      isEnabled = false,
      onClick = {},
    )

    Column(
      modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalAlignment = CenterHorizontally,
    ) {
      ShimmerLine(
        style = LocalTextStyle.current,
        modifier = Modifier.width(88.dp).shimmer(rememberShimmer(Window)),
      )
      Spacer(modifier = Modifier.height(4.dp))
    }

    AktualIconButton(
      modifier = Modifier.rotate(NEXT_ROTATION),
      imageVector = AktualIcons.CheveronDown,
      contentDescription = Strings.budgetingNextMonth,
      isEnabled = false,
      onClick = {},
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
  onAction: OnAction<BudgetAction>,
  modifier: Modifier = Modifier,
) {
  val label = Strings.budgetingMonthCount
  AktualSlidingToggleButton(
    modifier = modifier.width(MonthCountWidth * max).semantics { contentDescription = label },
    selected = count,
    options = remember(max) { (1..max).toImmutableList() },
    onSelect = { onAction(SetMonthCount(it)) },
    fontSize = textSizes.medium,
  )
}

@Composable
internal fun MonthPickerSheet(
  state: BudgetState.Loaded,
  onAction: OnAction<BudgetAction>,
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
      AktualIconButton(
        modifier = Modifier.rotate(PREVIOUS_ROTATION),
        imageVector = AktualIcons.CheveronDown,
        contentDescription = Strings.budgetingPreviousYear,
        isEnabled = year > state.earliest.year,
        onClick = { year-- },
      )
      Text(
        modifier = Modifier.weight(1f),
        text = year.toString(),
        style = typography.headlineSmall,
        textAlign = Center,
      )
      AktualIconButton(
        modifier = Modifier.rotate(NEXT_ROTATION),
        imageVector = AktualIcons.CheveronDown,
        contentDescription = Strings.budgetingNextYear,
        isEnabled = year < state.latest.year,
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
    Text(
      text = month.stringShort(),
      fontSize = textSizes.medium,
      color = text,
      maxLines = 1,
    )
  }
}

private class MonthPickerProvider :
  ColorParamsProvider<BudgetState.Loaded>(
    PREVIEW_ENVELOPE,
    PREVIEW_LATER,
  )

@Preview
@Composable
private fun PreviewMonthPicker(
  @PreviewParameter(MonthPickerProvider::class) params: ColorParams<BudgetState.Loaded>,
) = PreviewWithColorParams(params) { MonthPickerContent(state = this, onSelect = {}) }

@Preview(widthDp = 320)
@Composable
private fun PreviewShimmerMonthSelector(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerMonthSelector(modifier = Modifier.fillMaxWidth()) }

private val MonthRows = Month.entries.chunked(size = 3)
private val MonthCountWidth = 40.dp
private const val PREVIOUS_ROTATION = 90f
private const val NEXT_ROTATION = -90f
