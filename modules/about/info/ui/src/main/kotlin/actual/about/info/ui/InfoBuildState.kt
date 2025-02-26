package actual.about.info.ui

import actual.about.info.res.InfoStrings
import actual.about.info.vm.BuildState
import actual.core.ui.PreviewColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun InfoBuildState(
  buildState: BuildState,
  onAction: (InfoAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    BuildStateItem(
      modifier = Modifier.padding(ItemMargin),
      icon = Icons.Filled.Numbers,
      title = InfoStrings.version,
      subtitle = buildState.buildVersion,
    )

    BuildStateItem(
      modifier = Modifier.padding(ItemMargin),
      icon = Icons.Filled.CalendarToday,
      title = InfoStrings.date,
      subtitle = buildState.buildDate,
    )

    BuildStateItem(
      modifier = Modifier.padding(ItemMargin),
      icon = Icons.Filled.Code,
      title = InfoStrings.repo,
      subtitle = buildState.sourceCodeRepo,
      onClick = { onAction(InfoAction.OpenSourceCode) },
    )
  }
}

private val ItemMargin = PaddingValues(horizontal = 6.dp, vertical = 3.dp)

@Preview
@Composable
private fun PreviewBuildState() = PreviewColumn(
  modifier = Modifier.fillMaxWidth(),
) {
  InfoBuildState(
    modifier = Modifier.fillMaxWidth(),
    buildState = PreviewBuildState,
    onAction = {},
  )
}
