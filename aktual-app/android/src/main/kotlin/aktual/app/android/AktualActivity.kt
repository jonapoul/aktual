package aktual.app.android

import aktual.app.nav.AktualAppContent
import aktual.app.nav.AppLockContent
import aktual.app.nav.AppLockManager
import aktual.app.nav.AppLockPrompt
import aktual.app.nav.rememberAppLockPrompt
import aktual.app.nav.rememberBackStack
import aktual.core.model.ForegroundStateHolder
import aktual.core.openid.AndroidOpenIdRedirects
import aktual.core.ui.LoadingScreenIfNull
import aktual.core.ui.rememberLocalNetworkPermissionRequest
import aktual.di.AppScope
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ActivityKey
@ContributesIntoMap(AppScope::class, binding<Activity>())
class AktualActivity(
  override val defaultViewModelProviderFactory: MetroViewModelFactory,
  private val appLockManager: AppLockManager,
  private val appLockAuthenticator: AndroidAppLockAuthenticator,
  private val foregroundState: ForegroundStateHolder,
  private val openIdRedirects: AndroidOpenIdRedirects,
) : ComponentActivity() {
  private val viewModel by viewModels<AktualActivityViewModel>()

  private val authenticator = BiometricAuthenticator(activity = this, onResult = ::onAuthResult)

  override fun onStart() {
    super.onStart()
    appLockManager.onForeground()
    foregroundState.update { true }
  }

  override fun onStop() {
    super.onStop()
    if (!isChangingConfigurations) {
      appLockManager.onBackground()
      foregroundState.update { false }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.onDestroy()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    openIdRedirects.onIntent(intent)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState == null) openIdRedirects.onIntent(intent)
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

      Unavailable -> onAuthResult(Unavailable)
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
  // Locking takes the app content out of composition, so hold onto its saveable state (nested tab
  // stacks, selected tab, per-entry state) to restore it when unlocked again
  val stateHolder = rememberSaveableStateHolder()
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

    Unlocked ->
      stateHolder.SaveableStateProvider(UNLOCKED_CONTENT_KEY) { UnlockedContent(viewModel) }
  }
}

private const val UNLOCKED_CONTENT_KEY = "unlocked"

@Composable
@Suppress("ViewModelForwarding")
@SuppressLint("ComposeViewModelForwarding")
private fun UnlockedContent(viewModel: AktualActivityViewModel) {
  // Server requests wait for the answer, so they go through once it's given
  val requestLocalNetworkPermission =
    rememberLocalNetworkPermissionRequest(onResult = { viewModel.onLocalNetworkPermissionResult() })
  LaunchedEffect(Unit) {
    if (viewModel.needsLocalNetworkPermission()) requestLocalNetworkPermission()
  }
  // Leaving composition with the dialog up unregisters the launcher, so no result would arrive.
  // Not on a configuration change though, since the recreated launcher still gets the result
  val activity = LocalActivity.current
  DisposableEffect(activity) {
    onDispose {
      if (activity?.isChangingConfigurations != true) viewModel.onLocalNetworkPermissionResult()
    }
  }

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
