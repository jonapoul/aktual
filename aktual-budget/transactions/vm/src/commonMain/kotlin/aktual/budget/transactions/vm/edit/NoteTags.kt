package aktual.budget.transactions.vm.edit

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// The tag being typed into a note: the word up to the cursor, when it starts with a single '#'.
// Upstream has "##name" for a literal "#name", so that isn't one
@Immutable data class TagQuery(val start: Int, val end: Int, val name: String)

fun tagQuery(notes: String, cursor: Int): TagQuery? {
  if (cursor <= 0 || cursor > notes.length) return null
  var start = cursor
  while (start > 0 && !notes[start - 1].isWhitespace()) start--
  val word = notes.substring(start, cursor)
  if (word.takeWhile { it == '#' }.length != 1) return null
  return TagQuery(start = start, end = cursor, name = word.drop(1))
}

// As upstream's useFilteredTags, though by substring rather than fuzzily: shorter tags first, then
// those matching nearer their start. A lone '#' offers every tag
fun ImmutableList<TagOption>.matching(query: TagQuery): ImmutableList<TagOption> {
  if (query.name.isEmpty()) return take(MAX_TAGS).toImmutableList()
  return mapNotNull { option ->
      val index = option.tag.indexOf(query.name, ignoreCase = true)
      if (index < 0) null else option to index
    }
    .sortedWith(compareBy({ it.first.tag.length }, { it.second }))
    .take(MAX_TAGS)
    .map { it.first }
    .toImmutableList()
}

// No tag has exactly the name typed
fun ImmutableList<TagOption>.canCreate(query: TagQuery): Boolean =
  query.name.isNotEmpty() && none { it.tag == query.name }

// Replaces the notes from start to end with text, leaving the cursor after it
@Immutable data class NotesEdit(val start: Int, val end: Int, val text: String)

// As upstream's NoteInsertHashButton, the '#' starts a new word
fun insertHash(notes: String, start: Int, end: Int): NotesEdit {
  val space = if (start == 0 || notes[start - 1].isWhitespace()) "" else " "
  return NotesEdit(start = start, end = end, text = "$space#")
}

// As upstream's NoteTagAutocomplete, a space follows the tag picked
fun TagQuery.complete(tag: String): NotesEdit = NotesEdit(start = start, end = end, text = "#$tag ")

// As upstream's fzf limit
private const val MAX_TAGS = 100
