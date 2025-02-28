package actual.budget.model

enum class Currency(val symbol: Char) {
  Gbp(symbol = '£'),
  ;

  override fun toString(): String = symbol.toString()

  companion object {
    fun default() = Gbp
  }
}
