package aktual.prefs.ui.updates

import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudDownload
import aktual.core.l10n.Drawables
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.ui.ColorParameters
import aktual.core.ui.NoticeBanner
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.updates.UpdateSettingsState
import aktual.prefs.vm.updates.UpdateSettingsViewModel
import aktual.theme.Colors
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun UpdateSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: UpdateSettingsViewModel = metroViewModel<UpdateSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val uriHandler = LocalUriHandler.current
  UpdateSettingsScaffold(
    state = state,
    highlight = highlight,
    onBack = back::invoke,
    onOpenObtainium = { uriHandler.openUri(OBTAINIUM_URL) },
  )
}

@Composable
private fun UpdateSettingsScaffold(
  state: UpdateSettingsState,
  onBack: () -> Unit,
  onOpenObtainium: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsUpdates,
    description = Strings.settingsUpdatesDesc,
    highlight = highlight,
    onBack = onBack,
  ) {
    item {
      BooleanPreferenceItem(
        preference = state.checkNightlies,
        title = Strings.settingsUpdatesNightly,
        subtitle = Strings.settingsUpdatesNightlyDesc,
        icon = AktualIcons.CloudDownload,
      )
    }

    item {
      NoticeBanner(
        text = Strings.settingsUpdatesObtainium,
        onClick = onOpenObtainium,
        extra = {
          Image(
            modifier = Modifier.height(48.dp),
            painter = Drawables.obtainiumBadge,
            contentDescription = Strings.settingsUpdatesObtainiumBadge,
          )
        },
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewUpdateSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    UpdateSettingsScaffold(
      state = UpdateSettingsState(checkNightlies = BooleanPreference(false)),
      onBack = {},
      onOpenObtainium = {},
    )
  }

private const val OBTAINIUM_URL = "https://github.com/jonapoul/aktual#obtainium"
