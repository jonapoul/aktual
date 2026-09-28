package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.TextAlign
import aktual.core.icons.material.FormatAlignCenter
import aktual.core.icons.material.FormatAlignLeft
import aktual.core.icons.material.FormatAlignRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.BareIconButton
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun TextEditorToolbar(
  align: TextAlign,
  onAlign: (TextAlign) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    AlignButton(Left, MaterialIcons.FormatAlignLeft, Strings.reportsTextAlignLeft, align, onAlign)
    AlignButton(
      Center,
      MaterialIcons.FormatAlignCenter,
      Strings.reportsTextAlignCenter,
      align,
      onAlign,
    )
    AlignButton(
      Right,
      MaterialIcons.FormatAlignRight,
      Strings.reportsTextAlignRight,
      align,
      onAlign,
    )
  }
}

@Composable
private fun AlignButton(
  option: TextAlign,
  icon: ImageVector,
  description: String,
  selected: TextAlign,
  onAlign: (TextAlign) -> Unit,
) =
  BareIconButton(
    imageVector = icon,
    contentDescription = description,
    colors =
      if (option == selected) IconButtonColorProvider.Primary else IconButtonColorProvider.Bare,
    onClick = { onAlign(option) },
  )

@Preview
@Composable
private fun PreviewTextEditorToolbar(
  @PreviewParameter(TextEditorToolbarProvider::class) params: ColoredParams<TextAlign>
) = PreviewWithColoredParams(params) { TextEditorToolbar(align = this, onAlign = {}) }

private class TextEditorToolbarProvider :
  ColoredParameterProvider<TextAlign>(TextAlign.Left, TextAlign.Center)
