package aktual.core.ui

import aktual.core.theme.Colors
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

@Composable
fun Colors.topAppBarColors(
  containerColor: Color = mobileHeaderBackground,
  titleContentColor: Color = mobileHeaderText,
  navigationIconContentColor: Color = mobileHeaderText,
  actionIconContentColor: Color = mobileHeaderText,
): TopAppBarColors =
  TopAppBarDefaults.topAppBarColors(
    containerColor = containerColor,
    titleContentColor = titleContentColor,
    navigationIconContentColor = navigationIconContentColor,
    actionIconContentColor = actionIconContentColor,
  )

@Composable
fun Colors.transparentTopAppBarColors(): TopAppBarColors =
  topAppBarColors(
    containerColor = Transparent,
    navigationIconContentColor = pageText,
    actionIconContentColor = pageText,
    titleContentColor = pageText,
  )

@Stable
fun Colors.dropDownMenuItem(): MenuItemColors =
  MenuItemColors(
    textColor = menuItemText,
    leadingIconColor = menuItemText,
    trailingIconColor = menuItemText,
    disabledTextColor = menuItemText.disabled,
    disabledLeadingIconColor = menuItemText.disabled,
    disabledTrailingIconColor = menuItemText.disabled,
  )

@Composable
fun Colors.textField(
  focusedContainer: Color = fieldBackground(),
  unfocusedContainer: Color = focusedContainer,
  border: Color = formInputBorder,
  unfocusedBorder: Color = border,
  text: Color = formInputText,
  textPlaceholder: Color = formInputTextPlaceholder,
  icon: Color = formInputText,
): TextFieldColors =
  TextFieldDefaults.colors(
    focusedTextColor = text,
    unfocusedTextColor = text,
    disabledTextColor = text.disabled,
    focusedPlaceholderColor = textPlaceholder,
    unfocusedPlaceholderColor = textPlaceholder,
    disabledPlaceholderColor = textPlaceholder.disabled,
    focusedLabelColor = textPlaceholder,
    unfocusedLabelColor = textPlaceholder,
    disabledLabelColor = textPlaceholder.disabled,
    focusedIndicatorColor = border,
    unfocusedIndicatorColor = unfocusedBorder,
    disabledIndicatorColor = border.disabled,
    focusedContainerColor = focusedContainer,
    unfocusedContainerColor = unfocusedContainer,
    disabledContainerColor = unfocusedContainer.disabled,
    cursorColor = text,
    focusedLeadingIconColor = icon,
    unfocusedLeadingIconColor = icon,
    disabledLeadingIconColor = icon.disabled,
    focusedTrailingIconColor = icon,
    unfocusedTrailingIconColor = icon,
    disabledTrailingIconColor = icon.disabled,
  )

@Composable
fun Colors.exposedDropDownMenu(): TextFieldColors =
  textField()
    .copy(focusedTrailingIconColor = formInputText, unfocusedTrailingIconColor = formInputText)

// Darker than the surface behind it in dark themes, lighter in light themes. If the surface is
// already too close to black or white for that, shift it a little the other way instead
@Stable
fun Colors.fieldBackground(surface: Color): Color {
  val shifted = lerp(surface, if (isLight) White else Black, FIELD_SHIFT)
  return if (contrast(shifted, surface) >= FIELD_MIN_CONTRAST) {
    shifted
  } else {
    lerp(surface, if (isLight) Black else White, FIELD_FALLBACK_SHIFT)
  }
}

@Composable
@ReadOnlyComposable
fun Colors.fieldBackground(): Color = fieldBackground(LocalSurfaceColor.current ?: pageBackground)

private fun contrast(a: Color, b: Color): Float {
  val lumA = a.luminance() + CONTRAST_OFFSET
  val lumB = b.luminance() + CONTRAST_OFFSET
  return maxOf(lumA, lumB) / minOf(lumA, lumB)
}

private const val FIELD_SHIFT = 0.2f
private const val FIELD_FALLBACK_SHIFT = 0.08f
private const val FIELD_MIN_CONTRAST = 1.1f
private const val CONTRAST_OFFSET = 0.05f

@Composable
fun Colors.primaryButton(isPressed: Boolean): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
    disabledContainerColor = buttonPrimaryDisabledBackground,
    contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
    disabledContentColor = buttonPrimaryDisabledText,
  )

@Composable
fun Colors.normalButton(
  isPressed: Boolean,
  container: Color = buttonNormalBackground,
  containerPressed: Color = buttonNormalSelectedBackground,
  containerDisabled: Color = buttonNormalDisabledBackground,
  text: Color = buttonNormalText,
  textPressed: Color = buttonNormalSelectedText,
  textDisabled: Color = buttonNormalDisabledText,
): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) containerPressed else container,
    disabledContainerColor = containerDisabled,
    contentColor = if (isPressed) textPressed else text,
    disabledContentColor = textDisabled,
  )

@Composable
fun Colors.tonalButton(isPressed: Boolean): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor =
      buttonPrimaryBackground.copy(alpha = if (isPressed) ALPHA_TONAL_PRESSED else ALPHA_TONAL),
    disabledContainerColor = buttonPrimaryBackground.copy(alpha = ALPHA_TONAL_DISABLED),
    contentColor = pageTextPositive,
    disabledContentColor = pageTextPositive.disabled,
  )

@Composable
fun Colors.errorButton(isPressed: Boolean): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) errorBorder else errorBackground,
    contentColor = if (isPressed) errorBackground else errorText,
  )

@Composable
fun Colors.bareButton(isPressed: Boolean): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) buttonBareBackground else buttonBareBackgroundHover,
    disabledContainerColor = buttonBareDisabledBackground,
    contentColor = if (isPressed) buttonBareText else buttonBareTextHover,
    disabledContentColor = buttonBareDisabledText,
  )

@Composable
fun Colors.primaryIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
    disabledContainerColor = buttonPrimaryDisabledBackground,
    contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
    disabledContentColor = buttonPrimaryDisabledText,
  )

@Composable
fun Colors.normalIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonNormalBackground else buttonNormalBackgroundHover,
    disabledContainerColor = buttonNormalDisabledBackground,
    contentColor = if (isPressed) buttonNormalText else buttonNormalTextHover,
    disabledContentColor = buttonNormalDisabledText,
  )

@Composable
fun Colors.bareIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonBareBackgroundHover else buttonBareBackground,
    disabledContainerColor = buttonBareDisabledBackground,
    contentColor = if (isPressed) buttonBareTextHover else buttonBareText,
    disabledContentColor = buttonBareDisabledText,
  )

@Composable
fun Colors.switch(): SwitchColors =
  SwitchDefaults.colors(
    checkedThumbColor = checkboxText,
    uncheckedThumbColor = checkboxText,
    checkedTrackColor = checkboxToggleBackgroundSelected,
    uncheckedTrackColor = checkboxToggleBackground,
    checkedBorderColor = checkboxToggleBackgroundSelected,
    uncheckedBorderColor = checkboxToggleBackground,
    disabledCheckedThumbColor = checkboxText.disabled,
    disabledUncheckedThumbColor = checkboxText.disabled,
    disabledCheckedTrackColor = checkboxToggleDisabled,
    disabledUncheckedTrackColor = checkboxToggleDisabled,
    disabledCheckedBorderColor = checkboxToggleDisabled,
    disabledUncheckedBorderColor = checkboxToggleDisabled,
  )

@Composable
fun Colors.radioButton(): RadioButtonColors =
  RadioButtonDefaults.colors(
    selectedColor = checkboxToggleBackgroundSelected,
    unselectedColor = pageText.disabled,
    disabledSelectedColor = pageText.disabled,
    disabledUnselectedColor = pageText.disabled,
  )

@Composable
fun Colors.slider(): SliderColors =
  SliderDefaults.colors(
    thumbColor = checkboxToggleBackgroundSelected,
    activeTrackColor = checkboxToggleBackgroundSelected,
    activeTickColor = checkboxText,
    inactiveTrackColor = checkboxToggleBackground,
    inactiveTickColor = pageText.disabled,
    disabledThumbColor = checkboxToggleBackgroundSelected.disabled,
    disabledActiveTrackColor = checkboxToggleBackgroundSelected.disabled,
    disabledActiveTickColor = checkboxText.disabled,
    disabledInactiveTrackColor = checkboxToggleBackground.disabled,
    disabledInactiveTickColor = pageText.disabled,
  )

@Composable
fun Colors.checkbox(): CheckboxColors =
  CheckboxDefaults.colors(
    checkedCheckmarkColor = checkboxText,
    uncheckedCheckmarkColor = Transparent,
    disabledCheckmarkColor = checkboxText,
    checkedBoxColor = checkboxBackgroundSelected,
    uncheckedBoxColor = Transparent,
    disabledCheckedBoxColor = checkboxBackgroundSelected.disabled,
    disabledUncheckedBoxColor = Transparent,
    disabledIndeterminateBoxColor = checkboxBackgroundSelected.disabled,
    checkedBorderColor = checkboxBorderSelected,
    uncheckedBorderColor = pageText.disabled,
    disabledBorderColor = checkboxBorderSelected.disabled,
    disabledUncheckedBorderColor = pageText.disabled,
    disabledIndeterminateBorderColor = checkboxBorderSelected.disabled,
  )

@get:Stable
val Color.disabled: Color
  get() = copy(alpha = ALPHA_DISABLED)

@Stable fun Color.disabledIf(condition: Boolean): Color = if (condition) disabled else this

private const val ALPHA_DISABLED = 0.38f
private const val ALPHA_TONAL = 0.18f
private const val ALPHA_TONAL_PRESSED = 0.3f
private const val ALPHA_TONAL_DISABLED = 0.07f
