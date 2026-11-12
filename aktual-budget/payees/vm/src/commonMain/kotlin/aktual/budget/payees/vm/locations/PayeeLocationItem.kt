package aktual.budget.payees.vm.locations

import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.core.model.Coordinates
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class PayeeLocationItem(
  val id: PayeeLocationId,
  val payee: PayeeId,
  val payeeName: String,
  val coordinates: Coordinates,
  val date: LocalDate?,
)
