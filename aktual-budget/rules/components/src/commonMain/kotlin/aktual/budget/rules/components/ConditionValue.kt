package aktual.budget.rules.components

import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.evaluateAmountInput
import aktual.budget.model.toInputText
import aktual.budget.rules.domain.ConditionEntity
import aktual.budget.rules.domain.ConditionValueKind
import aktual.budget.rules.domain.DAY_RANGE_END
import aktual.budget.rules.domain.DAY_RANGE_START
import aktual.budget.rules.domain.dayRange
import aktual.budget.rules.domain.toDayOrNull
import aktual.budget.rules.domain.toJsonElement
import aktual.budget.rules.domain.toRecurConfigOrNull
import aktual.budget.schedules.components.RecurConfigFields
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.Delete
import aktual.core.icons.SearchAlternate
import aktual.core.icons.Subtract
import aktual.core.l10n.Strings
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualDatePickerDialog
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AnimatedFlowRow
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.Chip
import aktual.core.ui.DialogTextButton
import aktual.core.ui.HostedSheet
import aktual.core.ui.LocalCurrencyConfig
import aktual.core.ui.formatted
import aktual.core.ui.scrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

@Composable
fun ConditionValue(
  kind: ConditionValueKind,
  value: JsonElement,
  names: ImmutableMap<String, String>,
  onChange: (JsonElement) -> Unit,
  onPickEntity: () -> Unit,
  modifier: Modifier = Modifier,
  isSigned: Boolean = true,
) {
  when (kind) {
    Entity -> EntityPicker(value = value, names = names, onPick = onPickEntity, modifier = modifier)

    EntityList ->
      EntityListPicker(
        value = value as JsonArray,
        names = names,
        onChange = onChange,
        onPick = onPickEntity,
        modifier = modifier,
      )

    TextList -> TextListEditor(value = value as JsonArray, onChange = onChange, modifier = modifier)

    Text ->
      CommitTextField(
        modifier = modifier.fillMaxWidth(),
        text = value.jsonPrimitive.content,
        placeholder = Strings.reportsFormulaQueryText,
        onCommit = { text -> onChange(JsonPrimitive(text)) },
      )

    Money ->
      MoneyField(value = value, onChange = onChange, isSigned = isSigned, modifier = modifier)

    Day -> DayField(value = value, onChange = onChange, modifier = modifier)

    DayRange -> {
      val range = value.jsonObject
      fun bound(key: String) = range[key] ?: JsonPrimitive("")
      Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        DayField(
          value = bound(DAY_RANGE_START),
          onChange = { onChange(dayRange(it, bound(DAY_RANGE_END))) },
        )
        DayField(
          value = bound(DAY_RANGE_END),
          onChange = { onChange(dayRange(bound(DAY_RANGE_START), it)) },
        )
      }
    }

    Recurrence ->
      value.toRecurConfigOrNull()?.let { config ->
        RecurConfigFields(
          modifier = modifier,
          config = config,
          onConfigChange = { onChange(it.toJsonElement()) },
        )
      }

    Flag ->
      AktualSlidingToggleButton(
        modifier = modifier,
        selected = value.jsonPrimitive.content.toBoolean(),
        options = FLAGS,
        onSelect = { flag -> onChange(JsonPrimitive(flag)) },
        string = { flag ->
          if (flag) Strings.reportsFormulaQueryTrue else Strings.reportsFormulaQueryFalse
        },
      )

    NoValue -> Unit
  }
}

// Shows the unsigned amount, with a sign toggle since number keyboards don't all offer a minus.
// Without [isSigned] there's no toggle and the amount stays positive
@Composable
private fun MoneyField(
  value: JsonElement,
  onChange: (JsonElement) -> Unit,
  isSigned: Boolean,
  modifier: Modifier = Modifier,
) {
  val amount = Amount(value.jsonPrimitive.long)
  // Zero has no sign of its own, so keep the last one picked
  var isZeroNegative by rememberSaveable { mutableStateOf(true) }
  val isNegative = isSigned && if (amount == Zero) isZeroNegative else amount < Zero

  fun commit(newAmount: Amount, negative: Boolean) {
    if (newAmount == Zero) isZeroNegative = negative
    onChange(JsonPrimitive(newAmount.toLong()))
  }

  val currency = LocalCurrencyConfig.current
  val symbol = currency.currency.symbol.takeIf { it.isNotEmpty() }
  CommitTextField(
    modifier = modifier.fillMaxWidth(),
    text = amount.toInputText(),
    keyboardType = Decimal,
    onCommit = { text ->
      evaluateAmountInput(text)?.let { typed ->
        val signed =
          when {
            isNegative -> -typed
            !isSigned && typed < Zero -> -typed
            else -> typed
          }
        commit(signed, if (signed == Zero) isNegative else signed < Zero)
      }
    },
    leadingIcon = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (isSigned) {
          AktualIconButton(
            imageVector = if (isNegative) AktualIcons.Subtract else AktualIcons.Add,
            contentDescription =
              if (isNegative) {
                Strings.reportsFormulaQueryMakePositive
              } else {
                Strings.reportsFormulaQueryMakeNegative
              },
            onClick = { commit(-amount, !isNegative) },
          )
        }
        if (symbol != null && currency.position == BeforeAmount) CurrencySymbol(symbol)
      }
    },
    trailingIcon =
      symbol?.takeIf { currency.position == AfterAmount }?.let { { CurrencySymbol(it) } },
  )
}

// A date picker, which also holds dates outside its default year range
@Composable
private fun DayField(
  value: JsonElement,
  onChange: (JsonElement) -> Unit,
  modifier: Modifier = Modifier,
) {
  val text = value.jsonPrimitive.content
  val day = text.toDayOrNull()
  var showDialog by rememberSaveable { mutableStateOf(false) }

  AktualButton(
    modifier = modifier.fillMaxWidth(),
    // An invalid stored date shows as is, so it doesn't look unset
    text = day?.formatted() ?: text.ifEmpty { Strings.reportsFormulaQueryDate },
    icon = AktualIcons.Calendar3,
    onClick = { showDialog = true },
  )

  if (showDialog) {
    val years = DatePickerDefaults.YearRange
    val year = day?.year
    val state =
      rememberDatePickerState(
        initialSelectedDate = day?.toJavaLocalDate(),
        yearRange = if (year == null) years else minOf(years.first, year)..maxOf(years.last, year),
      )
    AktualDatePickerDialog(
      onDismissRequest = { showDialog = false },
      confirmButton = {
        DialogTextButton(
          kind = Primary,
          text = Strings.reportsFormulaQueryDateOk,
          isEnabled = state.selectedDateMillis != null,
          onClick = {
            state.getSelectedDate()?.let {
              onChange(JsonPrimitive(it.toKotlinLocalDate().toString()))
            }
            showDialog = false
          },
        )
      },
      dismissButton = {
        Row {
          if (text.isNotEmpty()) {
            DialogTextButton(
              text = Strings.reportsFormulaQueryDateClear,
              onClick = {
                onChange(JsonPrimitive(""))
                showDialog = false
              },
            )
          }
          DialogTextButton(
            text = Strings.reportsFormulaQueryDateCancel,
            onClick = { showDialog = false },
          )
        }
      },
      content = { DatePicker(state = state) },
    )
  }
}

@Composable
private fun CurrencySymbol(string: String, modifier: Modifier = Modifier) {
  MaterialText(
    modifier = modifier,
    text = string,
    style = typography.bodyLarge,
    color = colors.textTertiary,
  )
}

@Composable
private fun EntityPicker(
  value: JsonElement,
  names: ImmutableMap<String, String>,
  onPick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val id = value.jsonPrimitive.contentOrNull
  AktualButton(
    modifier = modifier.fillMaxWidth(),
    text = if (id == null) Strings.reportsFormulaQueryNothing else names.nameOf(id),
    onClick = onPick,
  )
}

// Chosen entities as removable chips
@Composable
private fun EntityListPicker(
  value: JsonArray,
  names: ImmutableMap<String, String>,
  onChange: (JsonElement) -> Unit,
  onPick: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    val ids = value.map { it.jsonPrimitive.content }
    if (ids.isNotEmpty()) {
      AnimatedFlowRow {
        ids.fastForEach { id ->
          item(key = id) {
            val name = names.nameOf(id)
            Chip(
              text = name,
              trailingIcon = AktualIcons.Delete,
              trailingIconDescription = Strings.reportsFormulaQueryRemove(name),
              onClick = { onChange(JsonArray(ids.minus(id).map(::JsonPrimitive))) },
            )
          }
        }
      }
    }

    AktualButton(
      kind = Tonal,
      modifier = Modifier.fillMaxWidth(),
      icon = AktualIcons.Add,
      text = Strings.reportsFormulaQueryChoose,
      onClick = onPick,
    )
  }

// Typed strings as removable chips, with a field that adds another on done
@Composable
private fun TextListEditor(
  value: JsonArray,
  onChange: (JsonElement) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    val texts = value.map { it.jsonPrimitive.content }
    if (texts.isNotEmpty()) {
      AnimatedFlowRow {
        texts.fastForEach { text ->
          item(key = text) {
            Chip(
              text = text,
              trailingIcon = AktualIcons.Delete,
              trailingIconDescription = Strings.reportsFormulaQueryRemove(text),
              onClick = { onChange(JsonArray(texts.minus(text).map(::JsonPrimitive))) },
            )
          }
        }
      }
    }

    val state = rememberTextFieldState()
    AktualTextField(
      modifier = Modifier.fillMaxWidth(),
      state = state,
      placeholderText = Strings.reportsFormulaQueryAddText,
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = Done),
      onKeyboardAction = {
        val text = state.text.toString().trim()
        if (text.isNotEmpty() && text !in texts) {
          onChange(JsonArray((texts + text).map(::JsonPrimitive)))
        }
        state.clearText()
      },
    )
  }

// Replaces the condition while open. Picking from a list adds to it rather than replacing it
@Composable
fun EntitySheet(
  kind: ConditionValueKind,
  value: JsonElement,
  entities: ImmutableList<ConditionEntity>,
  onChange: (JsonElement) -> Unit,
  onDismiss: () -> Unit,
) {
  if (kind == EntityList) {
    val ids = (value as JsonArray).map { it.jsonPrimitive.content }
    EntitySheet(
      selected = NOTHING,
      options = remember(entities, ids) { entities.filter { it.id !in ids }.toImmutableList() },
      onSelect = { entity -> onChange(JsonArray((ids + entity.id).map(::JsonPrimitive))) },
      onDismiss = onDismiss,
    )
  } else {
    val id = value.jsonPrimitive.contentOrNull
    EntitySheet(
      selected = entities.firstOrNull { it.id == id } ?: NOTHING,
      options = remember(entities) { (listOf(NOTHING) + entities).toImmutableList() },
      onSelect = { entity ->
        onChange(if (entity == NOTHING) JsonNull else JsonPrimitive(entity.id))
      },
      onDismiss = onDismiss,
    )
  }
}

// A list sheet with a search field, since there can be hundreds of payees
@Composable
private fun EntitySheet(
  selected: ConditionEntity,
  options: ImmutableList<ConditionEntity>,
  onSelect: (ConditionEntity) -> Unit,
  onDismiss: () -> Unit,
) =
  HostedSheet(onDismiss = onDismiss) {
    val search = rememberTextFieldState()
    val query = search.text.toString().trim()
    val nothing = Strings.reportsFormulaQueryNothing
    val filtered =
      remember(options, query) {
        options.filter { it == NOTHING || it.name.contains(query, ignoreCase = true) }
      }

    AktualTextField(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      state = search,
      placeholderText = Strings.reportsFormulaQuerySearch,
      singleLine = true,
      clearable = true,
      leadingIcon = { Icon(AktualIcons.SearchAlternate, contentDescription = null) },
    )

    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      items(filtered, key = ConditionEntity::id) { entity ->
        BottomSheetListItem(
          label = if (entity == NOTHING) nothing else entity.name,
          isSelected = entity == selected,
          onClick = { close { onSelect(entity) } },
        )
      }
    }
  }

@Composable
fun ImmutableMap<String, String>.nameOf(id: String): String =
  get(id) ?: Strings.reportsFormulaQueryUnknown

private val NOTHING = ConditionEntity(id = "", name = "")
private val FLAGS = persistentListOf(true, false)
