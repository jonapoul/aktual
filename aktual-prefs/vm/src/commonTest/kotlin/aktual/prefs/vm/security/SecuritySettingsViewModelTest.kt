package aktual.prefs.vm.security

import aktual.core.model.Coordinates
import aktual.core.utils.LocationProvider
import aktual.prefs.AppLockAuthenticator
import aktual.prefs.SecurityPreferencesImpl
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.test.buildPreferences
import aktual.test.unconfinedViewModelContext
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SecuritySettingsViewModelTest {
  private val authenticator = FakeAppLockAuthenticator()
  private val location = FakeLocationProvider()

  @Test
  fun `Enabling after a successful unlock`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel =
      SecuritySettingsViewModel(
        preferences,
        authenticator,
        location,
        viewModelContext = unconfinedViewModelContext,
      )

    viewModel.state.test {
      val initial = awaitItem()
      assertThat(initial).all {
        prop(SecuritySettingsState::isScreenLockAvailable).isTrue()
        prop(SecuritySettingsState::appLockEnabled).prop(BooleanPreference::value).isFalse()
        prop(SecuritySettingsState::appLockTimeout).prop(ListPreference<*>::enabled).isFalse()
      }

      authenticator.result = true
      initial.appLockEnabled.onChange(true)
      assertThat(awaitItem()).all {
        prop(SecuritySettingsState::appLockEnabled).prop(BooleanPreference::value).isTrue()
        prop(SecuritySettingsState::appLockTimeout).prop(ListPreference<*>::enabled).isTrue()
      }
      assertThat(authenticator.calls).isEqualTo(1)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Not enabled if the unlock fails`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel =
      SecuritySettingsViewModel(
        preferences,
        authenticator,
        location,
        viewModelContext = unconfinedViewModelContext,
      )

    viewModel.state.test {
      authenticator.result = false
      awaitItem().appLockEnabled.onChange(true)
      expectNoEvents()
      assertThat(preferences.appLockEnabled.get()).isFalse()
      assertThat(authenticator.calls).isEqualTo(1)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Disabling doesn't need an unlock`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    preferences.appLockEnabled.set(true)
    val viewModel =
      SecuritySettingsViewModel(
        preferences,
        authenticator,
        location,
        viewModelContext = unconfinedViewModelContext,
      )

    viewModel.state.test {
      awaitItem().appLockEnabled.onChange(false)
      assertThat(awaitItem().appLockEnabled.value).isFalse()
      assertThat(authenticator.calls).isEqualTo(0)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Toggle disabled without a screen lock`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    authenticator.available = false
    val viewModel =
      SecuritySettingsViewModel(
        preferences,
        authenticator,
        location,
        viewModelContext = unconfinedViewModelContext,
      )

    viewModel.state.test {
      assertThat(awaitItem()).all {
        prop(SecuritySettingsState::isScreenLockAvailable).isFalse()
        prop(SecuritySettingsState::appLockEnabled).prop(BooleanPreference::enabled).isFalse()
      }

      authenticator.available = true
      viewModel.refreshAvailability()
      assertThat(awaitItem()).all {
        prop(SecuritySettingsState::isScreenLockAvailable).isTrue()
        prop(SecuritySettingsState::appLockEnabled).prop(BooleanPreference::enabled).isTrue()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Can still turn off without a screen lock`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    preferences.appLockEnabled.set(true)
    authenticator.available = false
    val viewModel =
      SecuritySettingsViewModel(
        preferences,
        authenticator,
        location,
        viewModelContext = unconfinedViewModelContext,
      )

    viewModel.state.test {
      assertThat(awaitItem().appLockEnabled.enabled).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Timeout changes`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel =
      SecuritySettingsViewModel(
        preferences,
        authenticator,
        location,
        viewModelContext = unconfinedViewModelContext,
      )

    viewModel.state.test {
      awaitItem().appLockTimeout.onChange(Minutes5)
      assertThat(awaitItem().appLockTimeout.value).isEqualTo(Minutes5)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Address lookups off by default and can be turned on`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    val viewModel =
      SecuritySettingsViewModel(
        preferences,
        authenticator,
        location,
        viewModelContext = unconfinedViewModelContext,
      )

    viewModel.state.test {
      val initial = awaitItem().lookUpPayeeAddresses
      assertThat(initial.value).isFalse()
      initial.onChange(true)
      assertThat(awaitItem().lookUpPayeeAddresses.value).isTrue()
      assertThat(preferences.lookUpPayeeAddresses.get()).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Hide preview forced on by the app lock`() = runTest {
    val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
    preferences.hidePreviewInAppSwitcher.set(false)
    val viewModel =
      SecuritySettingsViewModel(
        preferences,
        authenticator,
        location,
        viewModelContext = unconfinedViewModelContext,
      )

    viewModel.state.test {
      assertThat(awaitItem().hidePreviewInAppSwitcher).all {
        prop(BooleanPreference::value).isFalse()
        prop(BooleanPreference::enabled).isTrue()
      }

      preferences.appLockEnabled.set(true)
      assertThat(awaitItem().hidePreviewInAppSwitcher).all {
        prop(BooleanPreference::value).isTrue()
        prop(BooleanPreference::enabled).isFalse()
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Auto-saving locations is only enabled with permission, checked again on refresh`() =
    runTest {
      val preferences = SecurityPreferencesImpl(buildPreferences(unconfinedDispatcher))
      val viewModel =
        SecuritySettingsViewModel(
          preferences,
          authenticator,
          location,
          viewModelContext = unconfinedViewModelContext,
        )

      viewModel.state.test {
        assertThat(awaitItem()).all {
          prop(SecuritySettingsState::hasLocationPermission).isFalse()
          prop(SecuritySettingsState::autoSavePayeeLocations)
            .prop(BooleanPreference::enabled)
            .isFalse()
        }

        location.hasPermission = true
        viewModel.refreshAvailability()
        assertThat(awaitItem()).all {
          prop(SecuritySettingsState::hasLocationPermission).isTrue()
          prop(SecuritySettingsState::autoSavePayeeLocations)
            .prop(BooleanPreference::enabled)
            .isTrue()
        }
        cancelAndIgnoreRemainingEvents()
      }
    }

  private class FakeLocationProvider : LocationProvider {
    var hasPermission = false

    override val isSupported = true

    override fun hasPermission() = hasPermission

    override suspend fun currentLocation(): Coordinates? = null
  }

  private class FakeAppLockAuthenticator : AppLockAuthenticator {
    var available = true
    var result = false
    var calls = 0

    override fun isAvailable(): Boolean = available

    override suspend fun authenticate(): Boolean {
      calls++
      return result
    }
  }
}
