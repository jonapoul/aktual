package actual.app.nav

import actual.core.model.PingState
import actual.core.ui.BottomBarState
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewConnected() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Success,
      budgetName = "My Budget",
    ),
  )
}

@Preview
@Composable
private fun PreviewDisconnected() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Failure,
      budgetName = null,
    ),
  )
}

@Preview
@Composable
private fun PreviewUnknown() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Unknown,
      budgetName = null,
    ),
  )
}
