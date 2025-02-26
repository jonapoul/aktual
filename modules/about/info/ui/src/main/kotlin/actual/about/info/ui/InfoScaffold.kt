package actual.about.info.ui

import actual.about.info.vm.BuildState
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import alakazam.android.ui.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@Composable
internal fun InfoScaffold(
  buildState: BuildState,
  onAction: (InfoAction) -> Unit,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
  val theme = LocalTheme.current
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = { InfoTopBar(theme, scrollBehavior, onAction) },
  ) { innerPadding ->
    InfoScreenContent(
      modifier = Modifier
        .padding(innerPadding)
        .background(theme.pageBackground),
      buildState = buildState,
      onAction = onAction,
      theme = theme,
    )
  }
}

@Composable
private fun InfoScreenContent(
  buildState: BuildState,
  onAction: (InfoAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
      .background(theme.pageBackground)
      .fillMaxSize()
      .padding(15.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    InfoHeader(
      modifier = Modifier
        .wrapContentWidth()
        .padding(vertical = 5.dp),
      year = buildState.year,
      theme = theme,
    )

    InfoBuildState(
      modifier = Modifier.fillMaxWidth(),
      buildState = buildState,
      onAction = onAction,
    )

    VerticalSpacer(weight = 1f)

    InfoButtons(
      modifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth(),
      onAction = onAction,
    )
  }
}

@ScreenPreview
@Composable
private fun PreviewInfo() = PreviewScreen {
  InfoScaffold(
    buildState = PreviewBuildState,
    onAction = {},
  )
}
