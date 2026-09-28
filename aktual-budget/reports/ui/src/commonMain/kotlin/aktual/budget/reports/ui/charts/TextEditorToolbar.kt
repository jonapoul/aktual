package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.TextAlign
import aktual.core.icons.material.FormatAlignCenter
import aktual.core.icons.material.FormatAlignLeft
import aktual.core.icons.material.FormatAlignRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.BareIconButton
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

private val EditorModes = persistentListOf(EditorMode.Write, EditorMode.Rendered)

@Composable
internal fun TextEditorToolbar(
  mode: EditorMode,
  onMode: (EditorMode) -> Unit,
  align: TextAlign,
  onAlign: (TextAlign) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      selected = mode,
      options = EditorModes,
      onSelect = onMode,
      string = { it.string() },
    )

    AlignButtons(align, onAlign)
  }

@Composable
private fun EditorMode.string() =
  when (this) {
    Write -> Strings.reportsTextWrite
    Rendered -> Strings.reportsTextPreview
  }

@Composable
private fun AlignButtons(align: TextAlign, onAlign: (TextAlign) -> Unit) {
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
  @PreviewParameter(TextEditorToolbarProvider::class) params: ColoredParams<EditorMode>
) =
  PreviewWithColoredParams(params) {
    TextEditorToolbar(mode = this, onMode = {}, align = TextAlign.Left, onAlign = {})
  }

private class TextEditorToolbarProvider : ColoredParameterProvider<EditorMode>(Write, Rendered)
