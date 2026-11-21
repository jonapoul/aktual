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
            modifier = Modifier.align(Center).height(48.dp),
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

private const val OBTAINIUM_URL =
  "https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/%7B%22id%22%3A%22dev.jonpoulton.aktual.app%22%2C%22" +
    "url%22%3A%22https%3A%2F%2FGitHub.com%2Fjonapoul%2Faktual%22%2C%22author%22%3A%22jonapoul%22%2C%22name%22%3A%22" +
    "Aktual%20Budget%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5" +
    "C%22%3Atrue%2C%5C%22includeDrafts%5C%22%3Afalse%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterRel" +
    "easeTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertReleaseTitlesFilter%5C%22%3Afalse%2C%5C%22filterReleaseNo" +
    "tesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertReleaseNotesFilter%5C%22%3Afalse%2C%5C%22verifyLatestTag%5C%22%3" +
    "Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Atrue%" +
    "2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchG" +
    "roupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afals" +
    "e%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertAP" +
    "KFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22includeZips%5C%22%3Atrue%2C%5C%22zipped" +
    "ApkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22includeTarballs%5C%22%3Afalse%2C%5C%22tarballedApkFilterRegEx%5C%2" +
    "2%3A%5C%22%5C%22%2C%5C%22minimumUpdateAgeDays%5C%22%3A%5C%22%5C%22%2C%5C%22exemptFromBackgroundUpdates%5C%22%3" +
    "Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22detec" +
    "tionDateAsReleaseDate%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C" +
    "%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22installerMode%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGoogleP" +
    "lay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22allowedSigningCertHashes%5C%22%3A%5C%22%5C%22%2C" +
    "%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthStaleRelease%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthSta" +
    "leVersion%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthFailedChecks%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthArchi" +
    "ved%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthFilteredReleases%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%2" +
    "2%3Anull%7D"
