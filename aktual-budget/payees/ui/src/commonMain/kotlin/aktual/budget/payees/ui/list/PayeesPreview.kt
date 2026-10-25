package aktual.budget.payees.ui.list

import aktual.budget.model.PayeeId
import aktual.budget.payees.vm.list.PayeeItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal object PayeesPreview {
  val tesco =
    PayeeItem(
      id = PayeeId("payee-1"),
      name = "Tesco",
      isTransfer = false,
      isFavorite = true,
      learnsCategories = true,
      numTransactions = 42,
      numRules = 2,
      isUnused = false,
    )

  val argos =
    PayeeItem(
      id = PayeeId("payee-2"),
      name = "Argos",
      isTransfer = false,
      isFavorite = false,
      learnsCategories = true,
      numTransactions = 1,
      numRules = 0,
      isUnused = false,
    )

  val transfer =
    PayeeItem(
      id = PayeeId("payee-3"),
      name = "Savings account",
      isTransfer = true,
      isFavorite = false,
      learnsCategories = true,
      numTransactions = 0,
      numRules = 1,
      isUnused = false,
    )

  val longName =
    PayeeItem(
      id = PayeeId("payee-4"),
      name = "A deliberately long payee name to check how the row truncates its text",
      isTransfer = false,
      isFavorite = true,
      learnsCategories = false,
      numTransactions = 0,
      numRules = 0,
      isUnused = true,
    )

  val all: ImmutableList<PayeeItem> = persistentListOf(argos, transfer, longName, tesco)
}
