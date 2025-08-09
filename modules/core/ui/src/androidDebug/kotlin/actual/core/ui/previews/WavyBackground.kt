package actual.core.ui.previews

import actual.core.model.ColorSchemeType
import actual.core.ui.SingleScreenPreview
import actual.core.ui.WavyBackground
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun BackgroundImage(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = WavyBackground(
  schemeType = type,
)

@SingleScreenPreview
@Composable
private fun PreviewScaled(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = WavyBackground(
  schemeType = type,
)
