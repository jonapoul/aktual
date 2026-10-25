package aktual.core.ui

import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
fun RemovableChip(
  text: String,
  removeDescription: String,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier =
      modifier
        .heightIn(min = 34.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(colors.pillBackground)
        .clickable(onClick = onRemove)
        .padding(horizontal = 12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f, fill = false),
      text = text,
      style = typography.bodyMedium,
      color = colors.pillText,
      maxLines = 1,
      overflow = Ellipsis,
    )
    Icon(
      modifier = Modifier.size(16.dp),
      imageVector = MaterialIcons.Clear,
      contentDescription = removeDescription,
      tint = colors.pillText,
    )
  }

@Preview
@Composable
private fun PreviewRemovableChip(
  @PreviewParameter(RemovableChipProvider::class) params: ColoredParams<String>,
) =
  PreviewWithColoredParams(params) {
    RemovableChip(text = this, removeDescription = "Remove", onRemove = {})
  }

private class RemovableChipProvider :
  ColoredParameterProvider<String>(
    "Tesco",
    "A deliberately long payee name to check how the chip truncates its text",
  )
