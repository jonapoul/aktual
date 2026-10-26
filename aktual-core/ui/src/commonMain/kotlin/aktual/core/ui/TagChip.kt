package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis

@Composable
fun TagChip(
  text: String,
  color: Color?,
  modifier: Modifier = Modifier,
  size: ChipSize = Regular,
  onClick: (() -> Unit)? = null,
) = TagChip(AnnotatedString("#$text"), color, modifier, size, onClick)

// [text] includes the leading '#'. Upstream falls back to the theme's note-tag colors when a tag
// has no explicit color
@Composable
fun TagChip(
  text: AnnotatedString,
  color: Color?,
  modifier: Modifier = Modifier,
  size: ChipSize = Regular,
  onClick: (() -> Unit)? = null,
) {
  Chip(
    modifier = modifier,
    colors =
      ChipDefaults.colors(
        background = color ?: colors.noteTagBackground,
        content = color?.contrastingTextColor() ?: colors.noteTagText,
      ),
    size = size,
    textStyle = typography.bodyMedium.copy(fontWeight = SemiBold),
    onClick = onClick,
  ) {
    Text(text = text, maxLines = 1, overflow = Ellipsis)
  }
}
