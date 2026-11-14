package aktual.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

/**
 * One bottom sheet for every [HostedSheet] inside it, so swapping one for another changes the page
 * rather than opening a new sheet. Give the first page [SheetHostState.dismiss] as its `onDismiss`.
 * A page given anything else is a sub-page, with a back button that calls its `onDismiss`. The
 * sheet keeps the height of its tallest page so far, so it doesn't shrink when swapping pages. New
 * pages slide in from the end, and going back slides them out again.
 */
@Composable
fun SheetHost(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
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

  val onBack = host.onBack
  AktualModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    onBack = onBack,
  ) {
    BackHandler(enabled = onBack != null) { onBack?.invoke() }
    var tallestPage by remember { mutableIntStateOf(0) }
    val minHeight = with(LocalDensity.current) { tallestPage.toDp() }
    Column(
      modifier =
        Modifier.heightIn(min = minHeight)
          .onSizeChanged { size -> if (size.height > tallestPage) tallestPage = size.height }
          .slidePages(host),
    ) {
      CompositionLocalProvider(LocalSheetHost provides host) { content(host) }
    }
  }
}

@Stable
class SheetHostState internal constructor(private val hide: (then: () -> Unit) -> Unit) {
  /** Hides the sheet, then calls the host's `onDismiss` */
  val dismiss: () -> Unit = { hide {} }

  // Shown pages by key. The deepest one is on top and gets the back button, since a page can open
  // another inside itself
  private val pages = mutableStateMapOf<Any, Page>()
  private var nextOrder = 0

  internal val topPage: Page?
    get() = pages.values.maxWithOrNull(compareBy(Page::depth, Page::order))

  internal val onBack: (() -> Unit)?
    get() = topPage?.onBack?.let { onBack -> { goBack(onBack) } }

  // Set when a page closes, so the next page change slides the other way
  internal var isGoingBack = false

  internal fun goBack(then: () -> Unit) {
    isGoingBack = true
    then()
  }

  internal fun register(key: Any, depth: Int, onBack: (() -> Unit)?) {
    pages[key] = Page(key, depth, nextOrder++, onBack)
  }

  internal fun unregister(key: Any) {
    pages.remove(key)
  }

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

  val parentDepth = LocalSheetDepth.current
  val isSubPage = onDismiss !== host.dismiss
  val depth = if (isSubPage) parentDepth + 1 else parentDepth
  val latestOnDismiss by rememberUpdatedState(onDismiss)
  val key = remember { Any() }
  DisposableEffect(host, key, depth, isSubPage) {
    val onBack = { latestOnDismiss() }
    host.register(key, depth, onBack = onBack.takeIf { isSubPage })
    onDispose { host.unregister(key) }
  }

  val scope =
    if (isSubPage) {
      val close = { then: () -> Unit ->
        host.goBack {
          then()
          onDismiss()
        }
      }
      HostedSheetScope(onClose = close, onDismiss = host::dismissThen)
    } else {
      HostedSheetScope(onClose = host::dismissThen, onDismiss = host::dismissThen)
    }
  CompositionLocalProvider(LocalSheetDepth provides depth) { scope.content() }
}

internal data class Page(val key: Any, val depth: Int, val order: Int, val onBack: (() -> Unit)?)

private val LocalSheetHost = staticCompositionLocalOf<SheetHostState?> { null }

private val LocalSheetDepth = staticCompositionLocalOf { 0 }
