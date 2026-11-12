package aktual.core.ui

import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.CheveronLeft
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Menu
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach

@Composable
fun AktualButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  kind: ButtonKind = Normal,
  isEnabled: Boolean = true,
  isLoading: Boolean = false,
  icon: ImageVector? = null,
  contentPadding: PaddingValues = DefaultButtonPadding,
  shape: Shape = ButtonShape,
  style: TextStyle = typography.buttonTextStyle,
  fontSize: TextUnit = Unspecified,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  val themeColors = AktualTheme.colors
  val enabled = isEnabled && !isLoading
  val colors: @Composable (Boolean) -> ButtonColors = { pressed ->
    themeColors.buttonColors(kind, pressed)
  }

  BasicTextButton(
    text = text,
    colors = colors,
    onClick = onClick,
    modifier = modifier,
    isEnabled = enabled,
    contentPadding = contentPadding,
    shape = shape,
    border = themeColors.buttonBorder(kind, enabled),
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    icon = icon,
  ) {
    // Hide the content with alpha so the button keeps its size while loading
    Box(contentAlignment = Center) {
      Row(
        modifier = Modifier.alpha(if (isLoading) 0f else 1f),
        verticalAlignment = CenterVertically,
      ) {
        DefaultTextButtonContent(text, style, fontSize, icon)
      }

      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(LoadingIndicatorSize),
          color = colors(false).contentColor,
          strokeWidth = LoadingIndicatorStroke,
        )
      }
    }
  }
}

enum class ButtonKind {
  Primary,
  Normal,
  Bare,
  Tonal,
  Destructive,
}

@Composable
private fun Colors.buttonColors(kind: ButtonKind, isPressed: Boolean): ButtonColors =
  when (kind) {
    Primary -> primaryButton(isPressed)
    Normal -> normalButton(isPressed)
    Bare -> bareButton(isPressed)
    Tonal -> tonalButton(isPressed)
    Destructive -> errorButton(isPressed)
  }

private fun Colors.buttonBorder(kind: ButtonKind, isEnabled: Boolean): BorderStroke? {
  val (width, color) =
    when (kind) {
      Primary -> Dp.Hairline to buttonPrimaryBorder
      Normal -> Dp.Hairline to buttonNormalBorder
      Bare -> return null
      Tonal -> TonalButtonBorder to buttonPrimaryBorder
      Destructive -> Dp.Hairline to errorBorder
    }
  return BorderStroke(width, color.disabledIf(!isEnabled))
}

private val TonalButtonBorder = 1.dp
private val LoadingIndicatorSize = 20.dp
private val LoadingIndicatorStroke = 2.dp

@Composable
fun BasicTextButton(
  text: String,
  colors: @Composable (isPressed: Boolean) -> ButtonColors,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  contentPadding: PaddingValues = DefaultButtonPadding,
  shape: Shape = ButtonShape,
  border: BorderStroke? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  style: TextStyle = LocalTextStyle.current,
  fontSize: TextUnit = Unspecified,
  icon: ImageVector? = null,
  content: @Composable RowScope.() -> Unit = {
    DefaultTextButtonContent(text, style, fontSize, icon)
  },
) {
  val isPressed by interactionSource.collectIsPressedAsState()

  TextButton(
    modifier = modifier.widthIn(min = 1.dp),
    enabled = isEnabled,
    shape = shape,
    border = border,
    colors = colors(isPressed),
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    onClick = onClick,
    content = content,
  )
}

@Composable
private fun RowScope.DefaultTextButtonContent(
  text: String,
  style: TextStyle,
  fontSize: TextUnit,
  icon: ImageVector?,
) {
  if (icon != null) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(ButtonIconSize))
    HorizontalSpacer(ButtonIconSpacing)
  }
  Text(text = text, style = style, fontSize = fontSize, textAlign = Center)
}

private val ButtonIconSize = 18.dp
private val ButtonIconSpacing = 8.dp

val DefaultButtonPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)

val Typography.buttonTextStyle: TextStyle
  get() = bodyMedium.copy(fontSize = textSizes.mediumPlus)

@Composable
fun AktualIconButton(
  imageVector: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  kind: ButtonKind = Bare,
  isEnabled: Boolean = true,
  isLoading: Boolean = false,
  size: Dp? = null,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  val colors =
    remember(kind) {
      IconButtonColorProvider { theme, isPressed -> theme.iconButtonColors(kind, isPressed) }
    }

  BasicIconButton(
    imageVector = imageVector,
    contentDescription = contentDescription,
    onClick = onClick,
    colors = colors,
    modifier = modifier,
    size = size,
    isEnabled = isEnabled && !isLoading,
    shape = shape,
    interactionSource = interactionSource,
  ) {
    Box(contentAlignment = Center) {
      DefaultIconButtonContent(
        imageVector = imageVector,
        contentDescription = contentDescription,
        size = size,
        modifier = Modifier.alpha(if (isLoading) 0f else 1f),
      )

      if (isLoading) {
        CircularProgressIndicator(
          modifier =
            Modifier.size(size?.coerceAtMost(LoadingIndicatorSize) ?: LoadingIndicatorSize),
          color = colors(AktualTheme.colors, isPressed = false).contentColor,
          strokeWidth = LoadingIndicatorStroke,
        )
      }
    }
  }
}

@Composable
fun BasicIconButton(
  imageVector: ImageVector,
  contentDescription: String?,
  onClick: () -> Unit,
  colors: IconButtonColorProvider,
  modifier: Modifier = Modifier,
  size: Dp? = null,
  isEnabled: Boolean = true,
  shape: Shape = ButtonShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit = {
    DefaultIconButtonContent(imageVector, contentDescription, size)
  },
) {
  val isPressed by interactionSource.collectIsPressedAsState()
  val buttonColors = colors(AktualTheme.colors, isPressed)
  val background =
    if (isEnabled) buttonColors.containerColor else buttonColors.disabledContainerColor

  IconButton(
    modifier = modifier.background(background, shape),
    onClick = onClick,
    enabled = isEnabled,
    colors = buttonColors,
    interactionSource = interactionSource,
    content = content,
  )
}

@Immutable
fun interface IconButtonColorProvider {
  @Composable operator fun invoke(colors: Colors, isPressed: Boolean): IconButtonColors
}

@Composable
private fun Colors.iconButtonColors(kind: ButtonKind, isPressed: Boolean): IconButtonColors =
  when (kind) {
    Primary -> primaryIconButton(isPressed)
    Normal -> normalIconButton(isPressed)
    Bare -> bareIconButton(isPressed)
    Tonal -> tonalIconButton(isPressed)
    Destructive -> errorIconButton(isPressed)
  }

@Composable
private fun DefaultIconButtonContent(
  imageVector: ImageVector,
  contentDescription: String?,
  size: Dp?,
  modifier: Modifier = Modifier,
) {
  Icon(
    modifier = if (size == null) modifier else modifier.size(size),
    imageVector = imageVector,
    contentDescription = contentDescription,
  )
}

@Composable
@Suppress("LambdaParameterEventTrailing")
fun NavBackIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  IconButton(modifier = modifier, onClick = onClick) {
    Icon(imageVector = AktualIcons.CheveronLeft, contentDescription = Strings.navBack)
  }
}

// Only shown when a nav drawer is available to open
@Composable
fun NavDrawerIconButton(modifier: Modifier = Modifier) {
  val openDrawer = LocalNavDrawerOpener.current ?: return
  IconButton(modifier = modifier, onClick = openDrawer) {
    Icon(imageVector = MaterialIcons.Menu, contentDescription = Strings.budgetNavMenu)
  }
}

@Preview
@Composable
private fun PreviewButtons(
  @PreviewParameter(BooleanColorParameters::class) params: ColorParams<Boolean>,
) =
  PreviewWithColorParams(params) {
    val isEnabled = this
    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      ButtonKind.entries.fastForEach { kind ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AktualButton(text = kind.name, kind = kind, isEnabled = isEnabled, onClick = {})
          AktualButton(
            text = kind.name,
            kind = kind,
            icon = AktualIcons.Add,
            isEnabled = isEnabled,
            onClick = {},
          )
        }
      }
    }
  }

@Preview
@Composable
private fun PreviewLoading(
  @PreviewParameter(BooleanColorParameters::class) params: ColorParams<Boolean>,
) =
  PreviewWithColorParams(params) {
    val isLoading = this
    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      ButtonKind.entries.fastForEach { kind ->
        AktualButton(text = kind.name, kind = kind, isLoading = isLoading, onClick = {})
      }
    }
  }

@Preview
@Composable
private fun PreviewIconButtons(
  @PreviewParameter(BooleanColorParameters::class) params: ColorParams<Boolean>,
) =
  PreviewWithColorParams(params) {
    val isEnabled = this
    Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      ButtonKind.entries.fastForEach { kind ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          AktualIconButton(
            imageVector = AktualIcons.Check,
            contentDescription = kind.name,
            kind = kind,
            isEnabled = isEnabled,
            onClick = {},
          )
          AktualIconButton(
            imageVector = AktualIcons.Check,
            contentDescription = kind.name,
            kind = kind,
            isLoading = true,
            onClick = {},
          )
        }
      }
    }
  }
