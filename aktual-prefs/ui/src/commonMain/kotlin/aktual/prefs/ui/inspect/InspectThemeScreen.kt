package aktual.prefs.ui.inspect

import aktual.app.nav.BackNavigator
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.OpenInNew
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.theme.DarkTheme
import aktual.core.theme.LightTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import aktual.core.theme.isLight
import aktual.core.ui.AktualTypography
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.ui.inspect.InspectThemeAction.NavBack
import aktual.prefs.ui.inspect.InspectThemeAction.OpenRepo
import aktual.prefs.ui.inspect.InspectThemeAction.Retry
import aktual.prefs.vm.inspect.InspectThemeState
import aktual.prefs.vm.inspect.InspectThemeState.Loaded
import aktual.prefs.vm.inspect.InspectThemeState.Loading
import aktual.prefs.vm.inspect.InspectThemeState.NotFound
import aktual.prefs.vm.inspect.InspectThemeViewModel
import aktual.prefs.vm.inspect.ThemeProperty
import aktual.prefs.vm.theme.properties
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlin.math.roundToInt

@Composable
fun InspectThemeScreen(
  back: BackNavigator,
  themeId: ThemeId,
  viewModel: InspectThemeViewModel = metroViewModel(themeId),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  InspectThemeScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        OpenRepo -> viewModel.openRepo()
        Retry -> viewModel.retry()
      }
    },
  )
}

@Composable
private fun metroViewModel(themeId: ThemeId) =
  assistedMetroViewModel<InspectThemeViewModel, InspectThemeViewModel.Factory> { create(themeId) }

@Composable
private fun InspectThemeScaffold(state: InspectThemeState, onAction: (InspectThemeAction) -> Unit) {
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  val title =
    when (state) {
      is Loading -> Strings.settingsThemeInspectLoading
      is NotFound -> Strings.settingsThemeInspectNotFound
      is Loaded -> state.id.value
    }

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.isScrollInProgress),
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(title) },
        actions = { if (state is Loaded && state.isCustom) OpenRepoButton(onAction) },
      )
    }
  ) { innerPadding ->
    InspectThemeContent(
      modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
      contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
      state = state,
      listState = listState,
      onAction = onAction,
    )
  }
}

@Composable
private fun OpenRepoButton(onAction: (InspectThemeAction) -> Unit) {
  IconButton(onClick = { onAction(OpenRepo) }) {
    Icon(
      imageVector = MaterialIcons.OpenInNew,
      contentDescription = Strings.settingsThemeInspectOpenRepo,
    )
  }
}

@Composable
private fun InspectThemeContent(
  state: InspectThemeState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: (InspectThemeAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  when (state) {
    is Loading -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }

    is NotFound -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        FailureScreen(
          modifier = Modifier.background(theme.tableBackground, CardShape),
          title = Strings.settingsThemeInspectNotFound,
          reason = Strings.settingsThemeInspectNotFoundReason(state.id.value),
          action =
            FailureAction(
              text = { Strings.settingsThemeInspectRetry },
              onClick = { onAction(Retry) },
              icon = MaterialIcons.Refresh,
            ),
        )
      }
    }

    is Loaded -> {
      LazyColumn(
        modifier = modifier.fillMaxSize().scrollbar(listState).padding(Dimens.Large),
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        items(state.properties) { property -> ThemePropertyRow(property) }

        item {
          BottomStatusBarSpacing()
          BottomNavBarSpacing()
        }
      }
    }
  }
}

@Composable
private fun ThemePropertyRow(property: ThemeProperty) {
  Row(
    modifier =
      Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(property.color).padding(4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val textColor =
      remember(property.color) { if (property.color.isLight()) Color.Black else Color.White }
    Text(
      modifier = Modifier.weight(1f),
      text = property.name,
      color = textColor,
      style = AktualTypography.bodySmall,
      textAlign = TextAlign.Start,
      maxLines = 1,
    )

    Text(
      text = property.color.toHexString(),
      color = textColor,
      style = AktualTypography.labelMedium,
      textAlign = TextAlign.End,
      maxLines = 1,
    )
  }
}

@Stable
@Suppress("MagicNumber")
private fun Color.toHexString(): String {
  val r = (red * 255).roundToInt()
  val g = (green * 255).roundToInt()
  val b = (blue * 255).roundToInt()
  val a = (alpha * 255).roundToInt()
  return if (a == 255) {
    "#%02X%02X%02X".format(r, g, b)
  } else {
    "#%02X%02X%02X%02X".format(a, r, g, b)
  }
}

@Preview
@Composable
private fun PreviewInspectTheme(
  @PreviewParameter(InspectThemePreviewProvider::class) params: ThemedParams<InspectThemeState>
) = PreviewWithTheme(params.theme) { InspectThemeScaffold(state = params.data, onAction = {}) }

private class InspectThemePreviewProvider :
  ThemedParameterProvider<InspectThemeState>(
    NotFound(id = ThemeId("username/repo")),
    Loading,
    Loaded(LightTheme.id, false, LightTheme.properties()),
    Loaded(DarkTheme.id, false, DarkTheme.properties()),
    Loaded(MidnightTheme.id, true, MidnightTheme.properties()),
  )
