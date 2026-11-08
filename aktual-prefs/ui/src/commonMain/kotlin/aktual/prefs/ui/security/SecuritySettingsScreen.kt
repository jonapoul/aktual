package aktual.prefs.ui.security

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheckCircle1
import aktual.core.icons.ExclamationOutline
import aktual.core.icons.Location
import aktual.core.icons.ViewHide
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Security
import aktual.core.icons.material.Timer
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.rememberLocationPermissionRequest
import aktual.prefs.AppLockTimeout
import aktual.prefs.components.BasicPreferenceItem
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.security.SecuritySettingsState
import aktual.prefs.vm.security.SecuritySettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Suppress("ViewModelForwarding", "ComposeViewModelForwarding")
@Composable
fun SecuritySettingsScreen(
  back: BackNavigator,
  viewModel: SecuritySettingsViewModel = metroViewModel<SecuritySettingsViewModel>(),
) {
  LifecycleResumeEffect(viewModel) {
    viewModel.refreshAvailability()
    onPauseOrDispose {}
  }

  val state by viewModel.state.collectAsStateWithLifecycle()
  val requestLocation =
    rememberLocationPermissionRequest(openSettingsIfBlocked = true) {
      viewModel.refreshAvailability()
    }
  SecuritySettingsScaffold(
    state = state,
    onBack = { back() },
    onRequestLocation = requestLocation,
  )
}

@Composable
private fun SecuritySettingsScaffold(
  state: SecuritySettingsState,
  onBack: () -> Unit,
  onRequestLocation: () -> Unit,
) {
  SettingsListScaffold(title = Strings.settingsSecurity, description = null, onBack = onBack) {
    item {
      BooleanPreferenceItem(
        preference = state.appLockEnabled,
        title = Strings.settingsSecurityAppLock,
        subtitle =
          if (state.isScreenLockAvailable) {
            Strings.settingsSecurityAppLockDesc
          } else {
            Strings.settingsSecurityAppLockUnavailable
          },
        icon = MaterialIcons.Security,
      )
    }
    item {
      ListPreferenceItem(
        preference = state.appLockTimeout,
        optionString = { it.string() },
        optionSuffix = null,
        title = Strings.settingsSecurityTimeout,
        subtitle = null,
        icon = MaterialIcons.Timer,
      )
    }
    item {
      BooleanPreferenceItem(
        preference = state.hidePreviewInAppSwitcher,
        title = Strings.settingsSecurityHidePreview,
        subtitle = Strings.settingsSecurityHidePreviewDesc,
        icon = AktualIcons.ViewHide,
      )
    }
    if (state.isLocationSupported) {
      item {
        PreferenceGroup(title = Strings.settingsSecurityLocations) {
          BooleanPreferenceItem(
            preference = state.autoSavePayeeLocations,
            title = Strings.settingsSecurityAutoSaveLocations,
            subtitle = Strings.settingsSecurityAutoSaveLocationsDesc,
            icon = AktualIcons.Location,
          )
          LocationPermissionItem(
            hasPermission = state.hasLocationPermission,
            onRequest = onRequestLocation,
          )
        }
      }
    }
  }
}

@Composable
private fun LocationPermissionItem(hasPermission: Boolean, onRequest: () -> Unit) {
  if (hasPermission) {
    BasicPreferenceItem(
      title = Strings.settingsSecurityLocationAllowed,
      subtitle = null,
      icon = AktualIcons.CheckCircle1,
      onClick = null,
    )
  } else {
    BasicPreferenceItem(
      title = Strings.settingsSecurityLocationDenied,
      subtitle = Strings.settingsSecurityLocationDeniedDesc,
      icon = AktualIcons.ExclamationOutline,
      onClick = onRequest,
    )
  }
}

@Composable
private fun AppLockTimeout.string(): String =
  when (this) {
    Immediately -> Strings.settingsSecurityTimeoutImmediately
    Seconds30 -> Strings.settingsSecurityTimeoutSeconds30
    Minutes1 -> Strings.settingsSecurityTimeoutMinutes1
    Minutes5 -> Strings.settingsSecurityTimeoutMinutes5
    Minutes15 -> Strings.settingsSecurityTimeoutMinutes15
  }

@PortraitPreview
@Composable
private fun PreviewSecuritySettingsScaffold(
  @PreviewParameter(SecuritySettingsProvider::class) params: ColorParams<SecuritySettingsState>,
) =
  PreviewWithColorParams(params) {
    SecuritySettingsScaffold(state = this, onBack = {}, onRequestLocation = {})
  }

private class SecuritySettingsProvider :
  ColorParamsProvider<SecuritySettingsState>(
    SecuritySettingsState(
      appLockEnabled = BooleanPreference(true),
      appLockTimeout = ListPreference(Minutes1),
      hidePreviewInAppSwitcher = BooleanPreference(true, enabled = false),
      autoSavePayeeLocations = BooleanPreference(true),
      isLocationSupported = true,
      hasLocationPermission = true,
      isScreenLockAvailable = true,
    ),
    SecuritySettingsState(
      appLockEnabled = BooleanPreference(false, enabled = false),
      appLockTimeout = ListPreference(Immediately, enabled = false),
      hidePreviewInAppSwitcher = BooleanPreference(false),
      autoSavePayeeLocations = BooleanPreference(true, enabled = false),
      isLocationSupported = true,
      hasLocationPermission = false,
      isScreenLockAvailable = false,
    ),
  )
