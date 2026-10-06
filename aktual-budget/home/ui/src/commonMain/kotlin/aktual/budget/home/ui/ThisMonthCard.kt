@file:Suppress("MagicNumber")

package aktual.budget.home.ui

import aktual.budget.home.vm.ThisMonthCardState
import aktual.budget.home.vm.ThisMonthCardState.Envelope
import aktual.budget.home.vm.ThisMonthCardState.Loaded
import aktual.budget.home.vm.ThisMonthCardState.Tracking
import aktual.budget.model.Amount
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.formattedString
import aktual.core.ui.stringLong
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.datetime.YearMonth

private val ProgressBarHeight = 8.dp

@Composable
internal fun ThisMonthCard(
  state: ThisMonthCardState,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  HomeCard(modifier = modifier) {
    when (state) {
      Loading -> ShimmerThisMonth()
      Failed -> CardError(message = Strings.homeThisMonthFailed, onAction = onAction)
      is Loaded -> ThisMonthContent(state)
    }
  }
}

// Keep this in sync with ThisMonthContent
@Composable
private fun ShimmerThisMonth(modifier: Modifier = Modifier) {
  val bar = Modifier.background(colors.tableText, CardShape)

  Column(
    modifier =
      modifier.fillMaxWidth().padding(horizontal = CardPadding).shimmer(rememberShimmer(Window)),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      Box(modifier = bar.width(72.dp).height(16.dp))
      Box(modifier = bar.width(64.dp).height(16.dp))
    }

    Box(modifier = bar.width(160.dp).height(32.dp))

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Box(modifier = bar.fillMaxWidth().height(ProgressBarHeight))

      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = bar.width(88.dp).height(14.dp))
        Box(modifier = bar.width(112.dp).height(14.dp))
      }
    }
  }
}

@Composable
private fun ThisMonthContent(state: Loaded, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = CardPadding),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    ThisMonthHeader(state.month, state.daysLeft)

    when (state) {
      is Envelope ->
        Headline(
          amount = state.toBudget,
          label =
            if (state.toBudget < Zero) {
              Strings.homeThisMonthOverbudgeted
            } else {
              Strings.homeThisMonthToBudget
            },
          color =
            when {
              state.toBudget < Zero -> colors.toBudgetNegative
              state.toBudget == Zero -> colors.toBudgetZero
              else -> colors.toBudgetPositive
            },
        )

      is Tracking ->
        Headline(
          amount = state.remaining,
          label =
            if (state.remaining < Zero) {
              Strings.homeThisMonthOverspent
            } else {
              Strings.homeThisMonthRemaining
            },
          color = if (state.remaining < Zero) colors.numberNegative else colors.pageText,
        )
    }

    SpentProgress(state)

    if (state is Tracking) {
      Text(
        text =
          Strings.homeThisMonthIncome(
            state.income.formattedString(),
            state.incomeBudgeted.formattedString(),
          ),
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    }
  }
}

@Composable
private fun ThisMonthHeader(month: YearMonth, daysLeft: Int, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f).semantics { heading() },
      text = month.month.stringLong(),
      style = typography.titleSmall,
      fontWeight = SemiBold,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )

    Text(
      text =
        if (daysLeft > 0) {
          Plurals.homeThisMonthDaysLeft(daysLeft, daysLeft)
        } else {
          Strings.homeThisMonthLastDay
        },
      style = typography.labelMedium,
      color = colors.pageTextSubdued,
      maxLines = 1,
    )
  }
}

// The label says which way it went, so the amount is shown without a sign
@Composable
private fun Headline(amount: Amount, label: String, color: Color, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      modifier = Modifier.alignByBaseline(),
      text = amount.absolute().formattedString(),
      style = typography.headlineMedium.tabularFigures(),
      fontWeight = SemiBold,
      color = color,
      maxLines = 1,
    )

    Text(
      modifier = Modifier.weight(1f).alignByBaseline(),
      text = label,
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }
}

@Composable
private fun SpentProgress(state: Loaded, modifier: Modifier = Modifier) {
  val progress = state.progress()
  val isOverspent = state.spent > state.budgeted && state.spent > Zero

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .height(ProgressBarHeight)
          .clip(CircleShape)
          .background(colors.tableRowHeaderBackground)
          .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }
    ) {
      Box(
        modifier =
          Modifier.fillMaxWidth(progress)
            .fillMaxHeight()
            .background(
              color = if (isOverspent) colors.numberNegative else colors.toBudgetPositive,
              shape = CircleShape,
            )
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        modifier = Modifier.weight(1f),
        text = Strings.homeThisMonthSpent(state.spent.formattedString()),
        style = typography.bodySmall.tabularFigures(),
        color = if (isOverspent) colors.numberNegative else colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )

      Text(
        text = Strings.homeThisMonthOfBudgeted(state.budgeted.formattedString()),
        style = typography.bodySmall.tabularFigures(),
        color = colors.pageTextSubdued,
        maxLines = 1,
      )
    }
  }
}

private fun Loaded.progress(): Float =
  when {
    spent <= Zero -> 0f
    budgeted <= Zero -> 1f
    else -> (spent / budgeted).coerceAtMost(1f)
  }

private fun Amount.absolute(): Amount = if (this < Zero) -this else this

@Preview
@Composable
private fun PreviewThisMonthCard(
  @PreviewParameter(ThisMonthCardStateProvider::class) params: ColoredParams<ThisMonthCardState>
) =
  PreviewWithColoredParams(params) {
    ThisMonthCard(modifier = Modifier.padding(16.dp), state = this, onAction = {})
  }

private class ThisMonthCardStateProvider :
  ColoredParameterProvider<ThisMonthCardState>(
    PREVIEW_THIS_MONTH,
    PREVIEW_THIS_MONTH.copy(toBudget = Zero),
    PREVIEW_THIS_MONTH.copy(toBudget = Amount(-120.00), spent = Amount(2_310.40), daysLeft = 0),
    Tracking(
      month = YearMonth(2026, 4),
      daysLeft = 1,
      spent = Amount(1_184.50),
      budgeted = Amount(2_100.00),
      income = Amount(2_650.00),
      incomeBudgeted = Amount(2_800.00),
    ),
    Loading,
    Failed,
  )

internal val PREVIEW_THIS_MONTH =
  Envelope(
    month = YearMonth(2026, 4),
    daysLeft = 12,
    spent = Amount(1_184.50),
    budgeted = Amount(2_100.00),
    toBudget = Amount(342.18),
  )
