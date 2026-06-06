package aktual.budget.list.ui

import aktual.core.theme.Colors
import aktual.core.theme.DarkColors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/** Shimmer placeholder that mimics the layout of [BudgetListItem]. */
@Composable
internal fun ShimmerBudgetList(
  numLoadingItems: Int,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.Medium)) {
    repeat(numLoadingItems) { ShimmerBudgetListItem() }
  }
}

@Composable
internal fun ShimmerBudgetListItem(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(ShimmerBounds.Window)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.buttonNormalBackground, RowShape)
        .border(Dp.Hairline, colors.pillBorderDark, RowShape)
        .padding(horizontal = 15.dp, vertical = 12.dp)
        .shimmer(shimmer),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Mimics the text column in BudgetListItem
    Column(modifier = Modifier.weight(1f)) {
      // Budget name placeholder (real: 16sp W700 ≈ 20dp)
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.55f)
            .height(20.dp)
            .background(colors.pageText, CardShape)
      )

      // State text placeholder (real: 18dp icon row with 13sp text)
      Box(
        modifier =
          Modifier.padding(top = 4.dp)
            .fillMaxWidth(fraction = 0.35f)
            .height(18.dp)
            .background(colors.pageText, CardShape)
      )

      // Description placeholder (real: 10sp / 12sp lineHeight x ~2 for 2 lines)
      Box(
        modifier =
          Modifier.padding(top = 4.dp)
            .fillMaxWidth(fraction = 0.45f)
            .height(20.dp)
            .background(colors.pageText, CardShape)
      )
    }
  }
}

// just to validate that they're roughly the same height
@Preview(widthDp = 1000)
@Composable
private fun PreviewShimmerVsReal() =
  PreviewWithColors(DarkColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      ShimmerBudgetListItem(modifier = Modifier.weight(1f))

      BudgetListItem(
        modifier = Modifier.weight(1f),
        budget = PreviewBudgetSynced,
        onClickOpen = {},
        onClickDelete = {},
      )
    }
  }

@Preview
@Composable
private fun PreviewShimmerBudgetList(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerBudgetList(numLoadingItems = 4) }
