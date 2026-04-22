package aktual.core.ui

import aktual.core.theme.Theme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

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

@Composable
fun Theme.transparentTopAppBarColors(): TopAppBarColors =
  topAppBarColors(
    containerColor = Color.Transparent,
    navigationIconContentColor = pageText,
    actionIconContentColor = pageText,
    titleContentColor = pageText,
  )

fun Theme.dropDownMenuItem(): MenuItemColors =
  MenuItemColors(
    textColor = menuItemText,
    leadingIconColor = menuItemText,
    trailingIconColor = menuItemText,
    disabledTextColor = menuItemText.disabled,
    disabledLeadingIconColor = menuItemText.disabled,
    disabledTrailingIconColor = menuItemText.disabled,
  )

@Composable
fun Theme.textField(
  focusedContainer: Color = formInputBackground,
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
fun Theme.exposedDropDownMenu(): TextFieldColors =
  textField()
    .copy(focusedTrailingIconColor = formInputText, unfocusedTrailingIconColor = formInputText)

@Composable
fun Theme.primaryButton(isPressed: Boolean): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
    disabledContainerColor = buttonPrimaryDisabledBackground,
    contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
    disabledContentColor = buttonPrimaryDisabledText,
  )

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

@Composable
fun Theme.bareButton(isPressed: Boolean): ButtonColors =
  ButtonDefaults.buttonColors(
    containerColor = if (isPressed) buttonBareBackground else buttonBareBackgroundHover,
    disabledContainerColor = buttonBareDisabledBackground,
    contentColor = if (isPressed) buttonBareText else buttonBareTextHover,
    disabledContentColor = buttonBareDisabledText,
  )

@Composable
fun Theme.primaryIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
    disabledContainerColor = buttonPrimaryDisabledBackground,
    contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
    disabledContentColor = buttonPrimaryDisabledText,
  )

@Composable
fun Theme.normalIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonNormalBackground else buttonNormalBackgroundHover,
    disabledContainerColor = buttonNormalDisabledBackground,
    contentColor = if (isPressed) buttonNormalText else buttonNormalTextHover,
    disabledContentColor = buttonNormalDisabledText,
  )

@Composable
fun Theme.bareIconButton(isPressed: Boolean): IconButtonColors =
  IconButtonDefaults.filledIconButtonColors(
    containerColor = if (isPressed) buttonBareBackgroundHover else buttonBareBackground,
    disabledContainerColor = buttonBareDisabledBackground,
    contentColor = if (isPressed) buttonBareTextHover else buttonBareText,
    disabledContentColor = buttonBareDisabledText,
  )

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

@Composable
fun Theme.radioButton(): RadioButtonColors =
  RadioButtonDefaults.colors(
    selectedColor = checkboxToggleBackgroundSelected,
    unselectedColor = pageText.disabled,
    disabledSelectedColor = pageText.disabled,
    disabledUnselectedColor = pageText.disabled,
  )

@Composable
fun Theme.slider(): SliderColors =
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
fun Theme.checkbox(): CheckboxColors =
  CheckboxDefaults.colors(
    checkedCheckmarkColor = checkboxText,
    uncheckedCheckmarkColor = Color.Transparent,
    disabledCheckmarkColor = checkboxText,
    checkedBoxColor = checkboxBackgroundSelected,
    uncheckedBoxColor = Color.Transparent,
    disabledCheckedBoxColor = checkboxBackgroundSelected.disabled,
    disabledUncheckedBoxColor = Color.Transparent,
    disabledIndeterminateBoxColor = checkboxBackgroundSelected.disabled,
    checkedBorderColor = checkboxBorderSelected,
    uncheckedBorderColor = pageText.disabled,
    disabledBorderColor = checkboxBorderSelected.disabled,
    disabledUncheckedBorderColor = pageText.disabled,
    disabledIndeterminateBorderColor = checkboxBorderSelected.disabled,
  )

@Composable
fun Theme.listItem(): ListItemColors =
  ListItemDefaults.colors(
    containerColor = Color.Transparent,
    headlineColor = pageText,
    leadingIconColor = pageText,
    trailingIconColor = pageText,
  )

@get:Stable
val Color.disabled: Color
  get() = copy(alpha = ALPHA_DISABLED)

@Stable fun Color.disabledIf(condition: Boolean): Color = if (condition) disabled else this

private const val ALPHA_DISABLED = 0.38f
