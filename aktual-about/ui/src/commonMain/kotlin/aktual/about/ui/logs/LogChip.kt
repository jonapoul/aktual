package aktual.about.ui.logs

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColoredBooleanParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun LogChip(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentDescription: String? = null,
  trailingIcon: ImageVector? = null,
  role: Role? = null,
) {
  val background = if (isSelected) colors.pillBackgroundSelected else colors.pillBackground
  val border = if (isSelected) colors.pillBackgroundSelected else colors.pillBorder
  Box(
    modifier =
      modifier
        .semantics {
          if (role == null) selected = isSelected
          if (contentDescription != null) this.contentDescription = contentDescription
        }
        .clip(ChipShape)
        .background(background, ChipShape)
        .border(1.dp, border, ChipShape)
        .clickable(role = role, onClick = onClick)
        .heightIn(min = 28.dp)
        .padding(horizontal = 10.dp, vertical = 4.dp),
    contentAlignment = Center,
  ) {
    val color = if (isSelected) colors.pillTextSelected else colors.pillText
    Row(verticalAlignment = CenterVertically) {
      Text(
        text = text,
        style = typography.labelLarge,
        color = color,
        maxLines = 1,
        overflow = Ellipsis,
      )

      if (trailingIcon != null) {
        Icon(
          modifier = Modifier.size(18.dp),
          imageVector = trailingIcon,
          contentDescription = null,
          tint = color,
        )
      }
    }
  }
}

private val ChipShape = RoundedCornerShape(16.dp)

@Preview
@Composable
private fun PreviewLogChip(
  @PreviewParameter(ColoredBooleanParameters::class) params: ColoredParams<Boolean>
) =
  PreviewWithColoredParams(params) {
    LogChip(
      text = "Something",
      isSelected = this,
      onClick = {},
    )
  }
