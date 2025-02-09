package actual.about.ui

import actual.about.res.AboutStrings
import actual.about.vm.BuildState
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
internal fun AboutBuildState(
  buildState: BuildState,
  onAction: (AboutAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    AboutBuildStateItem(
      modifier = Modifier.padding(ItemMargin),
      icon = Icons.Filled.Numbers,
      title = AboutStrings.version,
      subtitle = buildState.buildVersion,
    )

    AboutBuildStateItem(
      modifier = Modifier.padding(ItemMargin),
      icon = Icons.Filled.CalendarToday,
      title = AboutStrings.date,
      subtitle = buildState.buildDate,
    )

    AboutBuildStateItem(
      modifier = Modifier.padding(ItemMargin),
      icon = Icons.Filled.Code,
      title = AboutStrings.repo,
      subtitle = buildState.sourceCodeRepo,
      onClick = { onAction(AboutAction.OpenSourceCode) },
    )
  }
}

private val ItemMargin = PaddingValues(horizontal = 6.dp, vertical = 3.dp)

@Preview
@Composable
private fun PreviewBuildState() = PreviewColumn(
  modifier = Modifier.fillMaxWidth(),
) {
  AboutBuildState(
    modifier = Modifier.fillMaxWidth(),
    buildState = PreviewBuildState,
    onAction = {},
  )
}
