package actual.preview

import actual.budget.reports.ui.ChooseReportTypeScaffold
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun PreviewLoaded() = PreviewThemedScreen {
  ChooseReportTypeScaffold(
    onAction = {},
  )
}
