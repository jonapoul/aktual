package aktual.budget.formula.domain

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.truncate

// Port of the matching functions in HyperFormula's TextPlugin, plus TEXT from its DateTimePlugin
internal val TEXT_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "CONCATENATE" to
      formulaFunction(Param(Text), repeatLastArgs = 1, expandRanges = true) { args ->
        TextValue(args.texts().joinToString(separator = ""))
      },
    "LEN" to formulaFunction(Param(Text)) { args -> NumberValue(args.text(0).length.toDouble()) },
    "LOWER" to formulaFunction(Param(Text)) { args -> TextValue(args.text(0).lowercase()) },
    "UPPER" to formulaFunction(Param(Text)) { args -> TextValue(args.text(0).uppercase()) },
    "TRIM" to formulaFunction(Param(Text)) { args -> TextValue(trimSpaces(args.text(0))) },
    "LEFT" to
      formulaFunction(Param(Text), Param(Numeric, default = NumberValue(1.0))) { args ->
        val length = args.number(1)
        if (length < 0) {
          ErrorValue(Value, NegativeLength)
        } else {
          TextValue(jsSlice(args.text(0), 0.0, length))
        }
      },
    "RIGHT" to
      formulaFunction(Param(Text), Param(Numeric, default = NumberValue(1.0))) { args ->
        val length = args.number(1)
        when {
          length < 0 -> ErrorValue(Value, NegativeLength)
          length == 0.0 -> TextValue("")
          else -> TextValue(jsSlice(args.text(0), -length))
        }
      },
    "MID" to
      formulaFunction(Param(Text), Param(Numeric), Param(Numeric)) { args ->
        val start = args.number(1)
        val count = args.number(2)
        when {
          start < 1 -> ErrorValue(Value, LessThanOne)
          count < 0 -> ErrorValue(Value, NegativeLength)
          else -> TextValue(jsSubstring(args.text(0), start - 1, start + count - 1))
        }
      },
    "REPT" to
      formulaFunction(Param(Text), Param(Numeric)) { args ->
        val text = args.text(0)
        val count = truncate(args.number(1))
        when {
          count < 0 -> ErrorValue(Value, NegativeCount)
          text.length * count > MAX_TEXT_LENGTH -> ErrorValue(Value, TextTooLong)
          else -> TextValue(text.repeat(count.toInt()))
        }
      },
    "SUBSTITUTE" to
      formulaFunction(Param(Text), Param(Text), Param(Text), Param(Numeric, optional = true)) { args
        ->
        val occurrence = args[3] as? NumberValue
        when {
          occurrence == null -> TextValue(replaceAll(args.text(0), args.text(1), args.text(2)))
          occurrence.value < 1 -> ErrorValue(Value, LessThanOne)
          else ->
            TextValue(replaceOccurrence(args.text(0), args.text(1), args.text(2), occurrence.value))
        }
      },
    "TEXT" to
      formulaFunction(Param(Numeric), Param(Text)) { args ->
        TextValue(formatNumber(args.number(0), args.text(1)))
      },
    "FIND" to
      formulaFunction(Param(Text), Param(Text), Param(Numeric, default = NumberValue(1.0))) { args
        ->
        val text = args.text(1)
        val start = args.number(2)
        if (start < 1 || start > text.length) {
          ErrorValue(Value, StartOutOfRange)
        } else {
          // Upstream's check misses most failed searches, which then give start - 1
          val index = jsSubstring(text, start - 1).indexOf(args.text(0)) + start
          if (index > 0) NumberValue(index) else ErrorValue(Value, PatternNotFound)
        }
      },
    "SEARCH" to
      formulaFunction(Param(Text), Param(Text), Param(Numeric, default = NumberValue(1.0))) { args
        ->
        val text = args.text(1)
        val start = args.number(2)
        if (start < 1 || start > text.length) {
          ErrorValue(Value, StartOutOfRange)
        } else {
          val index = search(args.text(0).lowercase(), jsSubstring(text, start - 1).lowercase())
          if (index > -1) NumberValue(index + start) else ErrorValue(Value, PatternNotFound)
        }
      },
    "REPLACE" to
      formulaFunction(Param(Text), Param(Numeric), Param(Numeric), Param(Text)) { args ->
        val text = args.text(0)
        val start = args.number(1)
        val count = args.number(2)
        when {
          start < 1 -> ErrorValue(Value, LessThanOne)
          count < 0 -> ErrorValue(Value, NegativeLength)
          else ->
            TextValue(
              jsSubstring(text, 0.0, start - 1) +
                args.text(3) +
                jsSubstring(text, start + count - 1),
            )
        }
      },
    "PROPER" to
      formulaFunction(Param(Text)) { args ->
        TextValue(
          args.text(0).replace(LETTERS) {
            it.value[0].uppercase() + it.value.substring(1).lowercase()
          },
        )
      },
    "EXACT" to
      formulaFunction(Param(Text), Param(Text)) { args ->
        BooleanValue(args.text(0) == args.text(1))
      },
  )

// Upstream would throw a RangeError instead
private const val MAX_TEXT_LENGTH = 1_000_000

private val LEADING_SPACES = Regex("^ +")
private val TRAILING_SPACES = Regex(" +$")
private val SPACES = Regex(" +")
private val LETTERS = Regex("\\p{L}+")
private val WILDCARDS = setOf('*', '?')
private val REGEX_SPECIAL =
  setOf('{', '}', '[', ']', '(', ')', '<', '>', '=', '.', '+', '-', ',', '\\', '$', '^', '!')

private fun trimSpaces(text: String): String =
  text.replace(LEADING_SPACES, "").replace(TRAILING_SPACES, "").replace(SPACES, " ")

// JavaScript's String.slice
private fun jsSlice(text: String, start: Double, end: Double = text.length.toDouble()): String {
  fun index(position: Double): Int {
    val relative = truncate(position)
    return if (relative < 0) {
      max(text.length + relative, 0.0).toInt()
    } else {
      min(relative, text.length.toDouble()).toInt()
    }
  }
  val from = index(start)
  val to = index(end)
  return if (from >= to) "" else text.substring(from, to)
}

// JavaScript's String.substring
private fun jsSubstring(text: String, start: Double, end: Double = text.length.toDouble()): String {
  fun index(position: Double): Int =
    truncate(position).coerceIn(0.0, text.length.toDouble()).toInt()
  val from = index(start)
  val to = index(end)
  return text.substring(min(from, to), max(from, to))
}

// Port of SEARCH's matching with upstream's config: "*" and "?" are wildcards unless escaped with
// "~". Accents are only stripped from the pattern, like upstream
private fun search(pattern: String, text: String): Int {
  if (pattern.none { it in WILDCARDS }) return text.indexOf(pattern)
  val normalized = stripAccents(pattern)
  val regex = buildString {
    var i = 0
    while (i < normalized.length) {
      val char = normalized[i]
      val next = normalized.getOrNull(i + 1)
      when {
        char == '~' ->
          if (next == null) {
            append('~')
          } else {
            if (next in WILDCARDS || next in REGEX_SPECIAL) append('\\')
            append(next)
            i++
          }
        char in WILDCARDS -> append('.').append(char)
        char in REGEX_SPECIAL -> append('\\').append(char)
        else -> append(char.lowercase())
      }
      i++
    }
  }
  return Regex(regex).find(text)?.range?.first ?: -1
}

// JavaScript's String.replace with a global regex of the escaped search text, including the "$"
// patterns in the replacement
private fun replaceAll(text: String, search: String, replacement: String): String = buildString {
  var position = 0
  while (position <= text.length) {
    val index = text.indexOf(search, position)
    if (index < 0) break
    append(text, position, index)
    append(substitution(text, index, search, replacement))
    position = index + search.length
    if (search.isEmpty()) {
      if (index < text.length) append(text[index])
      position++
    }
  }
  if (position < text.length) append(text, position, text.length)
}

// JavaScript's String.replace patterns, where [groups] are the match's capture groups
internal fun substitution(
  text: String,
  index: Int,
  match: String,
  replacement: String,
  groups: List<String> = emptyList(),
): String = buildString {
  var i = 0
  while (i < replacement.length) {
    val char = replacement[i]
    val next = replacement.getOrNull(i + 1)
    if (char != '$' || next == null) {
      append(char)
      i++
      continue
    }
    val twoDigits = replacement.getOrNull(i + 2)?.takeIf { next.isDigit() && it.isDigit() }
    val twoDigitGroup = twoDigits?.let { "$next$it".toInt() }?.takeIf { it in 1..groups.size }
    val oneDigitGroup = next.digitToIntOrNull()?.takeIf { it in 1..groups.size }
    val group = twoDigitGroup ?: oneDigitGroup
    when {
      group != null -> append(groups[group - 1])
      next == '$' -> append('$')
      next == '&' -> append(match)
      next == '`' -> append(text, 0, index)
      next == '\'' -> append(text, index + match.length, text.length)
      else -> append(char).append(next)
    }
    i += 2
    if (twoDigitGroup != null) i++
  }
}

// Empty search text matches at the start forever, so upstream only returns for whole occurrences
private fun replaceOccurrence(
  text: String,
  search: String,
  replacement: String,
  occurrence: Double,
): String {
  if (search.isEmpty()) return if (occurrence == floor(occurrence)) replacement + text else text
  var count = 0
  var position = 0
  while (true) {
    val index = text.indexOf(search, position)
    if (index < 0) return text
    count++
    if (occurrence == count.toDouble()) {
      return text.substring(0, index) + replacement + text.substring(index + search.length)
    }
    position = index + search.length
  }
}

// Decomposes the text and drops the combining accents
internal expect fun stripAccents(text: String): String
