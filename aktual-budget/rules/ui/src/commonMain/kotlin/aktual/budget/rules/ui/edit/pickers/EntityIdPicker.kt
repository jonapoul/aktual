package aktual.budget.rules.ui.edit.pickers

import aktual.budget.model.Field
import aktual.budget.rules.ui.LocalEntityListFetcher
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.vm.EntityListFetcher
import aktual.budget.rules.vm.EntitySummary
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.ListBottomSheet
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntityIdPicker(
  field: Field,
  value: JsonElement,
  isEnabled: Boolean,
  onValueChange: (JsonElement) -> Unit,
  fetchEntities: suspend (EntityListFetcher) -> ImmutableList<EntitySummary>,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val nameFetcher = LocalNameFetcher.current
  val entityListFetcher = LocalEntityListFetcher.current
  val selectedId = (value as? JsonPrimitive)?.content?.takeUnless { it.isBlank() }

  val nameFlow =
    remember(nameFetcher, field, selectedId) {
      if (selectedId == null) flowOf(null) else nameFetcher.name(field, selectedId)
    }
  val selectedName by nameFlow.collectAsStateWithLifecycle(initialValue = null)

  var showSheet by remember { mutableStateOf(false) }
  var entities by remember { mutableStateOf<ImmutableList<EntitySummary>?>(null) }
  val sheetState = rememberModalBottomSheetState()

  val fetchEntitiesState by rememberUpdatedState(fetchEntities)
  LaunchedEffect(showSheet, entityListFetcher) {
    if (showSheet && entities == null) {
      entities = fetchEntitiesState(entityListFetcher)
    }
  }

  Box(modifier = modifier) {
    TextField(
      modifier = Modifier.fillMaxWidth().border(Dp.Hairline, theme.buttonNormalBorder),
      value = selectedName.orEmpty(),
      onValueChange = {},
      placeholder = { Text(Strings.editRuleConditionNothing, style = AktualTypography.bodySmall) },
      readOnly = true,
      enabled = isEnabled,
      singleLine = true,
      textStyle = AktualTypography.bodySmall,
      trailingIcon = {
        if (selectedId != null && isEnabled) {
          IconButton(onClick = { onValueChange(JsonNull) }) {
            Icon(MaterialIcons.Clear, contentDescription = Strings.editRuleConditionDelete)
          }
        } else {
          ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSheet)
        }
      },
      colors = theme.pickerField(),
    )
    Box(modifier = Modifier.matchParentSize().clickable(isEnabled) { showSheet = true })
  }

  if (showSheet) {
    val items = entities ?: persistentListOf()
    val selectedEntity =
      items.firstOrNull { it.id == selectedId } ?: EntitySummary(id = "", name = "")
    ListBottomSheet(
      value = selectedEntity,
      options = items,
      onDismiss = { showSheet = false },
      onSelect = { entity -> onValueChange(JsonPrimitive(entity.id)) },
      sheetState = sheetState,
      string = { it.name },
      key = EntitySummary::id,
      theme = theme,
    )
  }
}
