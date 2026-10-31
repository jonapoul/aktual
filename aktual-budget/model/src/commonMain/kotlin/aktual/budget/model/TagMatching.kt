package aktual.budget.model

// A tag token is a '#' not preceded by another '#', then one or more non-'#', non-whitespace
// characters. No boundary is needed on the left, so "word#tag" matches "tag". Same pattern as
// hasTags in packages/loot-core/src/server/rules/condition.ts and tags/app.ts
private val TAG_TOKEN = Regex("""(?<!#)#([^#\s]+)""")

// Lowercased, without the leading '#'
fun tagsInNotes(notes: String): Set<String> =
  TAG_TOKEN.findAll(notes).mapTo(mutableSetOf()) { it.groupValues[1].lowercase() }

// extractTagsFromText() in packages/loot-core/src/shared/tags.ts. Case-sensitive and in the order
// they first appear
fun tagNamesInNotes(notes: String): List<String> =
  TAG_TOKEN.findAll(notes).map { it.groupValues[1] }.distinct().toList()

// Where each tag token sits in [notes], including its leading '#'
fun tagRangesInNotes(notes: String): List<IntRange> =
  TAG_TOKEN.findAll(notes).map { it.range }.toList()

fun notesContainTag(notes: String, tag: String): Boolean = tag.lowercase() in tagsInNotes(notes)
