package aktual.app.nav

import aktual.di.AppCoroutineScope
import aktual.di.AppScope
import aktual.log
import aktual.prefs.SecurityPreferences
import aktual.prefs.asStateFlow
import androidx.compose.runtime.Stable
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppLockState {
  Unknown,
  Locked,
  Unlocked,
}

@Inject
@Stable
@SingleIn(AppScope::class)
class AppLockManager(
  private val preferences: SecurityPreferences,
  private val clock: Clock,
  private val scope: AppCoroutineScope,
) {
  private val mutableState = MutableStateFlow(AppLockState.Unknown)
  val state: StateFlow<AppLockState> = mutableState.asStateFlow()

  // Set when the device no longer has a screen lock, so we show why the lock is turning off
  private val mutableIsUnavailable = MutableStateFlow(false)
  val isUnavailable: StateFlow<Boolean> = mutableIsUnavailable.asStateFlow()

  private val isEnabled = preferences.appLockEnabled.asStateFlow(scope)
  private val timeout = preferences.appLockTimeout.asStateFlow(scope)

  private var backgroundedAt: Instant? = null

  // The device credential screen is a separate activity, so showing the prompt sends us to the
  // background. Ignore lifecycle events until it's dismissed
  private var isAuthenticating = false

  init {
    scope.launch {
      preferences.appLockEnabled.asFlow().collect { enabled ->
        if (!enabled) isAuthenticating = false
        mutableState.update { state ->
          when {
            !enabled -> Unlocked
            state == Unknown -> Locked
            else -> state
          }.also { newValue ->
            if (newValue != state) {
              log.d { "AppLockManager state $state -> $newValue" }
            }
          }
        }
      }
    }
  }

  fun onBackground() {
    log.d { "onBackground" }
    if (isAuthenticating) return
    backgroundedAt = clock.now()
  }

  fun onForeground() {
    log.d { "onForeground" }
    if (isAuthenticating) return
    val since = backgroundedAt ?: return
    backgroundedAt = null
    val elapsed = clock.now() - since
    // Negative if the system clock was moved back, so don't trust it
    if (isEnabled.value && (elapsed.isNegative() || elapsed >= timeout.value.duration)) {
      log.d { "Locking after $elapsed in background" }
      mutableState.update { Locked }
    }
  }

  fun onAuthenticationStarted() {
    log.d { "onAuthenticationStarted" }
    isAuthenticating = true
  }

  fun onAuthenticated() {
    log.d { "onAuthenticated" }
    isAuthenticating = false
    mutableState.update { Unlocked }
  }

  fun onAuthenticationFailed() {
    log.d { "onAuthenticationFailed" }
    isAuthenticating = false
  }

  fun onUnavailable() {
    log.w { "onUnavailable" }
    isAuthenticating = false
    mutableIsUnavailable.update { true }
  }

  // Called once the user has seen why the lock is turning off
  fun disable() {
    log.w { "Disabling app lock" }
    isAuthenticating = false
    mutableIsUnavailable.update { false }
    scope.launch { preferences.appLockEnabled.set(false) }
  }
}
