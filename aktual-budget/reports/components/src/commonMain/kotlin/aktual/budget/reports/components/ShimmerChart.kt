package aktual.budget.reports.components

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.tableRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

val ChartHeight = 200.dp
val ChartPadding = 8.dp

@Composable
fun ShimmerDashboardItem(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)
  Column(
    modifier =
      modifier.dashboardItem(colors).padding(ChartPadding).height(ChartHeight).shimmer(shimmer),
    verticalArrangement = Arrangement.spacedBy(Dimens.Large),
  ) {
    val bar = Modifier.background(colors.tableText, CardShape)
    Box(modifier = bar.fillMaxWidth(fraction = 0.4f).height(Dimens.VeryLarge))
    Box(modifier = bar.fillMaxWidth().weight(1f))
  }
}

fun Modifier.dashboardItem(colors: Colors): Modifier = fillMaxWidth().tableRow(colors, CardShape)

@Preview
@Composable
private fun PreviewShimmerDashboardItem(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    ShimmerDashboardItem()
  }
