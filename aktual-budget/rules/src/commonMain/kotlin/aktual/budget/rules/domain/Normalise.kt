package aktual.budget.rules.domain

// likePatternToRegex in packages/loot-core/src/platform/server/sqlite/unicodeLike.ts: % is any
// run of characters, ? is any one character, and a backslash escapes either
@Suppress("UnnecessaryParentheses")
fun likePatternToRegex(pattern: String): Regex {
  val regex = StringBuilder()
  var i = 0
  while (i < pattern.length) {
    val ch = pattern[i]
    val next = pattern.getOrNull(i + 1)
    when (ch) {
      '\\' if (next == '%' || next == '?' || next == '\\') -> {
        regex.append(Regex.escape(next.toString()))
        i++
      }
      '%' -> regex.append(".*")
      '?' -> regex.append('.')
      else -> regex.append(Regex.escape(ch.toString()))
    }
    i++
  }
  return Regex(regex.toString(), RegexOption.IGNORE_CASE)
}

// getNormalisedString in packages/loot-core/src/shared/normalisation.ts
fun normalise(value: String): String {
  var lower = value.lowercase()
  for ((from, to) in LETTER_REPLACEMENTS) lower = lower.replace(from, to)
  return stripDiacritics(lower)
}

private val LETTER_REPLACEMENTS = listOf("ł" to "l", "ø" to "o", "ß" to "ss", "œ" to "oe")

/** Decomposes [value] and drops the combining marks, so "é" becomes "e" */
internal expect fun stripDiacritics(value: String): String
