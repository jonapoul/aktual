package aktual.core.ui

import aktual.budget.model.DateRangeType
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AktualExposedDropDownMenu(
  value: String,
  onValueChange: (String) -> Unit,
  options: ImmutableList<String>,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  theme: Theme = LocalTheme.current,
  textStyle: TextStyle = LocalTextStyle.current,
) =
  AktualExposedDropDownMenu(
    value = value,
    onValueChange = onValueChange,
    options = options,
    modifier = modifier,
    isEnabled = isEnabled,
    theme = theme,
    string = { it },
    textStyle = textStyle,
  )

@Composable
fun <T> AktualExposedDropDownMenu(
  value: T,
  onValueChange: (T) -> Unit,
  options: ImmutableList<T>,
  string: @Composable (T) -> String,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  theme: Theme = LocalTheme.current,
  textStyle: TextStyle = LocalTextStyle.current,
  contentPadding: PaddingValues = DROPDOWN_CONTENT_PADDING,
) {
  var isExpanded by remember { mutableStateOf(false) }
  var selectedOption by remember { mutableStateOf(value) }

  val selectedString = string(selectedOption)

  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current
  val trailingIconSize = LocalMinimumInteractiveComponentSize.current
  val layoutDirection = LocalLayoutDirection.current
  val contentWidth =
    remember(
      selectedString,
      textStyle,
      density,
      trailingIconSize,
      contentPadding,
      layoutDirection,
    ) {
      with(density) {
        val textWidth = textMeasurer.measure(selectedString, textStyle).size.width.toDp()
        val hPadding =
          contentPadding.calculateStartPadding(layoutDirection) +
            contentPadding.calculateEndPadding(layoutDirection)
        textWidth + trailingIconSize + hPadding
      }
    }

  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val borderColor =
    if (isFocused) theme.formInputBorderSelected else theme.formInputBackgroundSelected

  val dialogBlurState = LocalDialogBlurState.current

  ExposedDropdownMenuBox(
    modifier = modifier.then(Modifier.width(contentWidth)),
    expanded = isExpanded,
    onExpandedChange = { isExpanded = it },
  ) {
    BasicTextField(
      modifier =
        Modifier.menuAnchor(PrimaryNotEditable, enabled = isEnabled)
          .fillMaxWidth()
          .clip(TextFieldShape)
          .border(1.dp, borderColor, TextFieldShape)
          .onGloballyPositioned { coords ->
            if (isExpanded) {
              dialogBlurState.excludedFromBlur[ExposedDropdownMenuAnchorKey] = coords.boundsInRoot()
            }
          },
      value = selectedString,
      onValueChange = {},
      readOnly = true,
      enabled = isEnabled,
      singleLine = true,
      textStyle = textStyle.copy(color = theme.formInputText),
      interactionSource = interactionSource,
      decorationBox = { innerTextField ->
        TextFieldDefaults.DecorationBox(
          value = selectedString,
          innerTextField = innerTextField,
          enabled = isEnabled,
          singleLine = true,
          visualTransformation = VisualTransformation.None,
          interactionSource = interactionSource,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
          colors = theme.exposedDropDownMenu(),
          contentPadding = contentPadding,
        )
      },
    )

    ExposedDropdownMenu(
      modifier = Modifier.background(theme.menuBackground),
      expanded = isExpanded,
      onDismissRequest = { isExpanded = false },
      matchAnchorWidth = false,
    ) {
      DisposableEffect(Unit) {
        dialogBlurState.activeDialogCount++
        onDispose { dialogBlurState.activeDialogCount-- }
      }

      val itemColors = theme.dropDownMenuItem()
      options.fastForEach { o ->
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

private data object ExposedDropdownMenuAnchorKey

private val DROPDOWN_PADDING_H = 16.dp
private val DROPDOWN_PADDING_V = 12.dp

private val DROPDOWN_CONTENT_PADDING =
  TextFieldDefaults.contentPaddingWithoutLabel(
    start = DROPDOWN_PADDING_H,
    end = DROPDOWN_PADDING_H,
    top = DROPDOWN_PADDING_V,
    bottom = DROPDOWN_PADDING_V,
  )

@Preview
@Composable
private fun PreviewDropDownMenu(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    var value by remember { mutableStateOf("B") }
    val options = persistentListOf("A", "B", "C", "D")
    AktualExposedDropDownMenu(
      value = value,
      onValueChange = { newValue -> value = newValue },
      options = options,
    )
  }

@Preview
@Composable
private fun PreviewDropDownMenuForcedWidth(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    var value by remember { mutableStateOf("B") }
    val options = persistentListOf("A", "B", "C", "D")
    AktualExposedDropDownMenu(
      modifier = Modifier.width(100.dp),
      value = value,
      onValueChange = { newValue -> value = newValue },
      options = options,
    )
  }

@Preview
@Composable
@Suppress("ElseCaseInsteadOfExhaustiveWhen")
private fun PreviewDropDownMenuEnum(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    var value by remember { mutableStateOf(DateRangeType.YearToDate) }
    val options = DateRangeType.entries.toImmutableList()
    AktualExposedDropDownMenu(
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

@Preview
@Composable
private fun PreviewDropDownMenuLonger(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    var value by remember { mutableStateOf("Much longer item") }
    val options = persistentListOf("Much longer item", "A", "B")
    AktualExposedDropDownMenu(
      value = value,
      onValueChange = { newValue -> value = newValue },
      options = options,
    )
  }
