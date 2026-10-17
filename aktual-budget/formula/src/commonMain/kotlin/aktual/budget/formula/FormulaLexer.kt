package aktual.budget.formula

internal enum class TokenType {
  Plus,
  Minus,
  Times,
  Divide,
  Power,
  Equals,
  NotEqual,
  Percent,
  GreaterOrEqual,
  LessOrEqual,
  Greater,
  Less,
  LeftParen,
  RightParen,
  LeftBrace,
  RightBrace,
  Procedure,
  Colon,
  Number,
  Text,
  ErrorLiteral,
  Ampersand,
  CellReference,
  Name,
  Semicolon,
  Comma,
}

internal data class Token(val type: TokenType, val image: String, val offset: Int)

internal class FormulaParseException(val reason: ErrorReason, val subject: String? = null) :
  Exception("$reason $subject")

// Port of HyperFormula's lexer config with the enUS defaults. Patterns are tried in order and the
// first match wins, like Chevrotain does
internal object FormulaLexer {
  private const val LETTER = """A-Za-zÀ-ʯ"""
  private const val NON_RESERVED = "${LETTER}0-9_."

  private val WHITESPACE = Regex("[ \t\n\r]+")
  private val R1C1_REFERENCE = Regex("[rR][0-9]*[cC][0-9]*")

  private val PATTERNS =
    listOf(
      TokenType.Plus to Regex("""\+"""),
      TokenType.Minus to Regex("-"),
      TokenType.Times to Regex("""\*"""),
      TokenType.Divide to Regex("/"),
      TokenType.Power to Regex("""\^"""),
      TokenType.Equals to Regex("="),
      TokenType.NotEqual to Regex("<>"),
      TokenType.Percent to Regex("%"),
      TokenType.GreaterOrEqual to Regex(">="),
      TokenType.LessOrEqual to Regex("<="),
      TokenType.Greater to Regex(">"),
      TokenType.Less to Regex("<"),
      TokenType.LeftParen to Regex("""\("""),
      TokenType.RightParen to Regex("""\)"""),
      TokenType.LeftBrace to Regex("""\{"""),
      TokenType.RightBrace to Regex("""\}"""),
      TokenType.Procedure to Regex("""[$LETTER][$NON_RESERVED]*\("""),
      TokenType.Colon to Regex(":"),
      TokenType.Number to Regex("""((\.\d+)|(\d+(\.\d*)?))(e[+-]?\d+)?"""),
      TokenType.Text to Regex(""""([^"\\]*(\\.[^"\\]*)*)""""),
      TokenType.ErrorLiteral to Regex("#[A-Za-z0-9/]+[?!]?"),
      TokenType.Ampersand to Regex("&"),
      TokenType.CellReference to Regex("""(\$?[A-Za-z]+\$?[0-9]+)(?=[^$NON_RESERVED]|$)"""),
      TokenType.Name to Regex("[${LETTER}_][$NON_RESERVED]*"),
      TokenType.Semicolon to Regex(";"),
      TokenType.Comma to Regex(","),
    )

  fun tokenize(input: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var offset = 0
    while (offset < input.length) {
      val whitespace = WHITESPACE.matchAt(input, offset)
      if (whitespace != null) {
        offset = whitespace.range.last + 1
      } else {
        val token =
          nextToken(input, offset)
            ?: throw FormulaParseException(UnexpectedCharacter, input[offset].toString())
        tokens += token
        offset += token.image.length
      }
    }
    return tokens
  }

  private fun nextToken(input: String, offset: Int): Token? =
    PATTERNS.firstNotNullOfOrNull { (type, pattern) ->
      pattern
        .matchAt(input, offset)
        ?.takeUnless { type == Name && R1C1_REFERENCE.matches(it.value) }
        ?.let { Token(type, it.value, offset) }
    }
}
