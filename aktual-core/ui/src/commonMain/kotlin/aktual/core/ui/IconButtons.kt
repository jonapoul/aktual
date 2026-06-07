package aktual.core.ui

import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.Check
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp

@Composable
fun PrimaryIconButton(
  imageVector: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  enabled: Boolean = true,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  colors: IconButtonColorProvider = IconButtonColorProvider.Primary,
  content: @Composable () -> Unit = {
    DefaultIconButtonContent(imageVector, contentDescription, size)
  },
) {
  BasicIconButton(
    imageVector = imageVector,
    contentDescription = contentDescription,
    onClick = onClick,
    colors = colors,
    modifier = modifier,
    size = size,
    enabled = enabled,
    shape = shape,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun NormalIconButton(
  imageVector: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  isEnabled: Boolean = true,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  colors: IconButtonColorProvider = IconButtonColorProvider.Normal,
  content: @Composable () -> Unit = {
    DefaultIconButtonContent(imageVector, contentDescription, size)
  },
) {
  BasicIconButton(
    imageVector = imageVector,
    contentDescription = contentDescription,
    onClick = onClick,
    colors = colors,
    modifier = modifier,
    size = size,
    enabled = isEnabled,
    shape = shape,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun BareIconButton(
  imageVector: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  enabled: Boolean = true,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  colors: IconButtonColorProvider = IconButtonColorProvider.Bare,
  content: @Composable () -> Unit = {
    DefaultIconButtonContent(imageVector, contentDescription, size)
  },
) {
  BasicIconButton(
    imageVector = imageVector,
    contentDescription = contentDescription,
    onClick = onClick,
    colors = colors,
    modifier = modifier,
    size = size,
    enabled = enabled,
    shape = shape,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun BasicIconButton(
  imageVector: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  colors: IconButtonColorProvider,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  enabled: Boolean = true,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit = {
    DefaultIconButtonContent(imageVector, contentDescription, size)
  },
) {
  val isPressed by interactionSource.collectIsPressedAsState()
  val buttonColors = colors(AktualTheme.colors, isPressed)
  val background = if (enabled) buttonColors.containerColor else buttonColors.disabledContainerColor

  IconButton(
    modifier = modifier.background(background, shape),
    onClick = onClick,
    enabled = enabled,
    colors = buttonColors,
    content = content,
  )
}

@Composable
fun NavBackIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(modifier = modifier, onClick = onClick) {
    Icon(imageVector = MaterialIcons.ArrowBack, contentDescription = Strings.navBack)
  }
}

@Immutable
fun interface IconButtonColorProvider {
  @Composable operator fun invoke(colors: Colors, isPressed: Boolean): IconButtonColors

  companion object {
    val Bare = IconButtonColorProvider { theme, isPressed -> theme.bareIconButton(isPressed) }
    val Primary = IconButtonColorProvider { theme, isPressed -> theme.primaryIconButton(isPressed) }
    val Normal = IconButtonColorProvider { theme, isPressed -> theme.normalIconButton(isPressed) }

    val NormalRed = IconButtonColorProvider { theme, isPressed ->
      theme
        .normalIconButton(isPressed)
        .copy(
          containerColor = theme.errorBackground,
          disabledContainerColor = theme.errorBackground.disabled,
          contentColor = theme.errorText,
        )
    }
  }
}

@Composable
@Suppress("ComposeModifierMissing")
fun NavBackIconButton(onClick: () -> Unit) {
  NavBackIconButton(modifier = Modifier, onClick = onClick)
}

@Composable
private fun DefaultIconButtonContent(
  imageVector: ImageVector,
  contentDescription: String?,
  size: Dp? = null,
) {
  Icon(
    modifier = if (size == null) Modifier else Modifier.size(size),
    imageVector = imageVector,
    contentDescription = contentDescription,
  )
}

@Preview
@Composable
private fun PreviewBare(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    BareIconButton(imageVector = MaterialIcons.Check, contentDescription = "Cancel", onClick = {})
  }

@Preview
@Composable
private fun PreviewNormal(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    NormalIconButton(imageVector = MaterialIcons.Check, contentDescription = "Cancel", onClick = {})
  }

@Preview
@Composable
private fun PreviewPrimary(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    PrimaryIconButton(imageVector = MaterialIcons.Check, contentDescription = "OK", onClick = {})
  }
