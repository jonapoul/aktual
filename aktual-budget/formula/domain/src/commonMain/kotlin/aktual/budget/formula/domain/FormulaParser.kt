package aktual.budget.formula.domain

// Recursive descent port of HyperFormula's FormulaParser grammar. From loosest to tightest:
// comparisons, &, + -, * /, ^, unary + -, postfix %. All binary operators are left associative,
// and unary minus binds tighter than ^, so -2^2 is 4
internal class FormulaParser private constructor(private val tokens: List<Token>) {
  private var position = 0

  private fun parseFormula(): FormulaNode {
    expect(TokenType.Equals)
    val node = booleanExpression()
    val extra = peek()
    if (extra != null) throw FormulaParseException("Unexpected '${extra.image}'")
    return node
  }

  private fun booleanExpression(): FormulaNode {
    var left = concatenateExpression()
    while (true) {
      val op = COMPARISONS[peek()?.type] ?: return left
      position++
      left = BinaryNode(op, left, concatenateExpression())
    }
  }

  private fun concatenateExpression(): FormulaNode {
    var left = additionExpression()
    while (consume(TokenType.Ampersand)) {
      left = BinaryNode(BinaryOp.Concat, left, additionExpression())
    }
    return left
  }

  private fun additionExpression(): FormulaNode {
    var left = multiplicationExpression()
    while (true) {
      val op =
        when (peek()?.type) {
          TokenType.Plus -> BinaryOp.Add
          TokenType.Minus -> BinaryOp.Subtract
          else -> return left
        }
      position++
      left = BinaryNode(op, left, multiplicationExpression())
    }
  }

  private fun multiplicationExpression(): FormulaNode {
    var left = powerExpression()
    while (true) {
      val op =
        when (peek()?.type) {
          TokenType.Times -> BinaryOp.Multiply
          TokenType.Divide -> BinaryOp.Divide
          else -> return left
        }
      position++
      left = BinaryNode(op, left, powerExpression())
    }
  }

  private fun powerExpression(): FormulaNode {
    var left = atomicExpression()
    while (consume(TokenType.Power)) {
      left = BinaryNode(BinaryOp.Power, left, atomicExpression())
    }
    return left
  }

  private fun atomicExpression(): FormulaNode =
    when {
      consume(TokenType.Plus) -> UnaryNode(UnaryOp.Plus, atomicExpression())
      consume(TokenType.Minus) -> UnaryNode(UnaryOp.Minus, atomicExpression())
      else -> {
        val node = positiveAtomicExpression()
        if (consume(TokenType.Percent)) UnaryNode(UnaryOp.Percent, node) else node
      }
    }

  private fun positiveAtomicExpression(): FormulaNode {
    val token = next() ?: throw FormulaParseException("Unexpected end of formula")
    return when (token.type) {
      TokenType.LeftBrace -> arrayExpression()
      TokenType.LeftParen -> booleanExpression().also { expect(TokenType.RightParen) }
      TokenType.Procedure -> procedureExpression(token)
      TokenType.CellReference -> cellReference(token)
      TokenType.Name -> NamedNode(token.image)
      TokenType.Number -> NumberNode(token.image.toDouble())
      TokenType.Text -> TextNode(token.image.substring(1, token.image.length - 1))
      TokenType.ErrorLiteral -> errorLiteral(token)
      TokenType.Colon -> throw FormulaParseException(RANGES_UNSUPPORTED)
      else -> throw FormulaParseException("Unexpected '${token.image}'")
    }
  }

  private fun procedureExpression(token: Token): FormulaNode {
    val name = token.image.dropLast(1).uppercase()
    val args = mutableListOf(argumentOrEmpty())
    while (consume(TokenType.Comma)) {
      args += argumentOrEmpty()
    }
    expect(TokenType.RightParen)
    if (args.size == 1 && args.single() == EmptyArgNode) args.clear()
    return CallNode(name, args)
  }

  private fun argumentOrEmpty(): FormulaNode =
    when (peek()?.type) {
      TokenType.Comma,
      TokenType.RightParen -> EmptyArgNode
      else -> booleanExpression()
    }

  private fun arrayExpression(): FormulaNode {
    val rows = mutableListOf(mutableListOf(booleanExpression()))
    while (true) {
      when {
        consume(TokenType.Comma) -> rows.last() += booleanExpression()
        consume(TokenType.Semicolon) -> rows += mutableListOf(booleanExpression())
        else -> break
      }
    }
    expect(TokenType.RightBrace)
    return ArrayNode(rows)
  }

  private fun cellReference(token: Token): FormulaNode {
    if (peek()?.type == TokenType.Colon) throw FormulaParseException(RANGES_UNSUPPORTED)
    val address = token.image.replace("$", "").uppercase()
    return CellReferenceNode(isFormulaCell = address == "A1")
  }

  private fun errorLiteral(token: Token): FormulaNode {
    val image = token.image.uppercase()
    val type =
      FormulaErrorType.entries.firstOrNull { it.literal == image }
        ?: throw FormulaParseException("Unknown error literal")
    return ErrorNode(type)
  }

  private fun peek(): Token? = tokens.getOrNull(position)

  private fun next(): Token? = tokens.getOrNull(position)?.also { position++ }

  private fun consume(type: TokenType): Boolean {
    if (peek()?.type != type) return false
    position++
    return true
  }

  private fun expect(type: TokenType) {
    if (!consume(type)) {
      val found = peek()?.image?.let { "'$it'" } ?: "end of formula"
      throw FormulaParseException("Expected $type but found $found")
    }
  }

  companion object {
    private const val RANGES_UNSUPPORTED = "Cell ranges aren't supported"

    private val COMPARISONS =
      mapOf(
        TokenType.Equals to BinaryOp.Equal,
        TokenType.NotEqual to BinaryOp.NotEqual,
        TokenType.Greater to BinaryOp.Greater,
        TokenType.Less to BinaryOp.Less,
        TokenType.GreaterOrEqual to BinaryOp.GreaterOrEqual,
        TokenType.LessOrEqual to BinaryOp.LessOrEqual,
      )

    fun parse(formula: String): FormulaNode =
      FormulaParser(FormulaLexer.tokenize(formula)).parseFormula()
  }
}
