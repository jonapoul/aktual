package actual.preview

import actual.core.ui.PreviewThemedColumn
import actual.settings.ui.items.ShowBottomBarPreferenceItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewChecked() = PreviewThemedColumn {
  ShowBottomBarPreferenceItem(
    value = true,
    onChange = {},
  )
}

@Preview
@Composable
private fun PreviewUnchecked() = PreviewThemedColumn {
  ShowBottomBarPreferenceItem(
    value = false,
    onChange = {},
  )
}
