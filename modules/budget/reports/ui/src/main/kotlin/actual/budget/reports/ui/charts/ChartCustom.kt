package actual.budget.reports.ui.charts

import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.CustomData
import actual.budget.reports.vm.ReportTimeRange
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun CustomChart(
  data: CustomData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
) {
  if (compact) {
    Header(
      modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth(),
      data = data,
      theme = theme,
    )
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = Strings.reportsCustomNoop,
      style = MaterialTheme.typography.titleLarge,
      color = theme.warningText,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun Header(
  data: CustomData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier.fillMaxWidth(),
) {
  Text(
    modifier = Modifier.weight(1f),
    text = data.title,
    color = theme.pageText,
    overflow = TextOverflow.Ellipsis,
  )

  Text(
    text = dateRange(data.range),
    color = theme.pageTextSubdued,
    overflow = TextOverflow.Ellipsis,
  )
}

@Composable
@ReadOnlyComposable
private fun dateRange(timeRange: ReportTimeRange): String = when (timeRange) {
  is ReportTimeRange.Relative -> timeRange.type.string()
  is ReportTimeRange.Specific -> dateRange(timeRange.range.start, timeRange.range.endInclusive)
}

@ScreenPreview
@Composable
private fun PreviewRegular() = PreviewScreen(isPrivacyEnabled = false) {
  Surface(Modifier.background(LocalTheme.current.tableBackground)) {
    CustomChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .padding(horizontal = 4.dp),
      data = PreviewCustom.DATA,
      compact = false,
    )
  }
}

@ScreenPreview
@Composable
private fun PreviewRegularPrivate() = PreviewScreen(isPrivacyEnabled = true) {
  Surface(Modifier.background(LocalTheme.current.tableBackground)) {
    CustomChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .padding(horizontal = 4.dp),
      data = PreviewCustom.DATA,
      compact = false,
    )
  }
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewColumn(isPrivacyEnabled = false) {
  CustomChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .height(300.dp)
      .padding(4.dp),
    data = PreviewCustom.DATA,
    compact = true,
  )
}
