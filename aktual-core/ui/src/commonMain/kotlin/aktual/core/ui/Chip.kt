package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.icons.Delete
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ChipColors(
  val background: Color,
  val content: Color,
  val border: Color = Unspecified,
)

object ChipDefaults {
  val Radius = 8.dp
  val Shape = RoundedCornerShape(Radius)
  val ContentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
  val Spacing = 4.dp
  val IconSize = 16.dp

  @Composable
  @ReadOnlyComposable
  fun colors(
    background: Color = colors.pillBackground,
    content: Color = colors.pillText,
    border: Color = Unspecified,
  ) = ChipColors(background, content, border)

  @Composable
  @ReadOnlyComposable
  fun selectableColors(isSelected: Boolean): ChipColors =
    with(colors) {
      if (isSelected) {
        ChipColors(
          background = pillBackgroundSelected,
          content = pillTextSelected,
          border = pillBackgroundSelected,
        )
      } else {
        ChipColors(background = pillBackground, content = pillText, border = pillBorder)
      }
    }
}

@Composable
fun Chip(
  text: String,
  modifier: Modifier = Modifier,
  colors: ChipColors = ChipDefaults.colors(),
  textStyle: TextStyle = typography.labelLarge,
  trailingIcon: ImageVector? = null,
  trailingIconDescription: String? = null,
  onClick: (() -> Unit)? = null,
  role: Role? = null,
) {
  Chip(
    modifier = modifier,
    colors = colors,
    textStyle = textStyle,
    onClick = onClick,
    role = role,
  ) {
    Text(
      modifier = Modifier.weight(1f, fill = false),
      text = text,
      maxLines = 1,
      overflow = Ellipsis,
    )

    if (trailingIcon != null) {
      Icon(
        modifier = Modifier.size(ChipDefaults.IconSize),
        imageVector = trailingIcon,
        contentDescription = trailingIconDescription,
      )
    }
  }
}

@Composable
fun Chip(
  modifier: Modifier = Modifier,
  colors: ChipColors = ChipDefaults.colors(),
  textStyle: TextStyle = typography.labelLarge,
  spacing: Dp = ChipDefaults.Spacing,
  onClick: (() -> Unit)? = null,
  role: Role? = null,
  content: @Composable RowScope.() -> Unit,
) {
  Row(
    modifier =
      modifier
        .clip(ChipDefaults.Shape)
        .background(colors.background, ChipDefaults.Shape)
        .then(
          if (colors.border.isSpecified) {
            Modifier.border(1.dp, colors.border, ChipDefaults.Shape)
          } else {
            Modifier
          },
        )
        .then(if (onClick == null) Modifier else Modifier.clickable(role = role, onClick = onClick))
        .padding(ChipDefaults.ContentPadding),
    horizontalArrangement = Arrangement.spacedBy(spacing),
    verticalAlignment = CenterVertically,
  ) {
    CompositionLocalProvider(
      LocalContentColor provides colors.content,
      LocalTextStyle provides textStyle.copy(color = colors.content),
    ) {
      content()
    }
  }
}

@Preview
@Composable
private fun PreviewChip(@PreviewParameter(ChipProvider::class) params: ColoredParams<String>) =
  PreviewWithColoredParams(params) {
    val text = this
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Chip(text = text)
      Chip(
        text = text,
        trailingIcon = AktualIcons.Delete,
        trailingIconDescription = "Remove",
        onClick = {},
      )
      Chip(
        text = text,
        colors = ChipDefaults.selectableColors(isSelected = false),
        onClick = {},
      )
      Chip(
        text = text,
        colors = ChipDefaults.selectableColors(isSelected = true),
        trailingIcon = AktualIcons.CheveronDown,
        onClick = {},
      )
    }
  }

private class ChipProvider :
  ColoredParameterProvider<String>(
    "Tesco",
    "A deliberately long payee name to check how the chip truncates its text",
  )
