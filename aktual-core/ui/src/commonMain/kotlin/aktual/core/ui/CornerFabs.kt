package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.SearchAlternate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Room at the end of scrolling content so the last item can scroll out from under a FAB
val FabClearance = 72.dp

/**
 * Places FABs in the bottom corners of the screen, above the nav bar. Usually [SearchFab] at the
 * start and the screen's main [AktualFab] (e.g. create) at the end, both passing the same
 * `isExpanded` from `isScrollingUp()`. End the scrolling content with [FabClearance].
 */
@Composable
fun BoxScope.CornerFabs(
  modifier: Modifier = Modifier,
  start: @Composable BoxScope.() -> Unit = {},
  end: @Composable BoxScope.() -> Unit = {},
) {
  Box(
    modifier =
      modifier
        .align(BottomCenter)
        .fillMaxWidth()
        .padding(
          start = Dimens.Huge,
          end = Dimens.Huge,
          bottom = Dimens.Huge + bottomNavBarPadding() + LocalBottomSpacing.current,
        )
  ) {
    Box(modifier = Modifier.align(BottomStart), content = start)
    Box(modifier = Modifier.align(BottomEnd), content = end)
  }
}

@Composable
fun SearchFab(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isVisible: Boolean = true,
  isExpanded: Boolean = true,
) {
  AktualFab(
    modifier = modifier,
    text = text,
    icon = AktualIcons.SearchAlternate,
    kind = Normal,
    isVisible = isVisible,
    isExpanded = isExpanded,
    onClick = onClick,
  )
}
