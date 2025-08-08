package actual.core.ui.previews

import actual.core.ui.BareTextButton
import actual.core.ui.NormalTextButton
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.PrimaryTextButton
import actual.core.ui.PrimaryTextButtonWithLoading
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun Bare(
  @PreviewParameter(BooleanParameters::class) enabled: Boolean,
) = PreviewThemedColumn {
  BareTextButton(
    text = "Bare",
    isEnabled = enabled,
    onClick = {},
  )
}

@Preview
@Composable
private fun Primary(
  @PreviewParameter(BooleanParameters::class) enabled: Boolean,
) = PreviewThemedColumn {
  PrimaryTextButton(
    text = "Primary",
    isEnabled = enabled,
    onClick = {},
  )
}

@Preview
@Composable
private fun Normal(
  @PreviewParameter(BooleanParameters::class) enabled: Boolean,
) = PreviewThemedColumn {
  NormalTextButton(
    text = "Normal",
    isEnabled = enabled,
    onClick = {},
  )
}

@Preview
@Composable
private fun PrimaryWithLoadingNotLoading(
  @PreviewParameter(BooleanParameters::class) isLoading: Boolean,
) = PreviewThemedColumn {
  PrimaryTextButtonWithLoading(
    text = "OK",
    isLoading = isLoading,
    onClick = {},
  )
}
