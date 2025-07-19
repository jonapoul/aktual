package actual.budget.reports.ui.charts

import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.vm.TextData
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
internal fun TextChart(
  data: TextData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = MarkdownText(
  modifier = modifier,
  markdown = data.content,
  linkColor = theme.pageTextLink,
  isTextSelectable = true,
  imageLoader = null,
  enableUnderlineForLink = true,
)

@ScreenPreview
@Composable
private fun PreviewRegular() = PreviewScreen(isPrivacyEnabled = false) {
  Surface(Modifier.background(LocalTheme.current.tableBackground)) {
    TextChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .padding(horizontal = 4.dp),
      data = PreviewText.DATA,
      compact = false,
    )
  }
}

@ScreenPreview
@Composable
private fun PreviewRegularPrivate() = PreviewScreen(isPrivacyEnabled = true) {
  Surface(Modifier.background(LocalTheme.current.tableBackground)) {
    TextChart(
      modifier = Modifier
        .background(LocalTheme.current.tableBackground, CardShape)
        .padding(horizontal = 4.dp),
      data = PreviewText.DATA,
      compact = false,
    )
  }
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewColumn(isPrivacyEnabled = false) {
  TextChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(4.dp),
    data = PreviewText.DATA,
    compact = true,
  )
}
