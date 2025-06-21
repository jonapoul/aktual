package actual.budget.model

enum class TransactionsFormat {
  List,
  Table,
  ;

  companion object {
    val Default = List
  }
}
