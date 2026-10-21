package aktual.app.nav

import aktual.di.AppCoroutineScope
import aktual.di.AppScope
import aktual.prefs.SystemUiPreferences
import aktual.prefs.asStateFlow
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

enum class AppLockState {
  Unknown,
  Locked,
  Unlocked,
}

@Inject
@SingleIn(AppScope::class)
class AppLockManager(
  private val preferences: SystemUiPreferences,
  private val clock: Clock,
  scope: AppCoroutineScope,
) {
  private val mutableState = MutableStateFlow(AppLockState.Unknown)
  val state: StateFlow<AppLockState> = mutableState.asStateFlow()

  private val isEnabled = preferences.appLockEnabled.asStateFlow(scope)
  private val timeout = preferences.appLockTimeout.asStateFlow(scope)

  private var backgroundedAt: Instant? = null

  // The device credential screen is a separate activity, so showing the prompt sends us to the
  // background. Ignore lifecycle events until it's dismissed
  private var isAuthenticating = false

  init {
    scope.launch {
      preferences.appLockEnabled.asFlow().collect { enabled ->
        mutableState.update { state ->
          when {
            !enabled -> Unlocked
            state == Unknown -> Locked
            else -> state
          }
        }
      }
    }
  }

  fun onBackground() {
    if (isAuthenticating) return
    backgroundedAt = clock.now()
  }

  fun onForeground() {
    if (isAuthenticating) return
    val since = backgroundedAt ?: return
    backgroundedAt = null
    val elapsed = clock.now() - since
    if (isEnabled.value && elapsed >= timeout.value.duration) {
      logcat.d { "Locking after $elapsed in background" }
      mutableState.update { Locked }
    }
  }

  fun onAuthenticationStarted() {
    isAuthenticating = true
  }

  fun onAuthenticated() {
    isAuthenticating = false
    mutableState.update { Unlocked }
  }

  fun onAuthenticationFailed() {
    isAuthenticating = false
  }
}
