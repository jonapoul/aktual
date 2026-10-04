package aktual.budget.home.ui

import aktual.budget.home.vm.HomeState
import aktual.budget.home.vm.HomeViewModel
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
internal fun HomeScreen(
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  HomeScaffold(state = state, modifier = modifier)
}

@Composable
private fun HomeScaffold(state: HomeState, modifier: Modifier = Modifier) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { HomeTitle(budgetName = state.budgetName) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      // Placeholder until the home cards land
      FailureScreen(
        modifier = Modifier.padding(innerPadding),
        title = Strings.homeEmpty,
        reason = null,
        action = null,
        icon = null,
        background = colors.tableBackground,
      )
    }
  }
}

@Composable
private fun HomeTitle(budgetName: String?, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(text = Strings.homeTitle, maxLines = 1, overflow = Ellipsis)
    if (budgetName != null) {
      Text(
        text = budgetName,
        style = MaterialTheme.typography.bodySmall,
        color = colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    }
  }
}

private class HomeStateProvider :
  ColoredParameterProvider<HomeState>(
    HomeState(budgetName = "Household budget"),
    HomeState(budgetName = null),
  )

@PortraitPreview
@Composable
private fun PreviewHomeScaffold(
  @PreviewParameter(HomeStateProvider::class) params: ColoredParams<HomeState>
) = PreviewWithColoredParams(params) { HomeScaffold(state = this) }
