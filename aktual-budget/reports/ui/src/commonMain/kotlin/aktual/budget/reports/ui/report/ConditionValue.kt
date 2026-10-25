package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.montecarlo.CommitTextField
import aktual.budget.reports.ui.montecarlo.NumberField
import aktual.budget.reports.vm.ConditionValueKind
import aktual.budget.reports.vm.QueryEntity
import aktual.budget.reports.vm.toDayOrNull
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.SearchAlternate
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.NormalTextButton
import aktual.core.ui.RemovableChip
import aktual.core.ui.TonalButton
import aktual.core.ui.scrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlin.math.roundToLong
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
  entities: ImmutableList<QueryEntity>,
  names: ImmutableMap<String, String>,
  onChange: (JsonElement) -> Unit,
) {
  when (kind) {
    Entity -> EntityPicker(value, entities, names, onChange)

    EntityList -> EntityListPicker(value as JsonArray, entities, names, onChange)

    Text ->
      CommitTextField(
        modifier = Modifier.fillMaxWidth(),
        text = value.jsonPrimitive.content,
        placeholder = Strings.reportsFormulaQueryText,
        onCommit = { text -> onChange(JsonPrimitive(text)) },
      )

    Money ->
      NumberField(
        value = value.jsonPrimitive.long / CENTS,
        min = -MAX_AMOUNT,
        max = MAX_AMOUNT,
        onCommit = { amount -> onChange(JsonPrimitive(((amount ?: 0.0) * CENTS).roundToLong())) },
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
  entities: ImmutableList<QueryEntity>,
  names: ImmutableMap<String, String>,
  onChange: (JsonElement) -> Unit,
) {
  var showSheet by remember { mutableStateOf(false) }
  val id = value.jsonPrimitive.contentOrNull
  NormalTextButton(
    modifier = Modifier.fillMaxWidth(),
    text = if (id == null) Strings.reportsFormulaQueryNothing else names.nameOf(id),
    onClick = { showSheet = true },
  )

  if (showSheet) {
    EntitySheet(
      selected = entities.firstOrNull { it.id == id } ?: NOTHING,
      options = remember(entities) { (listOf(NOTHING) + entities).toImmutableList() },
      onSelect = { entity ->
        onChange(if (entity == NOTHING) JsonNull else JsonPrimitive(entity.id))
      },
      onDismiss = { showSheet = false },
    )
  }
}

// Chosen entities as removable chips
@Composable
private fun EntityListPicker(
  value: JsonArray,
  entities: ImmutableList<QueryEntity>,
  names: ImmutableMap<String, String>,
  onChange: (JsonElement) -> Unit,
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
          RemovableChip(
            text = name,
            removeDescription = Strings.reportsFormulaQueryRemove(name),
            onRemove = { onChange(JsonArray(ids.minus(id).map(::JsonPrimitive))) },
          )
        }
      }
    }

    var showSheet by remember { mutableStateOf(false) }
    TonalButton(
      modifier = Modifier.fillMaxWidth(),
      icon = AktualIcons.Add,
      text = Strings.reportsFormulaQueryChoose,
      onClick = { showSheet = true },
    )

    if (showSheet) {
      EntitySheet(
        selected = NOTHING,
        options = remember(entities, ids) { entities.filter { it.id !in ids }.toImmutableList() },
        onSelect = { entity -> onChange(JsonArray((ids + entity.id).map(::JsonPrimitive))) },
        onDismiss = { showSheet = false },
      )
    }
  }

// A list sheet with a search field, since there can be hundreds of payees
@Composable
private fun EntitySheet(
  selected: QueryEntity,
  options: ImmutableList<QueryEntity>,
  onSelect: (QueryEntity) -> Unit,
  onDismiss: () -> Unit,
) =
  AktualModalBottomSheet(onDismissRequest = onDismiss) {
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
      items(filtered, key = QueryEntity::id) { entity ->
        BottomSheetListItem(
          label = if (entity == NOTHING) nothing else entity.name,
          isSelected = entity == selected,
          onClick = {
            onSelect(entity)
            onDismiss()
          },
        )
      }
    }
  }

@Composable
internal fun ImmutableMap<String, String>.nameOf(id: String): String =
  get(id) ?: Strings.reportsFormulaQueryUnknown

private val NOTHING = QueryEntity(id = "", name = "")
private val FLAGS = persistentListOf(true, false)
private const val CENTS = 100.0
private const val MAX_AMOUNT = 1_000_000_000.0
