package dev.jonpoulton.actual.core.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Stable
@Composable
fun PrimaryActualTextButton(
  text: String,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = LocalTextStyle.current,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
  onClick: () -> Unit,
) {
  BasicActualTextButton(
    text = text,
    modifier = modifier,
    contentPadding = contentPadding,
    shape = shape,
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    prefix = prefix,
    onClick = onClick,
    colors = { scheme, isPressed -> scheme.primaryButton(isPressed) },
    content = content,
  )
}

@Stable
@Composable
fun PrimaryActualTextButtonWithLoading(
  text: String,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = LocalTextStyle.current,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
  onClick: () -> Unit,
) {
  PrimaryActualTextButton(
    text = text,
    modifier = modifier,
    contentPadding = contentPadding,
    shape = shape,
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    prefix = prefix,
    onClick = onClick,
    content = {
      // Using opacity here so we don't adjust the size bounds of the containing box
      Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
          modifier = Modifier
            .alpha(if (isLoading) 1f else 0f)
            .size(20.dp),
          color = LocalActualColorScheme.current.buttonPrimaryText,
          strokeWidth = 2.dp,
        )

        Text(
          modifier = Modifier.alpha(if (isLoading) 0f else 1f),
          text = text,
          fontFamily = ActualFontFamily,
          style = style,
          fontSize = fontSize,
        )
      }
    },
  )
}

@Stable
@Composable
fun BareActualTextButton(
  text: String,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = LocalTextStyle.current,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
  onClick: () -> Unit,
) {
  BasicActualTextButton(
    text = text,
    modifier = modifier,
    contentPadding = contentPadding,
    shape = shape,
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    prefix = prefix,
    colors = { scheme, isPressed -> scheme.bareButton(isPressed) },
    content = content,
    onClick = onClick,
  )
}

@Stable
@Composable
fun BasicActualTextButton(
  text: String,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = LocalTextStyle.current,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
  colors: @Composable (scheme: ActualColorScheme, isPressed: Boolean) -> ButtonColors,
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
  onClick: () -> Unit,
) {
  val colorScheme = LocalActualColorScheme.current
  val isPressed by interactionSource.collectIsPressedAsState()

  TextButton(
    modifier = modifier.widthIn(min = 1.dp),
    shape = shape,
    colors = colors(colorScheme, isPressed),
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    onClick = onClick,
    content = { content() },
  )
}

@Stable
@Composable
private fun RowScope.DefaultTextButtonContent(
  text: String,
  style: TextStyle,
  fontSize: TextUnit,
  prefix: (@Composable () -> Unit)?,
) {
  prefix?.invoke()

  Text(
    text = text,
    fontFamily = ActualFontFamily,
    style = style,
    fontSize = fontSize,
  )
}

private val ActualButtonPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
private val ActualButtonShape = RoundedCornerShape(size = 4.dp)

@Stable
@Composable
private fun ActualColorScheme.primaryButton(isPressed: Boolean): ButtonColors = ButtonDefaults.buttonColors(
  containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
  disabledContainerColor = buttonPrimaryDisabledBackground,
  contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
  disabledContentColor = buttonPrimaryDisabledText,
)

@Stable
@Composable
private fun ActualColorScheme.bareButton(isPressed: Boolean): ButtonColors = ButtonDefaults.buttonColors(
  containerColor = if (isPressed) buttonBareBackground else buttonBareBackgroundHover,
  disabledContainerColor = buttonBareDisabledBackground,
  contentColor = if (isPressed) buttonBareText else buttonBareTextHover,
  disabledContentColor = buttonBareDisabledText,
)

@PreviewThemes
@Composable
private fun PreviewBareButton() = PreviewActual {
  BareActualTextButton(
    text = "Cancel",
    onClick = {},
  )
}

@PreviewThemes
@Composable
private fun PreviewPrimaryButton() = PreviewActual {
  PrimaryActualTextButton(
    text = "OK",
    onClick = {},
  )
}

@PreviewThemes
@Composable
private fun PreviewPrimaryWithLoadingNotLoadingButton() = PreviewActual {
  PrimaryActualTextButtonWithLoading(
    text = "OK",
    isLoading = false,
    onClick = {},
  )
}

@PreviewThemes
@Composable
private fun PreviewPrimaryWithLoadingButton() = PreviewActual {
  PrimaryActualTextButtonWithLoading(
    text = "OK",
    isLoading = true,
    onClick = {},
  )
}
