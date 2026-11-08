package aktual.prefs.ui

import aktual.core.nav.SettingsHighlight
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.components.LocalPreferenceHighlighter
import aktual.prefs.components.ScrollToHighlight
import aktual.prefs.components.rememberPreferenceHighlighter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsListScaffold(
  title: String,
  description: String?,
  onBack: () -> Unit,
  actions: @Composable RowScope.() -> Unit = {},
  highlight: SettingsHighlight? = null,
  content: LazyListScope.() -> Unit,
) {
  val listState = rememberLazyListState()
  val hazeState = rememberHazedTopBarState()

  ProvidePreferenceHighlight(highlight, listState) {
    Scaffold(
      topBar = {
        TopAppBar(
          modifier = Modifier.hazedTopBar(hazeState, listState),
          colors = colors.transparentTopAppBarColors(),
          navigationIcon = { NavBackIconButton(onClick = onBack) },
          title = { Text(title) },
          actions = actions,
        )
      },
    ) { innerPadding ->
      Box {
        PageBackground()
        LazyColumn(
          modifier =
            Modifier.hazedTopBarContent(hazeState, innerPadding)
              .fillMaxSize()
              .scrollbar(listState)
              .padding(Dimens.Large),
          state = listState,
          contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (description != null) {
            item {
              Text(
                modifier = Modifier.padding(horizontal = 6.dp),
                text = description,
                fontWeight = Light,
                style = typography.bodyMedium,
                color = colors.pageTextLight,
              )
            }
          }
          content()
          item { BottomSpacing() }
        }
      }
    }
  }
}

// Scrolls to and flashes the setting matching highlight, once
@Composable
internal fun ProvidePreferenceHighlight(
  highlight: SettingsHighlight?,
  listState: LazyListState,
  content: @Composable () -> Unit,
) {
  val highlighter = rememberPreferenceHighlighter(highlight?.group, highlight?.title)
  if (highlighter != null) ScrollToHighlight(highlighter, listState)
  CompositionLocalProvider(LocalPreferenceHighlighter provides highlighter, content = content)
}
