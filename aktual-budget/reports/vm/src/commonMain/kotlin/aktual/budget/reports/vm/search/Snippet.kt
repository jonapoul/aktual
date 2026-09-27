package aktual.budget.reports.vm.search

private val MarkdownLink = Regex("""!?\[([^\]]*)]\([^)]*\)""")
private val MarkdownSymbols = Regex("""[*_`~#>|]""")
private val Whitespace = Regex("""\s+""")

// Roughly how many characters to keep before a match, so it has some context
private const val SNIPPET_LEAD = 20

internal fun String.markdownToPlainText(): String =
  replace(MarkdownLink, "$1").replace(MarkdownSymbols, "").collapseWhitespace()

internal fun String.collapseWhitespace(): String = replace(Whitespace, " ").trim()

// Starts shortly before the first match, so it's visible when the text is cut to a couple of lines
internal fun snippet(text: String, query: String): String {
  val index = text.indexOf(query, ignoreCase = true)
  if (index <= SNIPPET_LEAD) return text
  val wordStart = text.lastIndexOf(' ', startIndex = index - SNIPPET_LEAD) + 1
  val start = if (wordStart > 0) wordStart else index - SNIPPET_LEAD
  return "…" + text.substring(start)
}
