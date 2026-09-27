package aktual.budget.reports.vm.search

private val MarkdownLink = Regex("""!?\[([^\]]*)]\([^)]*\)""")
private val MarkdownSymbols = Regex("""[*_`~#>|]""")
private val Whitespace = Regex("""\s+""")

internal fun String.markdownToPlainText(): String =
  replace(MarkdownLink, "$1").replace(MarkdownSymbols, "").collapseWhitespace()

internal fun String.collapseWhitespace(): String = replace(Whitespace, " ").trim()
