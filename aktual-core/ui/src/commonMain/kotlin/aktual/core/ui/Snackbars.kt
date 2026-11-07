package aktual.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Shows snackbars from a host above the screens, so one shown just before navigating away stays up
@Stable
class Snackbars
internal constructor(
  internal val hostState: SnackbarHostState,
  private val scope: CoroutineScope,
) {
  private val keyed = mutableMapOf<Any, Job>()

  // A snackbar shown with the same [key] as one still waiting or showing replaces it
  fun show(key: Any? = null, block: suspend SnackbarHostState.() -> Unit) {
    if (key == null) {
      scope.launch { hostState.block() }
      return
    }
    keyed.remove(key)?.cancel()
    val job = scope.launch { hostState.block() }
    keyed[key] = job
    job.invokeOnCompletion { if (keyed[key] === job) keyed.remove(key) }
  }

  fun show(key: Any? = null, message: String) = show(key) { showSnackbar(message) }
}

// Shown in error colours, as upstream's error notifications
suspend fun SnackbarHostState.showErrorSnackbar(message: String): SnackbarResult =
  showSnackbar(ErrorSnackbarVisuals(message))

private class ErrorSnackbarVisuals(override val message: String) : SnackbarVisuals {
  override val actionLabel: String? = null
  override val withDismissAction: Boolean = false
  override val duration: SnackbarDuration = Short
}

val LocalSnackbars = staticCompositionLocalOf<Snackbars> { error("No Snackbars provided") }

@Composable
fun rememberSnackbars(): Snackbars {
  val scope = rememberCoroutineScope()
  return remember(scope) { Snackbars(SnackbarHostState(), scope) }
}

@Composable
fun SnackbarsHost(snackbars: Snackbars, modifier: Modifier = Modifier) =
  SnackbarHost(
    hostState = snackbars.hostState,
    modifier = modifier.padding(bottom = LocalBottomSpacing.current + bottomNavBarPadding()),
  ) { data ->
    if (data.visuals is ErrorSnackbarVisuals) {
      val colors = AktualTheme.colors
      Snackbar(
        snackbarData = data,
        modifier = Modifier.border(Hairline, colors.errorBorder, SnackbarDefaults.shape),
        containerColor = colors.errorBackground,
        contentColor = colors.errorTextDark,
      )
    } else {
      Snackbar(data)
    }
  }
