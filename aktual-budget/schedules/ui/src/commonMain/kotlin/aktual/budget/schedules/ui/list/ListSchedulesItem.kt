package aktual.budget.schedules.ui.list

import aktual.budget.schedules.vm.Schedule
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.CardShape
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.RowShape
import aktual.core.ui.ThemeParameters
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
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

/** Keep in sync with [ShimmerListSchedulesItem] */
@Composable
internal fun ListSchedulesItem(
  schedule: Schedule,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(theme.tableBackground, RowShape)
        .border(Dp.Hairline, theme.tableBorder, RowShape)
        .padding(ListSchedulesDS.itemPadding)
        .clickable { onAction(Open(schedule.id)) },
    horizontalArrangement =
      Arrangement.spacedBy(ListSchedulesDS.itemHorizontalSpacing, Alignment.Start),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text("TBC")
  }
}

/** Keep in sync with [ListSchedulesItem] */
@Composable
internal fun ShimmerListSchedulesItem(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val shimmer = rememberShimmer(ShimmerBounds.Window)
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(theme.tableBackground, RowShape)
        .border(Dp.Hairline, theme.tableBorder, RowShape)
        .padding(ListSchedulesDS.itemPadding)
        .shimmer(shimmer),
    horizontalArrangement =
      Arrangement.spacedBy(ListSchedulesDS.itemHorizontalSpacing, Alignment.Start),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.55f)
            .height(ListSchedulesDS.shimmerItemTextHeight)
            .background(theme.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.35f)
            .height(ListSchedulesDS.shimmerItemTextHeight)
            .background(theme.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.45f)
            .height(ListSchedulesDS.shimmerItemTextHeight)
            .background(theme.pageText, CardShape)
      )
    }

    Box(
      modifier = Modifier.size(LocalMinimumInteractiveComponentSize.current),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier =
          Modifier.size(LocalMinimumInteractiveComponentSize.current / 2)
            .background(theme.pageText, CardShape)
      )
    }
  }
}

@Preview
@Composable
private fun PreviewListItem(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    ListSchedulesItem(schedule = ListSchedulesPreview.scheduleA, onAction = {})
  }

@Preview
@Composable
private fun PreviewLoadingListItem(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) { ShimmerListSchedulesItem() }
