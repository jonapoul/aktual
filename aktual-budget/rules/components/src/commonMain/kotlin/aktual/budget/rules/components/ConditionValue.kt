package aktual.budget.rules.components

import aktual.budget.model.Amount
import aktual.budget.model.evaluateAmountInput
import aktual.budget.model.toSignedInputText
import aktual.budget.rules.domain.ConditionEntity
import aktual.budget.rules.domain.ConditionValueKind
import aktual.budget.rules.domain.toDayOrNull
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Delete
import aktual.core.icons.SearchAlternate
import aktual.core.l10n.Strings
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.Chip
import aktual.core.ui.HostedSheet
import aktual.core.ui.scrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

@Composable
internal fun ConditionValue(
  kind: ConditionValueKind,
  value: JsonElement,
  names: ImmutableMap<String, String>,
  onChange: (JsonElement) -> Unit,
  onPickEntity: () -> Unit,
) {
  when (kind) {
    Entity -> EntityPicker(value, names, onPickEntity)

    EntityList -> EntityListPicker(value as JsonArray, names, onChange, onPickEntity)

    Text ->
      CommitTextField(
        modifier = Modifier.fillMaxWidth(),
        text = value.jsonPrimitive.content,
        placeholder = Strings.reportsFormulaQueryText,
        onCommit = { text -> onChange(JsonPrimitive(text)) },
      )

    // Number keyboards don't all offer a minus sign
    Money ->
      CommitTextField(
        modifier = Modifier.width(AMOUNT_WIDTH),
        text = Amount(value.jsonPrimitive.long).toSignedInputText(),
        keyboardType = Text,
        onCommit = { text ->
          evaluateAmountInput(text)?.let { amount -> onChange(JsonPrimitive(amount.toLong())) }
        },
      )

    Day ->
      CommitTextField(
        modifier = Modifier.fillMaxWidth(),
        text = value.jsonPrimitive.content,
        placeholder = Strings.reportsFormulaQueryDate,
        onCommit = { text ->
          val day = text.trim()
          if (day.isEmpty() || day.toDayOrNull() != null) onChange(JsonPrimitive(day))
        },
      )

    Flag ->
      AktualSlidingToggleButton(
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

@Composable
private fun EntityPicker(
  value: JsonElement,
  names: ImmutableMap<String, String>,
  onPick: () -> Unit,
) {
  val id = value.jsonPrimitive.contentOrNull
  AktualButton(
    modifier = Modifier.fillMaxWidth(),
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
) =
  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    val ids = value.map { it.jsonPrimitive.content }
    if (ids.isNotEmpty()) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ids.fastForEach { id ->
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

    AktualButton(
      kind = Tonal,
      modifier = Modifier.fillMaxWidth(),
      icon = AktualIcons.Add,
      text = Strings.reportsFormulaQueryChoose,
      onClick = onPick,
    )
  }

// Replaces the condition while open. Picking from a list adds to it rather than replacing it
@Composable
internal fun EntitySheet(
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
private val AMOUNT_WIDTH = 160.dp
