package aktual.settings.ui.inspect

import aktual.core.icons.MaterialIcons
import aktual.core.icons.OpenInNew
import aktual.core.l10n.Strings
import aktual.core.theme.DarkTheme
import aktual.core.theme.LightTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import aktual.core.theme.isLight
import aktual.core.ui.AktualTypography
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.Dimens
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.settings.vm.inspect.InspectThemeState
import aktual.settings.vm.inspect.InspectThemeViewModel
import aktual.settings.vm.inspect.ThemeProperty
import aktual.settings.vm.theme.properties
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
  nav: InspectThemeNavigator,
  themeId: Theme.Id,
  viewModel: InspectThemeViewModel = metroViewModel(themeId),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  InspectThemeScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        InspectThemeAction.NavBack -> nav.navBack()
        InspectThemeAction.OpenRepo -> viewModel.openRepo()
        InspectThemeAction.Retry -> viewModel.retry()
      }
    },
  )
}

@Composable
private fun metroViewModel(themeId: Theme.Id) =
  assistedMetroViewModel<InspectThemeViewModel, InspectThemeViewModel.Factory> { create(themeId) }

@Composable
private fun InspectThemeScaffold(state: InspectThemeState, onAction: (InspectThemeAction) -> Unit) {
  val theme = LocalTheme.current

  val title =
    when (state) {
      is InspectThemeState.Loading -> Strings.settingsThemeInspectLoading
      is InspectThemeState.NotFound -> Strings.settingsThemeInspectNotFound
      is InspectThemeState.Loaded -> state.id.value
    }

  Scaffold(
    topBar = {
      TopAppBar(
        colors = theme.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(InspectThemeAction.NavBack) } },
        title = { Text(title) },
        actions = {
          if (state is InspectThemeState.Loaded && state.isCustom) {
            IconButton(onClick = { onAction(InspectThemeAction.OpenRepo) }) {
              Icon(
                imageVector = MaterialIcons.OpenInNew,
                contentDescription = Strings.settingsThemeInspectOpenRepo,
              )
            }
          }
        },
      )
    }
  ) { innerPadding ->
    InspectThemeContent(
      modifier = Modifier.padding(innerPadding),
      state = state,
      onAction = onAction,
    )
  }
}

@Composable
private fun InspectThemeContent(
  state: InspectThemeState,
  onAction: (InspectThemeAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (state) {
    is InspectThemeState.Loading -> {
      Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }

    is InspectThemeState.NotFound -> {
      FailureScreen(
        modifier = modifier.fillMaxSize(),
        title = Strings.settingsThemeInspectNotFound,
        reason = Strings.settingsThemeInspectNotFoundReason(state.id.value),
        retryText = Strings.settingsThemeInspectRetry,
        onClickRetry = { onAction(InspectThemeAction.Retry) },
      )
    }

    is InspectThemeState.Loaded -> {
      val listState = rememberLazyListState()
      LazyColumn(
        modifier = modifier.fillMaxSize().padding(Dimens.Large).scrollbar(listState),
        state = listState,
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
      text = remember(property.color) { property.color.toHexString() },
      color = textColor,
      style = AktualTypography.labelMedium,
      textAlign = TextAlign.End,
      maxLines = 1,
    )
  }
}

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
) =
  PreviewWithColorScheme(params.theme) { InspectThemeScaffold(state = params.data, onAction = {}) }

private class InspectThemePreviewProvider :
  ThemedParameterProvider<InspectThemeState>(
    InspectThemeState.NotFound(id = Theme.Id("username/repo")),
    InspectThemeState.Loading,
    InspectThemeState.Loaded(LightTheme.id, false, LightTheme.properties()),
    InspectThemeState.Loaded(DarkTheme.id, false, DarkTheme.properties()),
    InspectThemeState.Loaded(MidnightTheme.id, true, MidnightTheme.properties()),
  )
