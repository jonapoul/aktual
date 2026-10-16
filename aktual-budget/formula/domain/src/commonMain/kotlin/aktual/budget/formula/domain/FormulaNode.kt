package aktual.budget.formula.domain

internal sealed interface FormulaNode

internal data class NumberNode(val value: Double) : FormulaNode

internal data class TextNode(val value: String) : FormulaNode

internal data class ErrorNode(val type: FormulaErrorType) : FormulaNode

// An argument left out between separators, e.g. the second one in ROUND(1.5,)
internal data object EmptyArgNode : FormulaNode

// Formulas are evaluated in cell A1 of an otherwise empty sheet
internal data class CellReferenceNode(val isFormulaCell: Boolean) : FormulaNode

internal data class NamedNode(val name: String) : FormulaNode

internal data class CallNode(val name: String, val args: List<FormulaNode>) : FormulaNode

internal data class UnaryNode(val op: UnaryOp, val operand: FormulaNode) : FormulaNode

internal data class BinaryNode(val op: BinaryOp, val left: FormulaNode, val right: FormulaNode) :
  FormulaNode

internal data class ArrayNode(val rows: List<List<FormulaNode>>) : FormulaNode

internal enum class UnaryOp {
  Plus,
  Minus,
  Percent,
}

internal enum class BinaryOp {
  Equal,
  NotEqual,
  Greater,
  Less,
  GreaterOrEqual,
  LessOrEqual,
  Concat,
  Add,
  Subtract,
  Multiply,
  Divide,
  Power,
}
