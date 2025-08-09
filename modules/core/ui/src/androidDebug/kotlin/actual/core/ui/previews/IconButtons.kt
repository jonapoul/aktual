package actual.core.ui.previews

import actual.core.model.ColorSchemeType
import actual.core.ui.BareIconButton
import actual.core.ui.NormalIconButton
import actual.core.ui.PreviewWithColorScheme
import actual.core.ui.PrimaryIconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun Bare(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  BareIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "Cancel",
    onClick = {},
  )
}

@Preview
@Composable
private fun Normal(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  NormalIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "Cancel",
    onClick = {},
  )
}

@Preview
@Composable
private fun Primary(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  PrimaryIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "OK",
    onClick = {},
  )
}
