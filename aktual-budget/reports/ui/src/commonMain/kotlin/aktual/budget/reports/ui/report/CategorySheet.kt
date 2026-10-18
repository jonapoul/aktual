package aktual.budget.reports.ui.report

import aktual.budget.model.CategoryId
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.CategoryGroupChoice
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.checkbox
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

// packages/desktop-client/src/components/reports/CategorySelector.tsx. Hidden categories are only
// listed when the report shows them
@Composable
internal fun CategorySheet(
  groups: ImmutableList<CategoryGroupChoice>,
  selected: ImmutableSet<CategoryId>,
  showHidden: Boolean,
  onChange: (Set<CategoryId>) -> Unit,
  onDismiss: () -> Unit,
) =
  QuerySheet(title = Strings.reportsCustomEditorCategories, onDismiss = onDismiss) {
    val all = groups.flatMap { group -> group.categories.map { it.id } }
    Row(horizontalArrangement = spacedBy(8.dp)) {
      TextButton(onClick = { onChange(all.toSet()) }) {
        Text(Strings.reportsCustomEditorSelectAll)
      }
      TextButton(onClick = { onChange(emptySet()) }) {
        Text(Strings.reportsCustomEditorSelectNone)
      }
    }

    Column {
      groups.fastForEach { group ->
        if (showHidden || !group.isHidden) {
          val visible = group.categories.filter { showHidden || !it.isHidden }
          val ids = visible.map { it.id }
          val count = ids.count { it in selected }
          val state: ToggleableState =
            when (count) {
              0 -> Off
              ids.size -> On
              else -> Indeterminate
            }
          CheckRow(
            modifier =
              Modifier.testTag(Tags.GroupCheckbox)
                .triStateToggleable(
                  state = state,
                  role = Role.Checkbox,
                  onClick = {
                    onChange(if (state == On) selected - ids.toSet() else selected + ids)
                  },
                ),
            text = group.name,
            isBold = true,
          ) {
            TriStateCheckbox(state = state, onClick = null, colors = colors.checkbox())
          }

          visible.fastForEach { category ->
            val isChecked = category.id in selected
            CheckRow(
              modifier =
                Modifier.padding(start = 24.dp)
                  .testTag(Tags.CategoryCheckbox)
                  .toggleable(
                    value = isChecked,
                    role = Role.Checkbox,
                    onValueChange = { checked ->
                      onChange(if (checked) selected + category.id else selected - category.id)
                    },
                  ),
              text = category.name,
              isBold = false,
            ) {
              Checkbox(checked = isChecked, onCheckedChange = null, colors = colors.checkbox())
            }
          }
        }
      }
    }
  }

@Composable
internal fun CheckRow(
  text: String,
  isBold: Boolean,
  modifier: Modifier = Modifier,
  checkbox: @Composable RowScope.() -> Unit,
) =
  CheckRow(modifier = modifier, checkbox = checkbox) {
    Text(
      text = text,
      style = if (isBold) typography.titleSmall else typography.bodyLarge,
      color = colors.pageText,
    )
  }

@Composable
internal fun CheckRow(
  checkbox: @Composable RowScope.() -> Unit,
  modifier: Modifier = Modifier,
  label: @Composable RowScope.() -> Unit,
) {
  Row(
    modifier = modifier.fillMaxWidth().heightIn(min = 44.dp),
    verticalAlignment = CenterVertically,
    horizontalArrangement = spacedBy(8.dp),
  ) {
    checkbox()
    label()
  }
}
