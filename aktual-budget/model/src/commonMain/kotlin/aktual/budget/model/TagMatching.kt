package aktual.budget.model

// A tag (#name) is "linked" to a transaction when its token appears in the transaction notes.
// A token is a '#' not immediately preceded by another '#' (so '##' is skipped), followed by one
// or more non-'#', non-whitespace characters. Note this does not require a whitespace boundary on
// the left, so "word#tag" still matches "tag". Matching is case-insensitive. Mirrors the hasTags
// filter in upstream Actual (packages/loot-core/src/server/rules/condition.ts) and the tag
// extraction in tags/app.ts, both of which use this exact pattern.
private val TAG_TOKEN = Regex("""(?<!#)#([^#\s]+)""")

// The distinct tag names (lowercased, without the leading '#') referenced in [notes].
fun tagsInNotes(notes: String): Set<String> =
  TAG_TOKEN.findAll(notes).mapTo(mutableSetOf()) { it.groupValues[1].lowercase() }

// extractTagsFromText() in packages/loot-core/src/shared/tags.ts: the distinct tag names in
// [notes],
// in the order they first appear. Unlike [tagsInNotes] this is case-sensitive, as tag identity is
fun tagNamesInNotes(notes: String): List<String> =
  TAG_TOKEN.findAll(notes).map { it.groupValues[1] }.distinct().toList()

// Where each tag token sits in [notes], including its leading '#'
fun tagRangesInNotes(notes: String): List<IntRange> =
  TAG_TOKEN.findAll(notes).map { it.range }.toList()

// Whether [notes] references the tag named [tag] (case-insensitive).
fun notesContainTag(notes: String, tag: String): Boolean = tag.lowercase() in tagsInNotes(notes)
