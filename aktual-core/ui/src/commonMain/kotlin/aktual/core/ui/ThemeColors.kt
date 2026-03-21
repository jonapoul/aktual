package aktual.core.ui

import aktual.core.theme.Theme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
@Composable
fun Theme.topAppBarColors(
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

@Stable
@Composable
fun Theme.transparentTopAppBarColors(): TopAppBarColors =
  topAppBarColors(
    containerColor = Color.Transparent,
    navigationIconContentColor = pageText,
    actionIconContentColor = pageText,
    titleContentColor = pageText,
  )

@Stable
fun Theme.dropDownMenuItem(): MenuItemColors =
  MenuItemColors(
    textColor = menuItemText,
    leadingIconColor = menuItemText,
    trailingIconColor = menuItemText,
    disabledTextColor = menuItemText.disabled,
    disabledLeadingIconColor = menuItemText.disabled,
    disabledTrailingIconColor = menuItemText.disabled,
  )

@Stable
@Composable
fun Theme.textField(
  focusedContainer: Color = formInputBackground,
  unfocusedContainer: Color = focusedContainer,
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
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
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

@Stable
@Composable
fun Theme.exposedDropDownMenu(): TextFieldColors =
  textField()
    .copy(focusedTrailingIconColor = formInputText, unfocusedTrailingIconColor = formInputText)

@Stable
@Composable
fun Theme.primaryButton(isPressed: Boolean): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
    disabledContainerColor = buttonPrimaryDisabledBackground,
    contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
    disabledContentColor = buttonPrimaryDisabledText,
  )

@Stable
@Composable
fun Theme.normalButton(
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

@Stable
@Composable
fun Theme.bareButton(isPressed: Boolean): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) buttonBareBackground else buttonBareBackgroundHover,
    disabledContainerColor = buttonBareDisabledBackground,
    contentColor = if (isPressed) buttonBareText else buttonBareTextHover,
    disabledContentColor = buttonBareDisabledText,
  )

@Stable
@Composable
fun Theme.primaryIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
    disabledContainerColor = buttonPrimaryDisabledBackground,
    contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
    disabledContentColor = buttonPrimaryDisabledText,
  )

@Stable
@Composable
fun Theme.normalIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonNormalBackground else buttonNormalBackgroundHover,
    disabledContainerColor = buttonNormalDisabledBackground,
    contentColor = if (isPressed) buttonNormalText else buttonNormalTextHover,
    disabledContentColor = buttonNormalDisabledText,
  )

@Stable
@Composable
fun Theme.bareIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonBareBackgroundHover else buttonBareBackground,
    disabledContainerColor = buttonBareDisabledBackground,
    contentColor = if (isPressed) buttonBareTextHover else buttonBareText,
    disabledContentColor = buttonBareDisabledText,
  )

@Stable
@Composable
fun Theme.segmentedButton(): SegmentedButtonColors =
  SegmentedButtonDefaults.colors(
    activeContainerColor = checkboxToggleBackgroundSelected,
    activeContentColor = checkboxText,
    activeBorderColor = checkboxText,
    inactiveContainerColor = checkboxToggleBackground,
    inactiveContentColor = checkboxText.disabled,
    inactiveBorderColor = checkboxText.disabled,
    disabledActiveContainerColor = checkboxToggleBackgroundSelected.disabled,
    disabledActiveContentColor = checkboxText.disabled,
    disabledActiveBorderColor = Color.Transparent,
    disabledInactiveContainerColor = checkboxToggleBackground.disabled,
    disabledInactiveContentColor = checkboxText.disabled,
    disabledInactiveBorderColor = Color.Transparent,
  )

@Stable
@Composable
fun Theme.switch(): SwitchColors =
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

@Stable
@Composable
fun Theme.radioButton(): RadioButtonColors =
  RadioButtonDefaults.colors(
    selectedColor = checkboxToggleBackgroundSelected,
    unselectedColor = pageText.disabled,
    disabledSelectedColor = pageText.disabled,
    disabledUnselectedColor = pageText.disabled,
  )

@get:Stable
val Color.disabled: Color
  get() = copy(alpha = ALPHA_DISABLED)

@Stable fun Color.disabledIf(condition: Boolean): Color = if (condition) disabled else this

private const val ALPHA_DISABLED = 0.38f
