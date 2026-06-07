package aktual.budget.tags.ui.list

import aktual.budget.tags.vm.list.ListTagsViewModel
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PageBackground
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
internal fun ListTagsScreen(
  modifier: Modifier = Modifier,
  @Suppress("UNUSED_PARAMETER") viewModel: ListTagsViewModel = metroViewModel(),
) {
  ListTagsScaffold(modifier = modifier)
}

@Composable
private fun ListTagsScaffold(modifier: Modifier = Modifier) {
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, listState),
        colors = colors.transparentTopAppBarColors(),
        title = { Text(text = Strings.tagsTitle) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      // Empty for now — content lands here once tags are loaded
      LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), state = listState) {
        // TBC
      }
    }
  }
}

@Preview
@Composable
private fun PreviewListTagsScaffold(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ListTagsScaffold() }
