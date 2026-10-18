package aktual.budget.transactions.ui.edit

import aktual.budget.tags.domain.TagLabel
import aktual.budget.transactions.vm.edit.NotesEdit
import aktual.budget.transactions.vm.edit.TagQuery
import aktual.budget.transactions.vm.edit.canCreate
import aktual.budget.transactions.vm.edit.complete
import aktual.budget.transactions.vm.edit.insertHash
import aktual.budget.transactions.vm.edit.matching
import aktual.budget.transactions.vm.edit.tagQuery
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Numbers
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.EditorSheet
import aktual.core.ui.TagChip
import aktual.core.ui.TagChipShape
import aktual.core.ui.keyboardFocusRequester
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

// As upstream's notes input, a button adds a '#', and typing one offers the tags to complete it
// with or to create one named as typed. Tab picks the first of them
@Composable
internal fun NotesSheet(
  notes: String?,
  tags: ImmutableList<TagLabel>,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit,
  onCreateTag: (String) -> Unit,
) {
  val text = rememberTextFieldState(initialText = notes.orEmpty())
  val focusRequester = keyboardFocusRequester()
  val notesText = text.text.toString()
  val cursor = text.selection.end
  val query = remember(notesText, cursor) { tagQuery(notesText, cursor) }
  val matches = remember(tags, query) { query?.let { tags.matching(it) } }
  val canCreate = query != null && tags.canCreate(query)

  fun select(typed: TagQuery, tag: String) = text.applyEdit(typed.complete(tag))

  fun create(typed: TagQuery) {
    onCreateTag(typed.name)
    select(typed, typed.name)
  }

  EditorSheet(
    title = Strings.transactionNotes,
    cancelText = Strings.transactionSheetCancel,
    confirmText = Strings.transactionSheetDone,
    canConfirm = true,
    onDismiss = onDismiss,
    onConfirm = { onSave(text.text.toString()) },
  ) {
    AktualTextField(
      modifier =
        Modifier.fillMaxWidth().focusRequester(focusRequester).onPreviewKeyEvent { event ->
          if (event.type != KeyDown || event.key != Tab || query == null) {
            return@onPreviewKeyEvent false
          }
          val first = matches?.firstOrNull()
          when {
            first != null -> select(query, first.name)
            canCreate -> create(query)
            else -> return@onPreviewKeyEvent false
          }
          true
        },
      state = text,
      placeholderText = Strings.transactionNotesPlaceholder,
      keyboardOptions = KeyboardOptions(capitalization = Sentences),
      trailingIcon = { InsertHashButton(text, focusRequester) },
    )

    if (query != null && (!matches.isNullOrEmpty() || canCreate)) {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NotesSheetDS.chipSpacing),
        verticalAlignment = CenterVertically,
      ) {
        items(matches.orEmpty(), key = { it.id.value }) { option ->
          TagChip(
            text = option.name,
            color = option.color,
            onClick = { select(query, option.name) },
          )
        }

        if (canCreate) {
          item { CreateTagChip(name = query.name, onClick = { create(query) }) }
        }
      }
    }
  }
}

@Composable
private fun InsertHashButton(text: TextFieldState, focusRequester: FocusRequester) =
  BareIconButton(
    imageVector = MaterialIcons.Numbers,
    contentDescription = Strings.transactionNotesAddTag,
    onClick = {
      val selection = text.selection
      text.applyEdit(insertHash(text.text.toString(), selection.min, selection.max))
      focusRequester.requestFocus()
    },
  )

@Composable
private fun CreateTagChip(name: String, onClick: () -> Unit) =
  Row(
    modifier =
      Modifier.clip(TagChipShape)
        .background(colors.noticeBackground, TagChipShape)
        .clickable(onClick = onClick)
        .padding(NotesSheetDS.createPadding),
    horizontalArrangement = Arrangement.spacedBy(NotesSheetDS.createSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      imageVector = MaterialIcons.Add,
      contentDescription = null,
      modifier = Modifier.size(NotesSheetDS.createIconSize),
      tint = colors.noticeTextDark,
    )
    Text(
      text = Strings.transactionNotesCreateTag,
      style = typography.bodyMedium,
      color = colors.noticeTextDark,
      maxLines = 1,
    )
    TagChip(text = name, color = null)
  }

// Replaces the text the edit covers, leaving the cursor after what it put in
private fun TextFieldState.applyEdit(change: NotesEdit) = edit {
  replace(change.start, change.end, change.text)
  selection = TextRange(change.start + change.text.length)
}

private object NotesSheetDS {
  val chipSpacing = 4.dp
  val createPadding = PaddingValues(start = 9.dp, top = 2.dp, end = 2.dp, bottom = 2.dp)
  val createSpacing = 4.dp
  val createIconSize = 12.dp
}
