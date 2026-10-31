package aktual.budget.reports.components

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

val ChartHeight = 200.dp
val ChartPadding = 8.dp

@Composable
fun ShimmerDashboardItem(modifier: Modifier = Modifier) =
  LoadingChart(modifier = modifier.fillMaxWidth().height(ChartHeight + ChartPadding * 2))

@Composable
private fun LoadingChart(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)
  Box(modifier = modifier.clip(CardShape).shimmer(shimmer).background(colors.tableText))
}
