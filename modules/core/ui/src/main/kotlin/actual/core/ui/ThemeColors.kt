package actual.core.ui

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import my.nanihadesuka.compose.ScrollbarLayoutSide
import my.nanihadesuka.compose.ScrollbarSettings

@Stable
@Composable
fun Theme.topAppBarColors() = TopAppBarDefaults.topAppBarColors(
  containerColor = mobileHeaderBackground,
  titleContentColor = mobileHeaderText,
  navigationIconContentColor = mobileHeaderText,
)

@Stable
@Composable
fun Theme.dropDownMenuItem() = MenuDefaults.itemColors(
  textColor = formInputText,
)

@Stable
@Composable
fun Theme.scrollbarSettings() = ScrollbarSettings(
  side = ScrollbarLayoutSide.End,
  thumbUnselectedColor = scrollbar,
  thumbSelectedColor = scrollbarSelected,
)

@Stable
@Composable
fun Theme.slider() = SliderDefaults.colors(
  thumbColor = sliderThumb,
  activeTrackColor = sliderActiveTrack,
  activeTickColor = sliderActiveTick,
  inactiveTrackColor = sliderInactiveTrack,
  inactiveTickColor = sliderInactiveTick,
)

@Stable
@Composable
fun Theme.textField(
  focusedContainer: Color = formInputBackground,
  unfocusedContainer: Color = focusedContainer,
  text: Color = formInputText,
  textPlaceholder: Color = formInputTextPlaceholder,
  icon: Color = formInputText,
): TextFieldColors = TextFieldDefaults.colors(
  focusedTextColor = text,
  unfocusedTextColor = text,
  focusedPlaceholderColor = textPlaceholder,
  unfocusedPlaceholderColor = textPlaceholder,
  focusedLabelColor = textPlaceholder,
  unfocusedLabelColor = textPlaceholder,
  focusedIndicatorColor = Color.Transparent,
  unfocusedIndicatorColor = Color.Transparent,
  disabledIndicatorColor = Color.Transparent,
  focusedContainerColor = focusedContainer,
  unfocusedContainerColor = unfocusedContainer,
  cursorColor = text,
  focusedLeadingIconColor = icon,
  unfocusedLeadingIconColor = icon,
  focusedTrailingIconColor = icon,
  unfocusedTrailingIconColor = icon,
)

@Stable
@Composable
fun Theme.exposedDropDownMenu(): TextFieldColors = textField().copy(
  focusedTrailingIconColor = formInputText,
  unfocusedTrailingIconColor = formInputText,
)

@Stable
@Composable
fun Theme.primaryButton(isPressed: Boolean) = ButtonDefaults.buttonColors(
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
) = ButtonDefaults.buttonColors(
  containerColor = if (isPressed) containerPressed else container,
  disabledContainerColor = containerDisabled,
  contentColor = if (isPressed) textPressed else text,
  disabledContentColor = textDisabled,
)

@Stable
@Composable
fun Theme.bareButton(isPressed: Boolean) = ButtonDefaults.buttonColors(
  containerColor = if (isPressed) buttonBareBackground else buttonBareBackgroundHover,
  disabledContainerColor = buttonBareDisabledBackground,
  contentColor = if (isPressed) buttonBareText else buttonBareTextHover,
  disabledContentColor = buttonBareDisabledText,
)

@Stable
@Composable
fun Theme.primaryIconButton(isPressed: Boolean) = IconButtonDefaults.filledIconButtonColors(
  containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
  disabledContainerColor = buttonPrimaryDisabledBackground,
  contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
  disabledContentColor = buttonPrimaryDisabledText,
)

@Stable
@Composable
fun Theme.normalIconButton(isPressed: Boolean) = IconButtonDefaults.filledIconButtonColors(
  containerColor = if (isPressed) buttonNormalBackground else buttonNormalBackgroundHover,
  disabledContainerColor = buttonNormalDisabledBackground,
  contentColor = if (isPressed) buttonNormalText else buttonNormalTextHover,
  disabledContentColor = buttonNormalDisabledText,
)

@Stable
@Composable
fun Theme.bareIconButton(isPressed: Boolean) = IconButtonDefaults.filledIconButtonColors(
  containerColor = if (isPressed) buttonBareBackgroundHover else buttonBareBackground,
  disabledContainerColor = buttonBareDisabledBackground,
  contentColor = if (isPressed) buttonBareTextHover else buttonBareText,
  disabledContentColor = buttonBareDisabledText,
)
