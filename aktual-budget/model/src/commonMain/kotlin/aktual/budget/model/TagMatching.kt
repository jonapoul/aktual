package aktual.budget.model

// A tag (#name) is "linked" to a transaction when its token appears in the transaction notes,
// bounded by start/whitespace on the left and whitespace, another '#', or end-of-string on the
// right. Matching is case-insensitive. Mirrors the hasTags filter in upstream Actual
// (packages/loot-core/src/server/rules/condition.ts) and the tag extraction in tags/app.ts.
private val TAG_TOKEN = Regex("""(?<!#)#([^#\s]+)""")

// The distinct tag names (lowercased, without the leading '#') referenced in [notes].
fun tagsInNotes(notes: String): Set<String> =
  TAG_TOKEN.findAll(notes).mapTo(mutableSetOf()) { it.groupValues[1].lowercase() }

// Whether [notes] references the tag named [tag] (case-insensitive).
fun notesContainTag(notes: String, tag: String): Boolean = tag.lowercase() in tagsInNotes(notes)
