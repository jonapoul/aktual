package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
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

// Shown in success colours, for things that finished as asked
suspend fun SnackbarHostState.showSuccessSnackbar(message: String): SnackbarResult =
  showSnackbar(SeveritySnackbarVisuals(message, Success))

// Shown in error colours, as upstream's error notifications
suspend fun SnackbarHostState.showErrorSnackbar(message: String): SnackbarResult =
  showSnackbar(SeveritySnackbarVisuals(message, Danger))

// Shown in warning colours, for things that partly worked or need the user's attention
suspend fun SnackbarHostState.showWarningSnackbar(message: String): SnackbarResult =
  showSnackbar(SeveritySnackbarVisuals(message, Warning))

private enum class Severity {
  Success,
  Danger,
  Warning,
}

private class SeveritySnackbarVisuals(
  override val message: String,
  val severity: Severity,
) : SnackbarVisuals {
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
    snackbar = ::AktualSnackbar,
  )

// Renders snackbars from showSuccessSnackbar, showErrorSnackbar and showWarningSnackbar in their
// colours
@Composable
fun AktualSnackbar(data: SnackbarData, modifier: Modifier = Modifier) {
  val visuals = data.visuals
  if (visuals !is SeveritySnackbarVisuals) {
    Snackbar(data, modifier)
    return
  }

  val (border, background, content) =
    with(colors) {
      when (visuals.severity) {
        Success ->
          Triple(noticeBorder, noticeBackground, legibleOn(noticeBackground, noticeTextDark))
        Danger -> Triple(errorBorder, errorBackground, errorTextDark)
        Warning -> Triple(warningBorder, warningBackground, warningTextDark)
      }
    }
  // Material's Snackbar(snackbarData) pads outside the given modifier, which would leave the border
  // floating 12dp clear of the surface, so pad first and draw the border on the surface itself
  Snackbar(
    modifier = modifier.padding(Dimens.VeryLarge).border(Hairline, border, SnackbarDefaults.shape),
    containerColor = background,
    contentColor = content,
  ) {
    Text(visuals.message)
  }
}
