package aktual.budget.formula

// Recursive descent port of HyperFormula's FormulaParser grammar. From loosest to tightest:
// comparisons, &, + -, * /, ^, unary + -, postfix %. All binary operators are left associative,
// and unary minus binds tighter than ^, so -2^2 is 4
internal class FormulaParser private constructor(private val tokens: List<Token>) {
  private var position = 0

  private fun parseFormula(): FormulaNode {
    expect(Equals)
    val node = booleanExpression()
    val extra = peek()
    if (extra != null) fail(UnexpectedToken, extra.image)
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
    while (consume(Ampersand)) {
      left = BinaryNode(Concat, left, additionExpression())
    }
    return left
  }

  private fun additionExpression(): FormulaNode {
    var left = multiplicationExpression()
    while (true) {
      val op = ADDITIONS[peek()?.type] ?: return left
      position++
      left = BinaryNode(op, left, multiplicationExpression())
    }
  }

  private fun multiplicationExpression(): FormulaNode {
    var left = powerExpression()
    while (true) {
      val op = MULTIPLICATIONS[peek()?.type] ?: return left
      position++
      left = BinaryNode(op, left, powerExpression())
    }
  }

  private fun powerExpression(): FormulaNode {
    var left = atomicExpression()
    while (consume(Power)) {
      left = BinaryNode(Power, left, atomicExpression())
    }
    return left
  }

  private fun atomicExpression(): FormulaNode =
    when {
      consume(Plus) -> UnaryNode(Plus, atomicExpression())
      consume(Minus) -> UnaryNode(Minus, atomicExpression())
      else -> percentExpression(positiveAtomicExpression())
    }

  private fun percentExpression(node: FormulaNode): FormulaNode =
    if (consume(Percent)) UnaryNode(Percent, node) else node

  private fun positiveAtomicExpression(): FormulaNode {
    val token = next() ?: fail(UnexpectedEnd)
    return when (token.type) {
      LeftBrace -> arrayExpression()
      LeftParen -> booleanExpression().also { expect(RightParen) }
      Procedure -> procedureExpression(token)
      CellReference -> cellReference(token)
      Name -> NamedNode(token.image)
      TokenType.Number -> NumberNode(token.image.toDouble())
      Text -> TextNode(token.image.substring(1, token.image.length - 1))
      ErrorLiteral -> errorLiteral(token)
      Colon -> fail(CellRange)
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
      RightParen,
      RightBrace,
      Ampersand,
      Semicolon,
      Comma -> fail(UnexpectedToken, token.image)
    }
  }

  private fun procedureExpression(token: Token): FormulaNode {
    val name = token.image.dropLast(1).uppercase()
    val args = mutableListOf(argumentOrEmpty())
    while (consume(Comma)) {
      args += argumentOrEmpty()
    }
    expect(RightParen)
    if (args.size == 1 && args.single() == EmptyArgNode) args.clear()
    return CallNode(name, args)
  }

  private fun argumentOrEmpty(): FormulaNode {
    val type = peek()?.type
    return if (type == Comma || type == RightParen) EmptyArgNode else booleanExpression()
  }

  private fun arrayExpression(): FormulaNode {
    val rows = mutableListOf(mutableListOf(booleanExpression()))
    while (true) {
      when {
        consume(Comma) -> rows.last() += booleanExpression()
        consume(Semicolon) -> rows += mutableListOf(booleanExpression())
        else -> break
      }
    }
    expect(RightBrace)
    return ArrayNode(rows)
  }

  private fun cellReference(token: Token): FormulaNode {
    if (peek()?.type == Colon) fail(CellRange)
    val address = token.image.replace("$", "").uppercase()
    return CellReferenceNode(isFormulaCell = address == "A1")
  }

  private fun errorLiteral(token: Token): FormulaNode {
    val image = token.image.uppercase()
    val type =
      FormulaErrorType.entries.firstOrNull { it.literal == image }
        ?: fail(UnknownErrorLiteral, token.image)
    return ErrorNode(type)
  }

  private fun fail(reason: ErrorReason, subject: String? = null): Nothing =
    throw FormulaParseException(reason, subject)

  private fun peek(): Token? = tokens.getOrNull(position)

  private fun next(): Token? = tokens.getOrNull(position)?.also { position++ }

  private fun consume(type: TokenType): Boolean {
    if (peek()?.type != type) return false
    position++
    return true
  }

  private fun expect(type: TokenType) {
    if (consume(type)) return
    val found = peek() ?: fail(UnexpectedEnd)
    fail(UnexpectedToken, found.image)
  }

  companion object {
    private val COMPARISONS =
      mapOf(
        TokenType.Equals to BinaryOp.Equal,
        TokenType.NotEqual to BinaryOp.NotEqual,
        TokenType.Greater to BinaryOp.Greater,
        TokenType.Less to BinaryOp.Less,
        TokenType.GreaterOrEqual to BinaryOp.GreaterOrEqual,
        TokenType.LessOrEqual to BinaryOp.LessOrEqual,
      )

    private val ADDITIONS =
      mapOf(TokenType.Plus to BinaryOp.Add, TokenType.Minus to BinaryOp.Subtract)

    private val MULTIPLICATIONS =
      mapOf(TokenType.Times to BinaryOp.Multiply, TokenType.Divide to BinaryOp.Divide)

    fun parse(formula: String): FormulaNode =
      FormulaParser(FormulaLexer.tokenize(formula)).parseFormula()
  }
}
