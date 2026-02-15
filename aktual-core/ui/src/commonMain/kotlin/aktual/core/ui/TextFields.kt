package aktual.core.ui

import aktual.budget.model.DateRangeType
import aktual.core.icons.Clear
import aktual.core.icons.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.ColorSchemeType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  colors: TextFieldColors? = null,
  clearable: Boolean = false,
  theme: Theme = LocalTheme.current,
) {
  val isFocused by interactionSource.collectIsFocusedAsState()
  val borderColor =
    if (isFocused) theme.formInputBorderSelected else theme.formInputBackgroundSelected

  var fieldModifier = modifier.border(1.dp, borderColor, shape)

  if (isFocused) {
    fieldModifier = fieldModifier.shadow(4.dp, shape, ambientColor = theme.formInputShadowSelected)
  }

  val clearButton: (@Composable () -> Unit)? =
    if (clearable && value.isNotEmpty()) {
      {
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
    placeholder = placeholderText?.let { { Text(text = it) } },
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
private fun ClearButton(tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(modifier = modifier, onClick = onClick) {
    Icon(imageVector = MaterialIcons.Clear, contentDescription = Strings.inputClear, tint = tint)
  }
}

@Composable
fun ExposedDropDownMenu(
  value: String,
  onValueChange: (String) -> Unit,
  options: ImmutableList<String>,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  ExposedDropDownMenu(
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
  var isExpanded by remember { mutableStateOf(false) }
  var selectedOption by remember { mutableStateOf(value) }

  ExposedDropdownMenuBox(
    modifier = modifier,
    expanded = isExpanded,
    onExpandedChange = { isExpanded = it },
  ) {
    TextField(
      modifier =
        Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
      readOnly = true,
      placeholderText = null,
      value = string(selectedOption),
      onValueChange = {},
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
      colors = theme.exposedDropDownMenu(),
      theme = theme,
    )

    ExposedDropdownMenu(
      modifier = Modifier.background(theme.menuItemBackground),
      expanded = isExpanded,
      onDismissRequest = { isExpanded = false },
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
            isExpanded = false
          },
        )
      }
    }
  }
}

private data class TextInputPreviewParams(
  val value: String,
  val placeholderText: String = "Placeholder",
  val isClearable: Boolean = false,
)

private class TextInputPreviewProvider :
  ThemedParameterProvider<TextInputPreviewParams>(
    TextInputPreviewParams(value = ""),
    TextInputPreviewParams(value = "I'm full"),
    TextInputPreviewParams(value = "I'm full", isClearable = true),
  )

@Preview
@Composable
private fun PreviewTextField(
  @PreviewParameter(TextInputPreviewProvider::class) params: ThemedParams<TextInputPreviewParams>
) =
  PreviewWithColorScheme(params.type) {
    TextField(
      value = params.data.value,
      onValueChange = {},
      placeholderText = params.data.placeholderText,
      clearable = params.data.isClearable,
    )
  }

@Preview
@Composable
private fun PreviewDropDownMenu(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType
) =
  PreviewWithColorScheme(type) {
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
private fun PreviewDropDownMenuForcedWidth(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType
) =
  PreviewWithColorScheme(type) {
    var value by remember { mutableStateOf("B") }
    val options = persistentListOf("A", "B", "C", "D")
    ExposedDropDownMenu(
      modifier = Modifier.width(100.dp),
      value = value,
      onValueChange = { newValue -> value = newValue },
      options = options,
    )
  }

@Preview
@Composable
@Suppress("ElseCaseInsteadOfExhaustiveWhen")
private fun PreviewDropDownMenuEnum(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType
) =
  PreviewWithColorScheme(type) {
    var value by remember { mutableStateOf(DateRangeType.YearToDate) }
    val options = DateRangeType.entries.toImmutableList()
    ExposedDropDownMenu(
      value = value,
      onValueChange = { newValue -> value = newValue },
      options = options,
      string = { t ->
        when (t) {
          DateRangeType.YearToDate -> "YTD"
          DateRangeType.LastYear -> "Last Year"
          DateRangeType.AllTime -> "All Time with some more text"
          else -> t.name
        }
      },
    )
  }
