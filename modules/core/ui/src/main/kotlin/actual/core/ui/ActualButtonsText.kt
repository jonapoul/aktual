@file:Suppress("UnusedReceiverParameter", "ContentTrailingLambda")

package actual.core.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
@Composable
fun PrimaryActualTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = MaterialTheme.typography.buttonPrimary,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: ComposableLambda? = null,
  colors: @Composable (Theme, Boolean) -> ButtonColors = { scheme, pressed -> scheme.primaryButton(pressed) },
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
) {
  BasicActualTextButton(
    text = text,
    modifier = modifier,
    isEnabled = isEnabled,
    contentPadding = contentPadding,
    shape = shape,
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    prefix = prefix,
    onClick = onClick,
    colors = colors,
    content = content,
  )
}

@Stable
@Composable
fun PrimaryActualTextButtonWithLoading(
  text: String,
  isLoading: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = MaterialTheme.typography.buttonPrimary,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: ComposableLambda? = null,
  colors: @Composable (Theme, Boolean) -> ButtonColors = { scheme, pressed -> scheme.primaryButton(pressed) },
) {
  PrimaryActualTextButton(
    text = text,
    modifier = modifier,
    isEnabled = isEnabled && !isLoading,
    contentPadding = contentPadding,
    shape = shape,
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    prefix = prefix,
    colors = colors,
    onClick = onClick,
    content = {
      // Using opacity here so we don't adjust the size bounds of the containing box
      Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
          modifier = Modifier
            .alpha(if (isLoading) 1f else 0f)
            .size(20.dp),
          color = LocalTheme.current.buttonPrimaryText,
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
fun NormalActualTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = MaterialTheme.typography.buttonPrimary,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: ComposableLambda? = null,
  colors: @Composable (Theme, Boolean) -> ButtonColors = { t, pressed -> t.normalButton(pressed) },
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
) {
  BareActualTextButton(
    text = text,
    modifier = modifier,
    isEnabled = isEnabled,
    contentPadding = contentPadding,
    shape = shape,
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    prefix = prefix,
    onClick = onClick,
    colors = colors,
    content = content,
  )
}

@Stable
@Composable
fun BareActualTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = MaterialTheme.typography.buttonBare,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: ComposableLambda? = null,
  colors: @Composable (Theme, Boolean) -> ButtonColors = { theme, pressed -> theme.bareButton(pressed) },
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
) {
  BasicActualTextButton(
    text = text,
    modifier = modifier,
    isEnabled = isEnabled,
    contentPadding = contentPadding,
    shape = shape,
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    prefix = prefix,
    onClick = onClick,
    colors = colors,
    content = content,
  )
}

@Stable
@Composable
fun BasicActualTextButton(
  text: String,
  colors: @Composable (theme: Theme, isPressed: Boolean) -> ButtonColors,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ActualButtonPadding,
  shape: Shape = ActualButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = LocalTextStyle.current,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: ComposableLambda? = null,
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
) {
  val theme = LocalTheme.current
  val isPressed by interactionSource.collectIsPressedAsState()

  TextButton(
    modifier = modifier.widthIn(min = 1.dp),
    enabled = isEnabled,
    shape = shape,
    colors = colors(theme, isPressed),
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
  prefix: ComposableLambda?,
) {
  prefix?.invoke()

  Text(
    text = text,
    fontFamily = ActualFontFamily,
    style = style,
    fontSize = fontSize,
  )
}

private val Typography.buttonPrimary: TextStyle
  @Composable
  @ReadOnlyComposable
  get() = actualTextStyle(fontSize = 15.sp, color = LocalTheme.current.buttonPrimaryText)

private val Typography.buttonBare: TextStyle
  @Composable
  @ReadOnlyComposable
  get() = actualTextStyle(fontSize = 15.sp, color = LocalTheme.current.buttonBareText)

@Preview
@Composable
private fun PreviewBareButton() = PreviewActualColumn {
  BareActualTextButton(
    text = "Bare",
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewPrimaryButton() = PreviewActualColumn {
  PrimaryActualTextButton(
    text = "Primary",
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewNormalButton() = PreviewActualColumn {
  NormalActualTextButton(
    text = "Normal",
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewPrimaryWithLoadingNotLoadingButton() = PreviewActualColumn {
  PrimaryActualTextButtonWithLoading(
    text = "OK",
    isLoading = false,
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewPrimaryWithLoadingButton() = PreviewActualColumn {
  PrimaryActualTextButtonWithLoading(
    text = "OK",
    isLoading = true,
    onClick = {},
  )
}
