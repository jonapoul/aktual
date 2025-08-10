package actual.core.ui

import actual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

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
      { Text(text = placeholderText) }
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
      contentDescription = Strings.inputClear,
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
  theme: Theme = LocalTheme.current,
) = ExposedDropDownMenu(
  value = value,
  onValueChange = onValueChange,
  options = options,
  modifier = modifier,
  theme = theme,
  string = { it },
)

@Composable
fun <T> ExposedDropDownMenu(
  value: T,
  onValueChange: (T) -> Unit,
  options: ImmutableList<T>,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  string: @Composable (T) -> String,
) {
  var expanded by remember { mutableStateOf(false) }
  var selectedOption by remember { mutableStateOf(value) }

  ExposedDropdownMenuBox(
    modifier = modifier,
    expanded = expanded,
    onExpandedChange = { expanded = it },
  ) {
    TextField(
      modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
      readOnly = true,
      placeholderText = null,
      value = string(selectedOption),
      onValueChange = {},
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      colors = theme.exposedDropDownMenu(),
      theme = theme,
    )

    ExposedDropdownMenu(
      modifier = Modifier.background(theme.menuItemBackground),
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      val itemColors = theme.dropDownMenuItem()
      options.forEach { o ->
        DropdownMenuItem(
          text = { Text(string(o)) },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
          colors = itemColors,
          onClick = {
            selectedOption = o
            onValueChange(o)
            expanded = false
          },
        )
      }
    }
  }
}
