package actual.core.ui

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
