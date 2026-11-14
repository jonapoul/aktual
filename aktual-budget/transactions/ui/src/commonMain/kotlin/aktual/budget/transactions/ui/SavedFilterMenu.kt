package aktual.budget.transactions.ui

import aktual.budget.transactions.vm.SavedFiltersState
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.DialogTextButton
import aktual.core.ui.HostedSheet
import aktual.core.ui.OnAction
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.DialogProperties

// Names the applied saved filter, or says the conditions aren't saved, and opens the menu
@Composable
internal fun SavedFilterChip(
  saved: SavedFiltersState,
  hasConditions: Boolean,
  onClick: () -> Unit,
) {
  val applied = saved.applied
  val text =
    when {
      applied != null && saved.isModified -> Strings.transactionsFilterModified(applied.name)
      applied != null -> applied.name
      hasConditions -> Strings.transactionsFilterUnsaved
      else -> Strings.transactionsFilterSaved
    }
  Chip(
    text = text,
    colors = ChipDefaults.selectableColors(isSelected = applied != null),
    trailingIcon = AktualIcons.CheveronDown,
    onClick = onClick,
    role = Button,
  )
}

internal enum class FilterNaming {
  SaveNew,
  Rename,
}

// The saved filters to apply, then what can be done with the current conditions, as upstream's
// SavedFilterMenuButton offers
@Composable
internal fun SavedFilterSheet(
  saved: SavedFiltersState,
  hasConditions: Boolean,
  onAction: OnAction<Action>,
  onName: (FilterNaming) -> Unit,
  onDismiss: () -> Unit,
) =
  HostedSheet(onDismiss = onDismiss) {
    val applied = saved.applied
    Column(modifier = Modifier.verticalScrollWithBar()) {
      Text(
        modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp),
        text = Strings.transactionsFilterSaved,
        style = typography.headlineSmall,
      )
      saved.filters.fastForEach { filter ->
        BottomSheetListItem(
          label = filter.name,
          isSelected = filter.id == applied?.id,
          onClick = { dismiss { onAction(Action.ApplySavedFilter(filter)) } },
        )
      }
      if (saved.filters.isNotEmpty() && hasConditions) HorizontalDivider()

      val item: @Composable (String, () -> Unit) -> Unit = { label, onClick ->
        BottomSheetListItem(label = label, isSelected = false, onClick = { dismiss(onClick) })
      }
      if (hasConditions && (applied == null || saved.isModified)) {
        item(Strings.transactionsFilterSaveNew) { onName(FilterNaming.SaveNew) }
      }
      if (applied != null && saved.isModified) {
        item(Strings.transactionsFilterUpdate) { onAction(Action.UpdateSavedFilter) }
        item(Strings.transactionsFilterRevert) { onAction(Action.ApplySavedFilter(applied)) }
      }
      if (applied != null) {
        item(Strings.transactionsFilterRename) { onName(FilterNaming.Rename) }
        item(Strings.transactionsFilterDelete) { onAction(Action.DeleteSavedFilter) }
      }
      if (hasConditions) {
        item(Strings.transactionsFilterClear) { onAction(Action.ClearConditions) }
      }
    }
  }

@Composable
internal fun FilterNameDialog(
  initialName: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    val state = rememberTextFieldState(initialName)
    val name = state.text.trim().toString()
    val enabled = name.isNotEmpty() && name != initialName
    AktualAlertDialogContent(
      title =
        if (initialName.isEmpty()) Strings.transactionsFilterSaveNew
        else Strings.transactionsFilterRename,
      buttons = {
        DialogTextButton(text = Strings.transactionsFilterNameCancel, onClick = onDismiss)
        DialogTextButton(
          kind = Primary,
          text = Strings.transactionsFilterNameConfirm,
          isEnabled = enabled,
          onClick = { onConfirm(name) },
        )
      },
      content = {
        AktualTextField(
          modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
          state = state,
          placeholderText = Strings.transactionsFilterNamePlaceholder,
          singleLine = true,
          keyboardOptions = KeyboardOptions(capitalization = Sentences, imeAction = Done),
          onKeyboardAction = { if (enabled) onConfirm(name) },
        )
      },
    )
  }
