package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.radioButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList

// Picks one of [options], applied straight away. Options the report can't use are greyed out
@Composable
internal fun <T> OptionSheet(
  title: String,
  options: ImmutableList<T>,
  selected: T,
  isEnabled: (T) -> Boolean,
  label: @Composable (T) -> String,
  onSelect: (T) -> Unit,
  onDismiss: () -> Unit,
) {
  QuerySheet(title = title, onDismiss = onDismiss) {
    Column(modifier = Modifier.selectableGroup()) {
      options.fastForEach { option ->
        val enabled = isEnabled(option)
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 48.dp)
              .testTag(Tags.CustomOption)
              .selectable(
                selected = option == selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = { onSelect(option) },
              ),
          verticalAlignment = CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          RadioButton(
            selected = option == selected,
            onClick = null,
            enabled = enabled,
            colors = colors.radioButton(),
          )
          Text(
            text = label(option),
            style = typography.bodyLarge,
            color = if (enabled) colors.pageText else colors.pageTextSubdued,
          )
        }
      }
    }
  }
}
