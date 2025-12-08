@file:Suppress("UnusedReceiverParameter", "ContentTrailingLambda")

package aktual.core.ui

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
@Composable
fun PrimaryTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ButtonPadding,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = AktualTypography.buttonTextStyle,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
  colors: @Composable (Theme, Boolean) -> ButtonColors = { scheme, pressed -> scheme.primaryButton(pressed) },
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
) {
  BasicTextButton(
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

@Composable
@Suppress("ModifierNotUsedAtRoot") // there's probably a better solution
fun PrimaryTextButtonWithLoading(
  text: String,
  isLoading: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ButtonPadding,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = AktualTypography.buttonTextStyle,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
  colors: @Composable (Theme, Boolean) -> ButtonColors = { scheme, pressed -> scheme.primaryButton(pressed) },
) {
  PrimaryTextButton(
    text = text,
//    modifier = modifier,
    modifier = Modifier.testTag(Tags.PrimaryTextButtonWithLoading),
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
      Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
      ) {
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
          style = style,
          fontSize = fontSize,
        )
      }
    },
  )
}

@Stable
@Composable
fun NormalTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ButtonPadding,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = AktualTypography.buttonTextStyle,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
  colors: @Composable (Theme, Boolean) -> ButtonColors = { t, pressed -> t.normalButton(pressed) },
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
) {
  BareTextButton(
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
fun BareTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ButtonPadding,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = AktualTypography.buttonTextStyle,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
  colors: @Composable (Theme, Boolean) -> ButtonColors = { theme, pressed -> theme.bareButton(pressed) },
  content: @Composable RowScope.() -> Unit = { DefaultTextButtonContent(text, style, fontSize, prefix) },
) {
  BasicTextButton(
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
fun BasicTextButton(
  text: String,
  colors: @Composable (theme: Theme, isPressed: Boolean) -> ButtonColors,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = ButtonPadding,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = LocalTextStyle.current,
  fontSize: TextUnit = TextUnit.Unspecified,
  prefix: (@Composable () -> Unit)? = null,
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
  prefix: (@Composable () -> Unit)?,
) {
  prefix?.invoke()

  Text(
    text = text,
    style = style,
    fontSize = fontSize,
  )
}

private val ButtonPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)

val Typography.buttonTextStyle: TextStyle
  get() = bodyMedium.copy(fontSize = 15.sp)

@Preview
@Composable
private fun PreviewBare(
  @PreviewParameter(BoolColorSchemeParameters::class) params: ThemedParams<Boolean>,
) = PreviewWithColorScheme(params.type) {
  BareTextButton(
    text = "Bare",
    isEnabled = params.data,
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewPrimary(
  @PreviewParameter(BoolColorSchemeParameters::class) params: ThemedParams<Boolean>,
) = PreviewWithColorScheme(params.type) {
  PrimaryTextButton(
    text = "Primary",
    isEnabled = params.data,
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewNormal(
  @PreviewParameter(BoolColorSchemeParameters::class) params: ThemedParams<Boolean>,
) = PreviewWithColorScheme(params.type) {
  NormalTextButton(
    text = "Normal",
    isEnabled = params.data,
    onClick = {},
  )
}

@Preview
@Composable
private fun PreviewPrimaryWithLoadingNotLoading(
  @PreviewParameter(BoolColorSchemeParameters::class) params: ThemedParams<Boolean>,
) = PreviewWithColorScheme(params.type) {
  PrimaryTextButtonWithLoading(
    text = "OK",
    isLoading = params.data,
    onClick = {},
  )
}

private class BoolColorSchemeParameters : ThemedParameterProvider<Boolean>(true, false)
