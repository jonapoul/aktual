package aktual.core.ui

import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.CheveronLeft
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Menu
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import aktual.theme.Colors
import alakazam.compose.HorizontalSpacer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

@Composable
fun AktualButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  kind: ButtonKind = Normal,
  variant: ButtonVariant = Filled,
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
    when (variant) {
      Filled -> themeColors.buttonColors(kind, pressed)
      Secondary -> themeColors.secondaryButtonColors(kind, pressed)
    }
  }
  val border =
    when (variant) {
      Filled -> themeColors.buttonBorder(kind, enabled)
      Secondary -> themeColors.secondaryButtonBorder(kind, enabled)
    }

  BasicTextButton(
    text = text,
    colors = colors,
    onClick = onClick,
    modifier = modifier,
    isEnabled = enabled,
    contentPadding = contentPadding,
    shape = shape,
    border = border,
    interactionSource = interactionSource,
    style = style,
    fontSize = fontSize,
    icon = icon,
  ) {
    LoadingTextButtonContent(isLoading, colors(false).contentColor) {
      DefaultTextButtonContent(text, style, fontSize, icon)
    }
  }
}

// Floating action button. Collapsing hides the text and fades it a little, e.g. while scrolling
// down a list. Showing and hiding slides it in from and out to the bottom of the screen
@Composable
fun AktualFab(
  text: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  kind: ButtonKind = Primary,
  isVisible: Boolean = true,
  isExpanded: Boolean = true,
  isFaded: Boolean = !isExpanded,
  isEnabled: Boolean = true,
  isLoading: Boolean = false,
) {
  val themeColors = AktualTheme.colors
  val alpha by animateFloatAsState(if (isFaded) FAB_FADED_ALPHA else 1f)
  val enabled = isEnabled && !isLoading

  AnimatedVisibility(
    modifier = modifier,
    visible = isVisible,
    enter = slideInVertically { it } + scaleIn(initialScale = FAB_HIDDEN_SCALE) + fadeIn(),
    exit = slideOutVertically { it } + scaleOut(targetScale = FAB_HIDDEN_SCALE) + fadeOut(),
  ) {
    BasicTextButton(
      text = text,
      colors = { pressed -> themeColors.buttonColors(kind, pressed) },
      onClick = onClick,
      modifier = Modifier.alpha(alpha),
      isEnabled = enabled,
      contentPadding = FabPadding,
      shape = RounderCardShape,
      border = themeColors.buttonBorder(kind, enabled),
    ) {
      LoadingTextButtonContent(isLoading, themeColors.buttonColors(kind, false).contentColor) {
        FabContent(text, icon, isExpanded)
      }
    }
  }
}

@Composable
private fun FabContent(text: String, icon: ImageVector, isExpanded: Boolean) {
  Row(modifier = Modifier.height(FabContentHeight), verticalAlignment = CenterVertically) {
    AnimatedIcon(
      imageVector = icon,
      contentDescription = text.takeUnless { isExpanded },
      modifier = Modifier.size(FabIconSize),
    )
    AnimatedVisibility(
      visible = isExpanded,
      enter = expandHorizontally() + fadeIn(),
      exit = shrinkHorizontally() + fadeOut(),
    ) {
      Text(
        modifier = Modifier.padding(start = ButtonIconSpacing, end = FabTextEndPadding),
        text = text,
        style = typography.buttonTextStyle,
        maxLines = 1,
      )
    }
  }
}

private const val FAB_FADED_ALPHA = 0.6f
private const val FAB_HIDDEN_SCALE = 0.8f
private val FabPadding = PaddingValues(16.dp)
private val FabContentHeight = 24.dp
private val FabIconSize = 24.dp
private val FabTextEndPadding = 4.dp

// For dialog actions: tinted text with no background or border
@Composable
fun DialogTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  kind: DialogButtonKind = Normal,
  isEnabled: Boolean = true,
  isLoading: Boolean = false,
) {
  val tint =
    with(AktualTheme.colors) {
      when (kind) {
        Normal -> pageText
        Primary -> pageTextPositive
        Destructive -> errorText
      }
    }
  val style = typography.buttonTextStyle

  BasicTextButton(
    text = text,
    colors = {
      ButtonColors(
        containerColor = Transparent,
        disabledContainerColor = Transparent,
        contentColor = tint,
        disabledContentColor = tint.disabled,
      )
    },
    onClick = onClick,
    modifier = modifier,
    isEnabled = isEnabled && !isLoading,
    style = style,
  ) {
    LoadingTextButtonContent(isLoading, tint) {
      DefaultTextButtonContent(text, style, fontSize = Unspecified, icon = null)
    }
  }
}

enum class DialogButtonKind {
  Normal,
  Primary,
  Destructive,
}

// Hide the content with alpha so the button keeps its size while loading
@Composable
private fun LoadingTextButtonContent(
  isLoading: Boolean,
  indicatorColor: Color,
  content: @Composable RowScope.() -> Unit,
) {
  Box(contentAlignment = Center) {
    Row(
      modifier = Modifier.alpha(if (isLoading) 0f else 1f),
      verticalAlignment = CenterVertically,
      content = content,
    )

    if (isLoading) {
      AktualProgressIndicator(size = LoadingIndicatorSize, color = indicatorColor)
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
      Destructive -> Dp.Hairline to error
    }
  return BorderStroke(width, color.disabledIf(!isEnabled))
}

// Filled draws the kind's usual background. Secondary is for the less prominent option next to a
// filled button: transparent body, with the border and text in the kind's colour
enum class ButtonVariant {
  Filled,
  Secondary,
}

private fun Colors.secondaryButtonTint(kind: ButtonKind): Color =
  when (kind) {
    Primary,
    Tonal -> pageTextPositive
    Normal -> buttonNormalText
    Bare -> buttonBareText
    Destructive -> errorText
  }

private fun Colors.secondaryButtonColors(kind: ButtonKind, isPressed: Boolean): ButtonColors {
  val tint = secondaryButtonTint(kind)
  return ButtonColors(
    containerColor = if (isPressed) tint.copy(alpha = SECONDARY_PRESSED_ALPHA) else Transparent,
    disabledContainerColor = Transparent,
    contentColor = tint,
    disabledContentColor = tint.disabled,
  )
}

private fun Colors.secondaryButtonBorder(kind: ButtonKind, isEnabled: Boolean): BorderStroke? =
  if (kind == Bare) {
    null
  } else {
    BorderStroke(SecondaryButtonBorder, secondaryButtonTint(kind).disabledIf(!isEnabled))
  }

private const val SECONDARY_PRESSED_ALPHA = 0.12f
private val SecondaryButtonBorder = 1.dp
private val TonalButtonBorder = 1.dp
private val LoadingIndicatorSize = 20.dp

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
  // Also animates the width when the icon comes or goes
  AnimatedContent(
    targetState = icon,
    transitionSpec = {
      fadeIn() + scaleIn(initialScale = BUTTON_ICON_HIDDEN_SCALE) togetherWith
        fadeOut() + scaleOut(targetScale = BUTTON_ICON_HIDDEN_SCALE)
    },
    label = "ButtonIcon",
  ) { shown ->
    if (shown != null) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = shown,
          contentDescription = null,
          modifier = Modifier.size(ButtonIconSize),
        )
        HorizontalSpacer(ButtonIconSpacing)
      }
    }
  }
  Text(text = text, style = style, fontSize = fontSize, textAlign = Center)
}

private const val BUTTON_ICON_HIDDEN_SCALE = 0.6f
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
        AktualProgressIndicator(
          size = size?.coerceAtMost(LoadingIndicatorSize) ?: LoadingIndicatorSize,
          color = colors(AktualTheme.colors, isPressed = false).contentColor,
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
  val background by
    animateColorAsState(
      if (isEnabled) buttonColors.containerColor else buttonColors.disabledContainerColor,
    )

  val contentColor by
    animateColorAsState(
      if (isEnabled) buttonColors.contentColor else buttonColors.disabledContentColor,
    )

  Box(
    modifier =
      modifier
        .clip(shape)
        .background(background)
        .clickable(
          onClick = onClick,
          enabled = isEnabled,
          role = Button,
          interactionSource = interactionSource,
          indication = ripple(color = contentColor),
        )
        .minimumInteractiveComponentSize()
        .size(IconButtonSize),
    contentAlignment = Center,
  ) {
    CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
  }
}

private val IconButtonSize = 40.dp

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
  AnimatedIcon(
    modifier = if (size == null) modifier else modifier.size(size),
    imageVector = imageVector,
    contentDescription = contentDescription,
  )
}

@Composable
@Suppress("LambdaParameterEventTrailing")
fun NavBackIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  AktualIconButton(
    modifier = modifier,
    imageVector = AktualIcons.CheveronLeft,
    contentDescription = Strings.navBack,
    onClick = onClick,
  )
}

// Only shown when a nav drawer is available to open
@Composable
fun NavDrawerIconButton(modifier: Modifier = Modifier) {
  val openDrawer = LocalNavDrawerOpener.current ?: return
  AktualIconButton(
    modifier = modifier,
    imageVector = MaterialIcons.Menu,
    contentDescription = Strings.budgetNavMenu,
    onClick = openDrawer,
  )
}

@Preview
@Composable
private fun PreviewButtons(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      ButtonKind.entries.fastForEach { kind ->
        ButtonVariant.entries.fastForEach { variant ->
          AnimatedFlowRow {
            for (isEnabled in listOf(true, false)) {
              for (isLoading in listOf(false, true)) {
                item {
                  AktualButton(
                    text = kind.name,
                    kind = kind,
                    variant = variant,
                    isEnabled = isEnabled,
                    isLoading = isLoading,
                    onClick = {},
                  )
                }
                item {
                  AktualButton(
                    text = kind.name,
                    kind = kind,
                    variant = variant,
                    icon = AktualIcons.Add,
                    isEnabled = isEnabled,
                    isLoading = isLoading,
                    onClick = {},
                  )
                }
              }
            }
          }
        }
      }
    }
  }

@Preview
@Composable
private fun PreviewIconButtons(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      ButtonKind.entries.fastForEach { kind ->
        AnimatedFlowRow {
          for (isEnabled in listOf(true, false)) {
            item {
              AktualIconButton(
                imageVector = AktualIcons.Check,
                contentDescription = kind.name,
                kind = kind,
                isEnabled = isEnabled,
                onClick = {},
              )
            }
          }
          item {
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
  }

@Preview
@Composable
private fun PreviewDialogTextButtons(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      DialogButtonKind.entries.fastForEach { kind ->
        AnimatedFlowRow {
          for (isEnabled in listOf(true, false)) {
            item {
              DialogTextButton(
                text = kind.name,
                kind = kind,
                isEnabled = isEnabled,
                onClick = {},
              )
            }
          }
          item {
            DialogTextButton(
              text = kind.name,
              kind = kind,
              isLoading = true,
              onClick = {},
            )
          }
        }
      }
    }
  }

@Preview
@Composable
private fun PreviewButtonIconChanges(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
      while (true) {
        delay(PreviewIconDelay)
        index = (index + 1) % PreviewIcons.size
      }
    }

    AktualButton(text = "Confirm", kind = Primary, icon = PreviewIcons[index], onClick = {})
  }

private val PreviewIcons = persistentListOf(null, AktualIcons.Add, AktualIcons.Check)
private val PreviewIconDelay = 1.seconds
