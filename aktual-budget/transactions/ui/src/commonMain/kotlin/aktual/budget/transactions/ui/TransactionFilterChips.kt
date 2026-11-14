package aktual.budget.transactions.ui

import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import aktual.budget.model.Field
import aktual.budget.rules.components.ConditionSheet
import aktual.budget.rules.components.label
import aktual.budget.rules.components.valueLabel
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.ConditionEntity
import aktual.budget.rules.domain.ConditionEntityLists
import aktual.budget.rules.domain.FILTER_FIELDS
import aktual.budget.rules.domain.newFilterCondition
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.icons.Delete
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.CardColumn
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.HostedSheet
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.Rectangle
import aktual.core.ui.SheetHost
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.JsonPrimitive

// One chip per condition, tapped to edit and removed by its icon
@Composable
internal fun TransactionFilterChips(
  conditions: ConditionSet,
  entities: ConditionEntities?,
  onAction: OnAction<Action>,
  modifier: Modifier = Modifier,
) {
  var editing by rememberSaveable { mutableStateOf<Int?>(null) }
  var adding by rememberSaveable { mutableStateOf(false) }
  val names = entities?.names ?: persistentMapOf()

  CardColumn(
    modifier = modifier.fillMaxWidth(),
    shape = Rectangle,
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
  ) {
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(2.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      if (conditions.conditions.size > 1) {
        val isAny = conditions.op == Or
        Chip(
          text =
            if (isAny) Strings.transactionsFilterMatchAny else Strings.transactionsFilterMatchAll,
          colors = ChipDefaults.selectableColors(isSelected = true),
          trailingIcon = AktualIcons.CheveronDown,
          onClick = { onAction(Action.SetConditionsOp(if (isAny) And else Or)) },
          role = Button,
        )
      }

      conditions.conditions.fastForEachIndexed { index, condition ->
        ConditionChip(
          condition = condition,
          names = names,
          onClick = { editing = index },
          onRemove = { onAction(Action.RemoveCondition(index)) },
        )
      }

      Chip(
        text = Strings.transactionsFilterAdd,
        colors = ChipDefaults.selectableColors(isSelected = false),
        trailingIcon = AktualIcons.Add,
        onClick = { adding = true },
        role = Button,
      )
    }
  }

  if (entities == null) return

  if (adding) {
    AddFilterSheet(
      entities = entities,
      onAdd = { condition -> onAction(Action.AddCondition(condition)) },
      onDismiss = { adding = false },
    )
  }

  editing?.let { index ->
    conditions.conditions.getOrNull(index)?.let { condition ->
      ConditionSheet(
        condition = condition,
        data = entities,
        onChange = { changed -> onAction(Action.EditCondition(index, changed)) },
        onDelete = {
          editing = null
          onAction(Action.RemoveCondition(index))
        },
        onDismiss = { editing = null },
      )
    }
  }
}

// Picks a field, then edits the new condition on a page of the same sheet, with back returning
// to the fields. Only Done adds it, so the list isn't narrowed while it's still being edited
@Composable
private fun AddFilterSheet(
  entities: ConditionEntities,
  onAdd: (Condition) -> Unit,
  onDismiss: () -> Unit,
) {
  var draft by remember { mutableStateOf<Condition?>(null) }
  SheetHost(onDismiss = onDismiss) { host ->
    val condition = draft
    if (condition == null) {
      HostedSheet(onDismiss = host.dismiss) {
        FieldList(onSelect = { field -> draft = newFilterCondition(field) })
      }
    } else {
      ConditionSheet(
        condition = condition,
        data = entities,
        onChange = { changed -> draft = changed },
        onDelete = {},
        onDismiss = { draft = null },
        onAdd = { draft?.let(onAdd) },
      )
    }
  }
}

@Composable
private fun ConditionChip(
  condition: Condition,
  names: ImmutableMap<String, String>,
  onClick: () -> Unit,
  onRemove: () -> Unit,
) {
  val label =
    listOfNotNull(
        condition.field.label().replaceFirstChar { it.titlecase() },
        condition.operator.label(),
        condition.valueLabel(names),
      )
      .joinToString(separator = " ")

  Chip(colors = ChipDefaults.selectableColors(isSelected = true), onClick = onClick) {
    Text(
      modifier = Modifier.weight(1f, fill = false),
      text = label,
      maxLines = 1,
      overflow = Ellipsis,
    )
    Icon(
      modifier = Modifier.size(ChipDefaults.IconSize).clickable(role = Button, onClick = onRemove),
      imageVector = AktualIcons.Delete,
      contentDescription = Strings.transactionsFilterRemove(label),
    )
  }
}

@Composable
private fun FieldList(onSelect: (Field) -> Unit) =
  Column(modifier = Modifier.verticalScrollWithBar()) {
    Text(
      modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp),
      text = Strings.transactionsFilterAdd,
      style = typography.headlineSmall,
    )
    FILTER_FIELDS.fastForEach { field ->
      BottomSheetListItem(
        label = field.label().replaceFirstChar { it.titlecase() },
        isSelected = false,
        onClick = { onSelect(field) },
      )
    }
  }

@Preview
@Composable
private fun PreviewTransactionFilterChips(
  @PreviewParameter(FilterChipsProvider::class) params: ColorParams<ConditionSet>,
) =
  PreviewWithColorParams(params) {
    TransactionFilterChips(conditions = this, entities = PREVIEW_ENTITIES, onAction = {})
  }

private class FilterChipsProvider :
  ColorParamsProvider<ConditionSet>(
    ConditionSet(emptyList()),
    ConditionSet(listOf(Condition(Payee, Is, JsonPrimitive(PREVIEW_PAYEE)))),
    ConditionSet(
      listOf(
        Condition(Payee, Is, JsonPrimitive(PREVIEW_PAYEE)),
        Condition(Notes, Contains, JsonPrimitive("a deliberately long note to check truncation")),
        Condition(Amount, GreaterThan, JsonPrimitive(PREVIEW_AMOUNT)),
      ),
      op = Or,
    ),
  )

private const val PREVIEW_PAYEE = "payee-1"
private const val PREVIEW_AMOUNT = -5000

private val PREVIEW_ENTITIES =
  ConditionEntityLists(
    accounts = persistentListOf(),
    categories = persistentListOf(),
    categoryGroups = persistentListOf(),
    payees = persistentListOf(ConditionEntity(PREVIEW_PAYEE, "Tesco")),
    names = persistentMapOf(PREVIEW_PAYEE to "Tesco"),
  )
