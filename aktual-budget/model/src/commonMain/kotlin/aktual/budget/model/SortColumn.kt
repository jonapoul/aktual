package aktual.budget.model

enum class SortColumn {
  Date,
  Account,
  Payee,
  Notes,
  Category,
  Amount,
  ;

  companion object {
    val Default = Date
  }
}

enum class SortDirection {
  Ascending,
  Descending,
  ;

  companion object {
    val Default = Descending
  }
}
