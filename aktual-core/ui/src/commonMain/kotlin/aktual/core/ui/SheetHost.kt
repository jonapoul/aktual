package aktual.core.ui

import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.launch

/**
 * One bottom sheet for every [HostedSheet] inside it, so swapping one for another changes the page
 * rather than opening a new sheet. Give the first page [SheetHostState.dismiss] as its `onDismiss`.
 * A page given anything else is a sub-page, with a back button that calls its `onDismiss`.
 */
@Composable
fun SheetHost(
  onDismiss: () -> Unit,
  content: @Composable (host: SheetHostState) -> Unit,
) {
  val sheetState =
    rememberBottomSheetState(initialValue = Hidden, enabledValues = setOf(Hidden, Expanded))
  val scope = rememberCoroutineScope()
  val latestOnDismiss by rememberUpdatedState(onDismiss)
  val host = remember {
    SheetHostState { then ->
      scope
        .launch { sheetState.hide() }
        .invokeOnCompletion {
          then()
          latestOnDismiss()
        }
    }
  }

  AktualModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    onBack = host.onBack,
  ) {
    CompositionLocalProvider(LocalSheetHost provides host) { content(host) }
  }
}

@Stable
class SheetHostState internal constructor(private val hide: (then: () -> Unit) -> Unit) {
  /** Hides the sheet, then calls the host's `onDismiss` */
  val dismiss: () -> Unit = { hide {} }

  internal var onBack by mutableStateOf<(() -> Unit)?>(null)

  /** Hides the sheet, then runs [then] and the host's `onDismiss` */
  fun dismissThen(then: () -> Unit) = hide(then)
}

/**
 * Lets a page close itself, running its argument first. [close] goes back on a sub-page, and hides
 * the sheet otherwise. [dismiss] always hides the whole sheet.
 */
@Stable
class HostedSheetScope
internal constructor(
  private val onClose: (then: () -> Unit) -> Unit,
  private val onDismiss: (then: () -> Unit) -> Unit,
) {
  fun close(then: () -> Unit = {}) = onClose(then)

  fun dismiss(then: () -> Unit = {}) = onDismiss(then)
}

/**
 * Shows [content] as a page of the enclosing [SheetHost], or in a sheet of its own when there's
 * none
 */
@Composable
fun HostedSheet(onDismiss: () -> Unit, content: @Composable HostedSheetScope.() -> Unit) {
  val host = LocalSheetHost.current
  if (host == null) {
    SheetHost(onDismiss = onDismiss) { own -> HostedSheet(onDismiss = own.dismiss, content) }
    return
  }

  val scope =
    if (onDismiss === host.dismiss) {
      HostedSheetScope(onClose = host::dismissThen, onDismiss = host::dismissThen)
    } else {
      BackHandler(onBack = onDismiss)
      DisposableEffect(host, onDismiss) {
        host.onBack = onDismiss
        onDispose { if (host.onBack === onDismiss) host.onBack = null }
      }
      val close = { then: () -> Unit ->
        then()
        onDismiss()
      }
      HostedSheetScope(onClose = close, onDismiss = host::dismissThen)
    }
  scope.content()
}

private val LocalSheetHost = staticCompositionLocalOf<SheetHostState?> { null }
