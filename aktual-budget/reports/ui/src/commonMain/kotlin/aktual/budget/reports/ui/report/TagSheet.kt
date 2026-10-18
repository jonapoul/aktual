package aktual.budget.reports.ui.report

import aktual.budget.model.TagId
import aktual.budget.tags.domain.TagLabel
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.TagChip
import aktual.core.ui.checkbox
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

// packages/desktop-client/src/components/reports/TagScopeSelector.tsx. Nothing picked means every
// tag
@Composable
internal fun TagSheet(
  tags: ImmutableList<TagLabel>,
  selected: ImmutableSet<TagId>,
  onChange: (Set<TagId>) -> Unit,
  onDismiss: () -> Unit,
) =
  QuerySheet(title = Strings.reportsCustomEditorTags, onDismiss = onDismiss) {
    Text(
      text = Strings.reportsCustomEditorTagsHint,
      style = typography.bodyMedium,
      color = colors.tableHeaderText,
    )
    TextButton(onClick = { onChange(emptySet()) }) { Text(Strings.reportsCustomEditorSelectNone) }

    Column {
      tags.fastForEach { tag ->
        val isChecked = tag.id in selected
        CheckRow(
          modifier =
            Modifier.toggleable(
              value = isChecked,
              role = Role.Checkbox,
              onValueChange = { checked ->
                onChange(if (checked) selected + tag.id else selected - tag.id)
              },
            ),
          checkbox = {
            Checkbox(checked = isChecked, onCheckedChange = null, colors = colors.checkbox())
          },
          label = { TagChip(text = tag.name, color = tag.color) },
        )
      }
    }
  }
