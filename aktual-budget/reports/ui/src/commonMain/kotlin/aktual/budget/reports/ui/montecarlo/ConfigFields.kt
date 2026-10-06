package aktual.budget.reports.ui.montecarlo

import aktual.budget.model.Amount
import aktual.budget.model.parseAmountInput
import aktual.budget.model.toInputText
import aktual.budget.reports.vm.clampAmount
import aktual.budget.reports.vm.numberInputText
import aktual.budget.reports.vm.parseNumberInput
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.checkbox
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.roundToLong

// One pot, income stream, contribution or phase. Upstream lays these out as table rows, which
// don't fit on a phone
@Composable
internal fun ItemCard(
  title: String,
  modifier: Modifier = Modifier,
  titleHelp: String? = null,
  leading: (@Composable RowScope.() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) =
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(CardShape)
        .background(colors.tableHeaderBackground, CardShape)
        .border(1.dp, colors.tableBorder, CardShape)
        .padding(ITEM_PADDING),
    verticalArrangement = Arrangement.spacedBy(FIELD_SPACING),
  ) {
    Row(verticalAlignment = CenterVertically) {
      leading?.invoke(this)
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LABEL_SPACING),
      ) {
        Text(
          modifier = Modifier.weight(1f, fill = false),
          text = title,
          style = typography.titleSmall,
          fontWeight = SemiBold,
          color = colors.pageText,
        )
        titleHelp?.let { HelpTooltip(it) }
      }
      actions()
    }
    content()
  }

@Composable
internal fun FieldRow(modifier: Modifier = Modifier, content: @Composable FlowRowScope.() -> Unit) =
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(FIELD_ROW_SPACING),
    verticalArrangement = Arrangement.spacedBy(FIELD_SPACING),
    content = content,
  )

@Composable
internal fun GroupHeadingWithHelp(text: String, help: String, modifier: Modifier = Modifier) =
  Row(
    modifier = modifier,
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(LABEL_SPACING),
  ) {
    GroupHeading(text)
    HelpTooltip(help)
  }

@Composable
internal fun LabeledField(
  label: String,
  modifier: Modifier = Modifier,
  help: String? = null,
  content: @Composable () -> Unit,
) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LABEL_SPACING)) {
    Row(
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(LABEL_SPACING),
    ) {
      Text(text = label, style = typography.labelMedium, color = colors.pageTextSubdued)
      help?.let { HelpTooltip(it) }
    }
    content()
  }

// A value the plan works out itself, shown where a field would otherwise be
@Composable
internal fun ReadOnlyValue(text: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier.heightIn(min = READ_ONLY_HEIGHT).wrapContentHeight(CenterVertically),
    text = text,
    style = typography.bodyMedium,
    color = colors.tableText,
  )

@Composable
internal fun LabeledCheckbox(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  help: String? = null,
) =
  Row(
    modifier = modifier,
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(LABEL_SPACING),
  ) {
    Row(
      modifier =
        Modifier.weight(1f, fill = false)
          .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
          .padding(vertical = LABEL_SPACING),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(CHECKBOX_SPACING),
    ) {
      Checkbox(checked = checked, onCheckedChange = null, colors = colors.checkbox())
      BodyText(label)
    }
    help?.let { HelpTooltip(it) }
  }

// Commits when the field loses focus rather than on every keystroke, since each change to the plan
// re-runs the simulation. Changes from outside show up while the field isn't being edited
@Composable
internal fun CommitTextField(
  text: String,
  onCommit: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String? = null,
  isEnabled: Boolean = true,
  keyboardType: KeyboardType = KeyboardType.Text,
) {
  val state = rememberTextFieldState(text)
  var isFocused by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val currentOnCommit by rememberUpdatedState(onCommit)

  // Also puts back the last value when the typed text was rejected
  SideEffect(text, isFocused) { if (!isFocused) state.setTextAndPlaceCursorAtEnd(text) }

  AktualTextField(
    modifier =
      modifier.onFocusChanged { focus ->
        if (isFocused && !focus.isFocused) currentOnCommit(state.text.toString())
        isFocused = focus.isFocused
      },
    state = state,
    placeholderText = placeholder,
    isEnabled = isEnabled,
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = Done),
    onKeyboardAction = { focusManager.clearFocus() },
  )
}

@Composable
internal fun NameField(
  name: String,
  placeholder: String,
  onCommit: (String) -> Unit,
  modifier: Modifier = Modifier,
) =
  CommitTextField(
    modifier = modifier.fillMaxWidth(),
    text = name,
    placeholder = placeholder,
    onCommit = { if (it != name) onCommit(it) },
  )

// Amounts are in minor units
@Composable
internal fun AmountField(
  amount: Double,
  onCommit: (Double) -> Unit,
  modifier: Modifier = Modifier,
) =
  CommitTextField(
    modifier = modifier.width(AMOUNT_WIDTH),
    text = Amount(amount.roundToLong()).toInputText(),
    placeholder = ZERO_PLACEHOLDER,
    keyboardType = Decimal,
    onCommit = { text ->
      val typed = parseAmountInput(text)?.let { clampAmount(it.toLong().toDouble()) }
      if (typed != null && typed != amount) onCommit(typed)
    },
  )

@Composable
internal fun IntField(
  value: Int,
  min: Int,
  max: Int,
  onCommit: (Int) -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
) =
  NumberField(
    modifier = modifier,
    value = value.toDouble(),
    min = min.toDouble(),
    max = max.toDouble(),
    roundToInteger = true,
    isEnabled = isEnabled,
    onCommit = { committed -> committed?.let { onCommit(it.roundToInt()) } },
  )

// Blank means the placeholder's default, like "now" or "end of plan"
@Composable
internal fun OptionalIntField(
  value: Int?,
  min: Int,
  max: Int,
  placeholder: String,
  onCommit: (Int?) -> Unit,
  modifier: Modifier = Modifier,
) =
  NumberField(
    modifier = modifier,
    value = value?.toDouble(),
    min = min.toDouble(),
    max = max.toDouble(),
    roundToInteger = true,
    allowEmpty = true,
    placeholder = placeholder,
    onCommit = { committed -> onCommit(committed?.roundToInt()) },
  )

// Shows a fraction as a percentage, with the bounds in percent
@Composable
internal fun PercentField(
  value: Double,
  onCommit: (Double) -> Unit,
  modifier: Modifier = Modifier,
  min: Double = 0.0,
  max: Double = PERCENT_SCALE.toDouble(),
  isEnabled: Boolean = true,
) =
  NumberField(
    modifier = modifier,
    value = value,
    min = min,
    max = max,
    scale = PERCENT_SCALE,
    isEnabled = isEnabled,
    onCommit = { committed -> committed?.let(onCommit) },
  )

@Composable
internal fun NumberField(
  value: Double?,
  min: Double,
  max: Double,
  onCommit: (Double?) -> Unit,
  modifier: Modifier = Modifier,
  scale: Int = 1,
  allowEmpty: Boolean = false,
  roundToInteger: Boolean = false,
  placeholder: String? = null,
  isEnabled: Boolean = true,
) =
  CommitTextField(
    modifier = modifier.width(NUMBER_WIDTH),
    text = numberInputText(value, scale),
    placeholder = placeholder,
    isEnabled = isEnabled,
    // Number keyboards don't all offer a minus sign
    keyboardType =
      when {
        min < 0 -> KeyboardType.Text
        roundToInteger -> KeyboardType.Number
        else -> KeyboardType.Decimal
      },
    onCommit = { text ->
      val input =
        parseNumberInput(
          text = text,
          min = min,
          max = max,
          scale = scale,
          allowEmpty = allowEmpty,
          roundToInteger = roundToInteger,
        )
      if (input is Valid && input.value != value) onCommit(input.value)
    },
  )

internal const val PERCENT_SCALE = 100
private const val ZERO_PLACEHOLDER = "0.00"
private val ITEM_PADDING = 12.dp
private val FIELD_SPACING = 10.dp
private val FIELD_ROW_SPACING = 16.dp
private val LABEL_SPACING = 4.dp
private val CHECKBOX_SPACING = 8.dp
private val READ_ONLY_HEIGHT = 48.dp
private val NUMBER_WIDTH = 120.dp
private val AMOUNT_WIDTH = 160.dp
