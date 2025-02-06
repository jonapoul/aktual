package actual.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ActualTextField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholderText: String?,
  modifier: Modifier = Modifier,
  shape: Shape = ActualTextFieldShape,
  readOnly: Boolean = false,
  trailingIcon: @Composable (() -> Unit)? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  colors: TextFieldColors? = null,
  theme: Theme = LocalTheme.current,
) {
  val isFocused by interactionSource.collectIsFocusedAsState()
  val borderColor = if (isFocused) theme.formInputBorderSelected else theme.formInputBackgroundSelected

  var fieldModifier = modifier.border(1.dp, borderColor, shape)

  if (isFocused) {
    fieldModifier = fieldModifier.shadow(4.dp, shape, ambientColor = theme.formInputShadowSelected)
  }

  TextField(
    modifier = fieldModifier,
    value = value,
    placeholder = if (placeholderText == null) {
      null
    } else {
      { Text(text = placeholderText, fontFamily = ActualFontFamily) }
    },
    shape = shape,
    colors = colors ?: theme.textField(),
    readOnly = readOnly,
    trailingIcon = trailingIcon,
    interactionSource = interactionSource,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    onValueChange = onValueChange,
  )
}

@Composable
fun ActualExposedDropDownMenu(
  value: String,
  onValueChange: (String) -> Unit,
  options: ImmutableList<String>,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  var selectedOptionText by remember { mutableStateOf(value) }
  val theme = LocalTheme.current

  ExposedDropdownMenuBox(
    modifier = modifier,
    expanded = expanded,
    onExpandedChange = { expanded = it },
  ) {
    ActualTextField(
      modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
      readOnly = true,
      placeholderText = null,
      value = selectedOptionText,
      onValueChange = onValueChange,
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      colors = theme.exposedDropDownMenu(),
    )

    ExposedDropdownMenu(
      modifier = Modifier.background(theme.menuItemBackground),
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      val itemColors = theme.dropDownMenuItem()
      options.forEach { selectionOption ->
        DropdownMenuItem(
          text = { Text(selectionOption) },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
          colors = itemColors,
          onClick = {
            selectedOptionText = selectionOption
            onValueChange(selectionOption)
            expanded = false
          },
        )
      }
    }
  }
}

@Stable
@Composable
private fun Theme.textField(): TextFieldColors = TextFieldDefaults.colors(
  focusedTextColor = formInputText,
  unfocusedTextColor = formInputText,
  focusedPlaceholderColor = formInputTextPlaceholder,
  unfocusedPlaceholderColor = formInputTextPlaceholder,
  focusedIndicatorColor = Color.Transparent,
  unfocusedIndicatorColor = Color.Transparent,
  disabledIndicatorColor = Color.Transparent,
  focusedContainerColor = tableBackground,
  unfocusedContainerColor = tableBackground,
  cursorColor = formInputText,
)

@Stable
@Composable
private fun Theme.exposedDropDownMenu(): TextFieldColors = textField().copy(
  focusedTrailingIconColor = formInputText,
  unfocusedTrailingIconColor = formInputText,
)

@Stable
@Composable
private fun Theme.dropDownMenuItem(): MenuItemColors = MenuDefaults.itemColors().copy(
  textColor = formInputText,
)

private val ActualTextFieldShape = RoundedCornerShape(size = 4.dp)

@Preview
@Composable
private fun PreviewEmptyTextField() = PreviewActualColumn {
  ActualTextField(
    value = "",
    onValueChange = {},
    placeholderText = "I'm empty",
  )
}

@Preview
@Composable
private fun PreviewFilledTextField() = PreviewActualColumn {
  ActualTextField(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
  )
}

@Preview
@Composable
private fun PreviewDropDownMenu() = PreviewActualColumn {
  var value by remember { mutableStateOf("B") }
  val options = persistentListOf("A", "B", "C", "D")
  ActualExposedDropDownMenu(
    value = value,
    onValueChange = { value = it },
    options = options,
  )
}

@Preview
@Composable
private fun PreviewDropDownMenuForcedWidth() = PreviewActualColumn {
  var value by remember { mutableStateOf("B") }
  val options = persistentListOf("A", "B", "C", "D")
  ActualExposedDropDownMenu(
    modifier = Modifier.width(100.dp),
    value = value,
    onValueChange = { value = it },
    options = options,
  )
}
