package aktual.core

/**
 * Starts shortly before the first case-insensitive match of [query], so it's still visible when
 * [text] is cut off at the end. Keeps around [lead] characters before the match for context,
 * backing up to the start of a word.
 */
fun snippet(text: String, query: String, lead: Int = 20): String {
  val index = text.indexOf(query, ignoreCase = true)
  if (index <= lead) return text
  val wordStart = text.lastIndexOf(' ', startIndex = index - lead) + 1
  val start = if (wordStart > 0) wordStart else index - lead
  return "…" + text.substring(start)
}
