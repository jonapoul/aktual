package aktual.core.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** To help align the content of the PTR box with the content underneath a scaffold's top bar. */
@Composable
fun BlurredPullToRefreshBox(
  blurState: BlurredTopBarState,
  innerPadding: PaddingValues,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  contentAlignment: Alignment = Alignment.TopStart,
  content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
  val state = rememberPullToRefreshState()
  PullToRefreshBox(
    modifier = modifier.blurredTopBarContent(blurState, innerPadding),
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    state = state,
    contentAlignment = contentAlignment,
    enabled = enabled,
    indicator = {
      PullToRefreshDefaults.Indicator(
        modifier =
          Modifier.align(Alignment.TopCenter).offset(y = innerPadding.calculateTopPadding()),
        isRefreshing = isRefreshing,
        state = state,
      )
    },
    content = {
      val contentPadding = blurredTopBarContentPadding(blurState, innerPadding)
      content(contentPadding)
    },
  )
}
