package actual.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp

@Stable
@Composable
fun PrimaryActualIconButton(
  imageVector: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  enabled: Boolean = true,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit = { DefaultIconButtonContent(imageVector, contentDescription, size) },
) {
  BasicActualIconButton(
    imageVector = imageVector,
    contentDescription = contentDescription,
    onClick = onClick,
    colors = { scheme, isPressed -> scheme.primary(isPressed) },
    modifier = modifier,
    size = size,
    enabled = enabled,
    shape = shape,
    interactionSource = interactionSource,
    content = content,
  )
}

@Stable
@Composable
fun NormalActualIconButton(
  imageVector: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  enabled: Boolean = true,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit = { DefaultIconButtonContent(imageVector, contentDescription, size) },
) {
  BasicActualIconButton(
    imageVector = imageVector,
    contentDescription = contentDescription,
    onClick = onClick,
    colors = { scheme, isPressed -> scheme.normal(isPressed) },
    modifier = modifier,
    size = size,
    enabled = enabled,
    shape = shape,
    interactionSource = interactionSource,
    content = content,
  )
}

@Stable
@Composable
fun BareActualIconButton(
  imageVector: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  enabled: Boolean = true,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit = { DefaultIconButtonContent(imageVector, contentDescription, size) },
) {
  BasicActualIconButton(
    imageVector = imageVector,
    contentDescription = contentDescription,
    onClick = onClick,
    colors = { scheme, isPressed -> scheme.bare(isPressed) },
    modifier = modifier,
    size = size,
    enabled = enabled,
    shape = shape,
    interactionSource = interactionSource,
    content = content,
  )
}

@Stable
@Composable
fun BasicActualIconButton(
  imageVector: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  colors: @Composable (theme: Theme, isPressed: Boolean) -> IconButtonColors,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  enabled: Boolean = true,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit = { DefaultIconButtonContent(imageVector, contentDescription, size) },
) {
  val theme = LocalTheme.current
  val isPressed by interactionSource.collectIsPressedAsState()
  val buttonColors = colors(theme, isPressed)
  val background = if (enabled) buttonColors.containerColor else buttonColors.disabledContainerColor

  IconButton(
    modifier = modifier.background(background, shape),
    onClick = onClick,
    enabled = enabled,
    colors = buttonColors,
    content = content,
  )
}

@Stable
@Composable
private fun DefaultIconButtonContent(
  imageVector: ImageVector,
  contentDescription: String,
  size: Dp? = null,
) {
  Icon(
    modifier = if (size == null) Modifier else Modifier.size(size),
    imageVector = imageVector,
    contentDescription = contentDescription,
  )
}

@Stable
@Composable
private fun Theme.primary(isPressed: Boolean) = IconButtonDefaults.filledIconButtonColors(
  containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
  disabledContainerColor = buttonPrimaryDisabledBackground,
  contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
  disabledContentColor = buttonPrimaryDisabledText,
)

@Stable
@Composable
private fun Theme.normal(isPressed: Boolean) = IconButtonDefaults.filledIconButtonColors(
  containerColor = if (isPressed) buttonNormalBackground else buttonNormalBackgroundHover,
  disabledContainerColor = buttonNormalDisabledBackground,
  contentColor = if (isPressed) buttonNormalText else buttonNormalTextHover,
  disabledContentColor = buttonNormalDisabledText,
)

@Stable
@Composable
private fun Theme.bare(isPressed: Boolean) = IconButtonDefaults.filledIconButtonColors(
  containerColor = if (isPressed) buttonBareBackgroundHover else buttonBareBackground,
  disabledContainerColor = buttonBareDisabledBackground,
  contentColor = if (isPressed) buttonBareTextHover else buttonBareText,
  disabledContentColor = buttonBareDisabledText,
)

@Preview
@Composable
private fun Bare() = PreviewActualColumn {
  BareActualIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "Cancel",
    onClick = {},
  )
}

@Preview
@Composable
private fun Normal() = PreviewActualColumn {
  NormalActualIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "Cancel",
    onClick = {},
  )
}

@Preview
@Composable
private fun Primary() = PreviewActualColumn {
  PrimaryActualIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "OK",
    onClick = {},
  )
}
