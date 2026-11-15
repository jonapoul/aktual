package aktual.budget.rules.components

import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.l10n.Strings
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.HostedSheet
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// A sheet that edits part of a query in place, so Done only closes it. Delete is only shown when
// [onDelete] is set
@Composable
fun QuerySheet(
  title: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  onDelete: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) =
  HostedSheet(onDismiss = onDismiss) {
    QuerySheetPage(
      title = title,
      close = ::close,
      modifier = modifier,
      onDelete = onDelete,
      content = content,
    )
  }

@Composable
fun QuerySheetPage(
  title: String,
  close: (then: () -> Unit) -> Unit,
  modifier: Modifier = Modifier,
  onDelete: (() -> Unit)? = null,
  onDone: () -> Unit = {},
  confirm: QuerySheetConfirm = Done,
  isDoneEnabled: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  val focusManager = LocalFocusManager.current
  Column(
    modifier = modifier.verticalScrollWithBar().padding(horizontal = 20.dp).padding(bottom = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(text = title, style = typography.headlineSmall)

    content()

    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (onDelete != null) {
        AktualButton(
          kind = Destructive,
          variant = Secondary,
          modifier = Modifier.testTag(ConditionTags.QuerySheetDelete),
          text = Strings.reportsFormulaQueryDeleteConditionConfirm,
          onClick = { close(onDelete) },
        )
      }
      AktualButton(
        kind = Primary,
        modifier = Modifier.weight(1f).testTag(ConditionTags.QuerySheetDone),
        text =
          when (confirm) {
            Done -> Strings.reportsFormulaEditorDone
            QuerySheetConfirm.Add -> Strings.reportsFormulaQueryAddConfirm
            Update -> Strings.reportsFormulaQueryUpdateConfirm
          },
        icon =
          when (confirm) {
            Done -> null
            QuerySheetConfirm.Add -> AktualIcons.Add
            Update -> AktualIcons.Check
          },
        isEnabled = isDoneEnabled,
        onClick = {
          // Commits a field still being typed in before Done reads the value
          focusManager.clearFocus()
          close(onDone)
        },
      )
    }
  }
}

// Done closes a sheet editing in place. Add and Update save a draft of a new or existing item
enum class QuerySheetConfirm {
  Done,
  Add,
  Update,
}
