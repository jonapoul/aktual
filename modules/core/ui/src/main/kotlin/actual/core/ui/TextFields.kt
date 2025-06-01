package actual.core.ui

import actual.core.res.CoreStrings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
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
fun TextField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholderText: String?,
  modifier: Modifier = Modifier,
  shape: Shape = TextFieldShape,
  readOnly: Boolean = false,
  enabled: Boolean = true,
  singleLine: Boolean = false,
  leadingIcon: ComposableLambda? = null,
  trailingIcon: ComposableLambda? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  colors: TextFieldColors? = null,
  clearable: Boolean = false,
  theme: Theme = LocalTheme.current,
) {
  val isFocused by interactionSource.collectIsFocusedAsState()
  val borderColor = if (isFocused) theme.formInputBorderSelected else theme.formInputBackgroundSelected

  var fieldModifier = modifier.border(1.dp, borderColor, shape)

  if (isFocused) {
    fieldModifier = fieldModifier.shadow(4.dp, shape, ambientColor = theme.formInputShadowSelected)
  }

  val clearButton = if (clearable && value.isNotEmpty()) {
    ComposableLambda {
      ClearButton(
        tint = colors?.focusedTrailingIconColor ?: theme.pageText,
        onClick = { onValueChange("") },
      )
    }
  } else {
    null
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
    enabled = enabled,
    singleLine = singleLine,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon ?: clearButton,
    interactionSource = interactionSource,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    onValueChange = onValueChange,
  )
}

@Composable
private fun ClearButton(
  tint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  IconButton(
    modifier = modifier,
    onClick = onClick,
  ) {
    Icon(
      imageVector = Icons.Default.Clear,
      contentDescription = CoreStrings.inputClear,
      tint = tint,
    )
  }
}

@Composable
fun ExposedDropDownMenu(
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
    TextField(
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

@Preview
@Composable
private fun PreviewEmptyTextField() = PreviewColumn {
  TextField(
    value = "",
    onValueChange = {},
    placeholderText = "I'm empty",
  )
}

@Preview
@Composable
private fun PreviewFilledTextField() = PreviewColumn {
  TextField(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
  )
}

@Preview
@Composable
private fun PreviewFilledClearable() = PreviewColumn {
  TextField(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
    clearable = true,
  )
}

@Preview
@Composable
private fun PreviewDropDownMenu() = PreviewColumn {
  var value by remember { mutableStateOf("B") }
  val options = persistentListOf("A", "B", "C", "D")
  ExposedDropDownMenu(
    value = value,
    onValueChange = { newValue -> value = newValue },
    options = options,
  )
}

@Preview
@Composable
private fun PreviewDropDownMenuForcedWidth() = PreviewColumn {
  var value by remember { mutableStateOf("B") }
  val options = persistentListOf("A", "B", "C", "D")
  ExposedDropDownMenu(
    modifier = Modifier.width(100.dp),
    value = value,
    onValueChange = { newValue -> value = newValue },
    options = options,
  )
}
