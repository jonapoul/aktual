package aktual.prefs.ui

import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardColumn
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.LandscapePreview
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.isCompactWidth
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.vm.ListPreference
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf

/**
 * Settings screen with a [preview] of the settings' output pinned above the list on narrow windows,
 * or beside it on wide ones, so it stays visible while the user changes options.
 */
@Composable
internal fun PreviewSettingsScaffold(
  title: String,
  description: String?,
  onBack: () -> Unit,
  preview: @Composable () -> Unit,
  highlight: SettingsHighlight? = null,
  content: LazyListScope.() -> Unit,
) {
  val listState = rememberLazyListState()

  ProvidePreferenceHighlight(highlight, listState) {
    Scaffold(
      topBar = {
        TopAppBar(
          colors = colors.transparentTopAppBarColors(),
          navigationIcon = { NavBackIconButton(onClick = onBack) },
          title = { Text(title) },
        )
      },
    ) { innerPadding ->
      Box {
        PageBackground()
        if (isCompactWidth()) {
          Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PreviewPane(preview, Modifier.fillMaxWidth().padding(Dimens.Large))
            SettingsList(
              listState = listState,
              description = description,
              modifier =
                Modifier.weight(1f)
                  .fillMaxWidth()
                  .scrollbar(listState)
                  .padding(horizontal = Dimens.Large),
              content = content,
            )
          }
        } else {
          Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PreviewPane(preview, Modifier.weight(1f).padding(Dimens.Large))
            SettingsList(
              listState = listState,
              description = description,
              modifier =
                Modifier.weight(1f).fillMaxSize().scrollbar(listState).padding(Dimens.Large),
              content = content,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PreviewPane(preview: @Composable () -> Unit, modifier: Modifier = Modifier) {
  CardColumn(modifier = modifier, horizontalAlignment = CenterHorizontally) { preview() }
}

@Composable
private fun PreviewScaffold() {
  var selected by remember { mutableStateOf("One") }
  PreviewSettingsScaffold(
    title = "Settings",
    description = "Pick an option and see it above",
    onBack = {},
    preview = { Text(text = selected, style = typography.headlineMedium) },
  ) {
    item {
      PreviewOptionGroup(
        title = "Options",
        preference =
          ListPreference(
            value = selected,
            options = persistentListOf("One", "Two", "Three"),
            onChange = { selected = it },
          ),
        label = { it },
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewPortrait(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { PreviewScaffold() }

@LandscapePreview
@Composable
private fun PreviewLandscape(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { PreviewScaffold() }
