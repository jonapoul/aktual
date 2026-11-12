package aktual.prefs.vm.security

import aktual.core.utils.LocationProvider
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.AppLockAuthenticator
import aktual.prefs.SecurityPreferences
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.collectAsState
import aktual.prefs.vm.launchAndSet
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class SecuritySettingsViewModel
internal constructor(
  private val preferences: SecurityPreferences,
  private val authenticator: AppLockAuthenticator,
  private val locationProvider: LocationProvider,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val isAvailable = MutableStateFlow(authenticator.isAvailable())
  private val hasLocationPermission = MutableStateFlow(locationProvider.hasPermission())

  val state: StateFlow<SecuritySettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val appLockEnabled by collectAsState(preferences.appLockEnabled)
      val appLockTimeout by collectAsState(preferences.appLockTimeout)
      val hidePreview by collectAsState(preferences.hidePreviewInAppSwitcher)
      val autoSaveLocations by collectAsState(preferences.autoSavePayeeLocations)
      val lookUpAddresses by collectAsState(preferences.lookUpPayeeAddresses)
      val isAvailable by isAvailable.collectAsState()
      val hasLocationPermission by hasLocationPermission.collectAsState()
      SecuritySettingsState(
        appLockEnabled =
          BooleanPreference(
            value = appLockEnabled,
            // Still allow turning it off
            enabled = isAvailable || appLockEnabled,
            onChange = ::setAppLockEnabled,
          ),
        appLockTimeout =
          ListPreference(
            value = appLockTimeout,
            enabled = appLockEnabled,
            onChange = { launchAndSet(preferences.appLockTimeout, it) },
          ),
        // The app lock always hides the preview
        hidePreviewInAppSwitcher =
          BooleanPreference(
            value = hidePreview || appLockEnabled,
            enabled = !appLockEnabled,
            onChange = { launchAndSet(preferences.hidePreviewInAppSwitcher, it) },
          ),
        autoSavePayeeLocations =
          BooleanPreference(
            value = autoSaveLocations,
            enabled = hasLocationPermission,
            onChange = { launchAndSet(preferences.autoSavePayeeLocations, it) },
          ),
        lookUpPayeeAddresses =
          BooleanPreference(
            value = lookUpAddresses,
            onChange = { launchAndSet(preferences.lookUpPayeeAddresses, it) },
          ),
        isLocationSupported = locationProvider.isSupported,
        hasLocationPermission = hasLocationPermission,
        isScreenLockAvailable = isAvailable,
      )
    }

  // The user might have changed their screen lock or permissions while we were in the background
  fun refreshAvailability() {
    isAvailable.update { authenticator.isAvailable() }
    hasLocationPermission.update { locationProvider.hasPermission() }
  }

  private fun setAppLockEnabled(enabled: Boolean) {
    viewModelScope.launch {
      // Make sure the user can unlock before turning it on
      if (!enabled || authenticator.authenticate()) {
        preferences.appLockEnabled.set(enabled)
      }
      refreshAvailability()
    }
  }
}
