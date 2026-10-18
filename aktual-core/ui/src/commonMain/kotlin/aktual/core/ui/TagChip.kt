package aktual.core.ui

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp

@Composable
fun TagChip(
  text: String,
  color: Color?,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) = TagChip(AnnotatedString("#$text"), color, modifier, onClick)

// [text] includes the leading '#'. Upstream falls back to the theme's note-tag colors when a tag
// has no explicit color
@Composable
fun TagChip(
  text: AnnotatedString,
  color: Color?,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) =
  Text(
    text = text,
    modifier =
      modifier
        .clip(TagChipShape)
        .background(color ?: colors.noteTagBackground, TagChipShape)
        .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
        .padding(TagChipPadding),
    style = typography.bodyMedium,
    fontWeight = SemiBold,
    color = color?.contrastingTextColor() ?: colors.noteTagText,
    maxLines = 1,
    overflow = Ellipsis,
  )

val TagChipShape = RoundedCornerShape(size = 16.dp)
private val TagChipPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
