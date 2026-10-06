package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.TextAlign
import aktual.core.icons.material.FormatAlignCenter
import aktual.core.icons.material.FormatAlignLeft
import aktual.core.icons.material.FormatAlignRight
import aktual.core.icons.material.FormatBold
import aktual.core.icons.material.FormatItalic
import aktual.core.icons.material.FormatListBulleted
import aktual.core.icons.material.Link
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Title
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.BareIconButton
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment.Companion.CenterVertically
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
  onFormat: (MarkdownFormat) -> Unit,
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

    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = CenterVertically,
    ) {
      AlignButtons(align, onAlign)
      if (mode == Write) {
        VerticalDivider(modifier = Modifier.height(24.dp))
        FormatButtons(onFormat)
      }
    }
  }

@Composable
private fun FormatButtons(onFormat: (MarkdownFormat) -> Unit) {
  FormatButton(MaterialIcons.FormatBold, Strings.reportsTextFormatBold) { onFormat(Bold) }
  FormatButton(MaterialIcons.FormatItalic, Strings.reportsTextFormatItalic) { onFormat(Italic) }
  FormatButton(MaterialIcons.Title, Strings.reportsTextFormatHeading) { onFormat(Heading) }
  FormatButton(MaterialIcons.FormatListBulleted, Strings.reportsTextFormatBullet) {
    onFormat(Bullet)
  }
  FormatButton(MaterialIcons.Link, Strings.reportsTextFormatLink) { onFormat(MarkdownFormat.Link) }
}

@Composable
@NonRestartableComposable
private fun FormatButton(icon: ImageVector, description: String, onClick: () -> Unit) =
  BareIconButton(imageVector = icon, contentDescription = description, onClick = onClick)

@Composable
private fun EditorMode.string() =
  when (this) {
    Write -> Strings.reportsTextWrite
    Rendered -> Strings.reportsTextPreview
  }

@Composable
private fun AlignButtons(align: TextAlign, onAlign: (TextAlign) -> Unit) {
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
    TextEditorToolbar(
      mode = this,
      onMode = {},
      align = Left,
      onAlign = {},
      onFormat = {},
    )
  }

private class TextEditorToolbarProvider : ColoredParameterProvider<EditorMode>(Write, Rendered)
