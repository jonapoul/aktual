package aktual.app.android

import aktual.app.nav.AktualAppContent
import aktual.app.nav.AppLockContent
import aktual.app.nav.AppLockManager
import aktual.app.nav.AppLockPrompt
import aktual.app.nav.rememberAppLockPrompt
import aktual.app.nav.rememberBackStack
import aktual.core.ui.LoadingScreenIfNull
import aktual.di.AppScope
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlinx.coroutines.launch

@ActivityKey
@ContributesIntoMap(AppScope::class, binding<Activity>())
class AktualActivity(
  override val defaultViewModelProviderFactory: MetroViewModelFactory,
  private val appLockManager: AppLockManager,
  private val appLockAuthenticator: AndroidAppLockAuthenticator,
) : ComponentActivity() {
  private val viewModel by viewModels<AktualActivityViewModel>()

  private val authenticator = BiometricAuthenticator(activity = this, onResult = ::onAuthResult)

  override fun onStart() {
    super.onStart()
    appLockManager.onForeground()
  }

  override fun onStop() {
    super.onStop()
    if (!isChangingConfigurations) appLockManager.onBackground()
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.onDestroy()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    installSplashScreen().setKeepOnScreenCondition { appLockManager.state.value == Unknown }

    val transparent = Color.Transparent.value.toInt()
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(transparent, transparent),
      navigationBarStyle = SystemBarStyle.auto(transparent, transparent),
    )

    // toggle FLAG_SECURE so the app's contents are hidden in the recents switcher (and screenshots
    // are blocked) according to the user's preferences. The flow seeds with true, so we're
    // secure-by-default until DataStore reports otherwise
    lifecycleScope.launch {
      repeatOnLifecycle(STARTED) {
        viewModel.secureWindow.collect { secure ->
          if (secure) window.addFlags(FLAG_SECURE) else window.clearFlags(FLAG_SECURE)
        }
      }
    }

    setContent {
      Content(
        viewModel = viewModel,
        appLockManager = appLockManager,
        appLockAuthenticator = appLockAuthenticator,
        onUnlock = ::unlock,
      )
    }
  }

  private fun unlock(prompt: AppLockPrompt) {
    when (authenticator.canAuthenticate()) {
      Available -> {
        appLockManager.onAuthenticationStarted()
        authenticator.authenticate(prompt.title, prompt.subtitle)
      }

      Unavailable -> {
        onAuthResult(Unavailable)
      }
    }
  }

  private fun onAuthResult(result: AuthResult) {
    // A request from settings, not the lock screen
    if (appLockAuthenticator.onResult(success = result == Success)) {
      appLockManager.onAuthenticationFailed()
      return
    }

    when (result) {
      Success -> appLockManager.onAuthenticated()
      Cancelled -> appLockManager.onAuthenticationFailed()
      // Never leave the user stuck behind a lock they can't open
      Unavailable -> appLockManager.onUnavailable()
      is Failed -> appLockManager.onAuthenticationFailed()
    }
  }
}

@Composable
@Suppress("ViewModelForwarding")
@SuppressLint("ComposeViewModelForwarding")
private fun Content(
  viewModel: AktualActivityViewModel,
  appLockManager: AppLockManager,
  appLockAuthenticator: AndroidAppLockAuthenticator,
  onUnlock: (AppLockPrompt) -> Unit,
) {
  val prompt = rememberAppLockPrompt()
  val currentOnUnlock by rememberUpdatedState(onUnlock)
  LaunchedEffect(appLockAuthenticator, prompt) {
    appLockAuthenticator.requests.collect { currentOnUnlock(prompt) }
  }

  val lockState by appLockManager.state.collectAsStateWithLifecycle()
  val isUnavailable by appLockManager.isUnavailable.collectAsStateWithLifecycle()
  when (lockState) {
    // The splash screen stays up until we know
    Unknown -> {}

    Locked -> {
      AppLockContent(
        viewModel = viewModel,
        isUnavailable = isUnavailable,
        onUnlock = onUnlock,
        onDisable = appLockManager::disable,
      )
    }

    Unlocked -> {
      UnlockedContent(viewModel)
    }
  }
}

@Composable
@Suppress("ViewModelForwarding")
@SuppressLint("ComposeViewModelForwarding")
private fun UnlockedContent(viewModel: AktualActivityViewModel) {
  val viewModelFactory by remember {
    viewModel.runLevels.viewModelFactory()
  }
    .collectAsState(initial = null)

  LoadingScreenIfNull(viewModelFactory) { vmf ->
    CompositionLocalProvider(LocalMetroViewModelFactory provides vmf) {
      val backStack = rememberBackStack(viewModel) ?: return@CompositionLocalProvider
      AktualAppContent(viewModel, backStack)
    }
  }
}
