package aktual.budget.transactions.domain

// The transaction last added while this budget has been open. As upstream's lastTransaction, the
// next new one starts on its date and in its account
interface LastTransaction {
  val fields: TransactionFields?

  fun set(fields: TransactionFields)
}
