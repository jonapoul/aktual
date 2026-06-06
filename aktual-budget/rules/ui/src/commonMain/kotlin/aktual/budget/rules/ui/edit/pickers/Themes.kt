package aktual.budget.rules.ui.edit.pickers

import aktual.core.theme.Colors
import aktual.core.ui.textField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

@Composable
internal fun Colors.pickerField(): TextFieldColors =
  textField(
    focusedContainer = buttonNormalBackground,
    border = buttonNormalBorder,
    textPlaceholder = formInputTextPlaceholder,
  )
