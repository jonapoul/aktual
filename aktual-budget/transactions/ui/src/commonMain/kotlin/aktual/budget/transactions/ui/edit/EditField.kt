package aktual.budget.transactions.ui.edit

// Only one field is edited at a time, as upstream's SingleActiveEditForm has it
internal enum class EditField {
  Amount,
  Payee,
  Category,
  Account,
  Date,
  Notes,
}
