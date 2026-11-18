package aktual.budget.tags.domain

// Matches upstream's sanitiseTagName in the mobile tags page. Tags in notes are "#word" tokens, so
// a name can't hold '#' or whitespace. Runs of whitespace (with any dashes around them) become "-"
fun sanitiseTagName(value: String): String =
  value.replace("#", "").trim().replace(WHITESPACE_RUN, "-")

// Like [sanitiseTagName], but keeps one trailing space while typing so the next character can
// still turn it into a dash
fun sanitiseTagNameInput(value: String): String {
  val withoutLeading = value.replace("#", "").trimStart()
  val name = sanitiseTagName(withoutLeading)
  val hasTrailingSpace = withoutLeading.lastOrNull()?.isWhitespace() == true
  return if (hasTrailingSpace && name.isNotEmpty()) "$name " else name
}

private val WHITESPACE_RUN = Regex("-*\\s[\\s-]*")
