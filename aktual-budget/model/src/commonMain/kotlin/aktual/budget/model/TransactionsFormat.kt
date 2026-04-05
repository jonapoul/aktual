package aktual.budget.model

enum class TransactionsFormat(private val value: String) {
  List("List"),
  Table("Table");

  override fun toString(): String = value

  companion object {
    val Default = List
  }
}
