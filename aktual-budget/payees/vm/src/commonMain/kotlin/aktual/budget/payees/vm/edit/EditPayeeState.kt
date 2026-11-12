package aktual.budget.payees.vm.edit

import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.domain.AddressResult
import aktual.budget.payees.vm.list.PayeeItem
import aktual.core.model.Coordinates
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable sealed interface EditPayeeState

data object Loading : EditPayeeState

// The payee as it's stored, kept up to date so the rule and transaction counts stay right
@JvmInline value class Editing(val payee: PayeeItem) : EditPayeeState

@JvmInline value class Failure(val cause: String?) : EditPayeeState

@Immutable
data class SavedLocation(
  val id: PayeeLocationId,
  val coordinates: Coordinates,
  val date: LocalDate?,
  // Null while address lookups are turned off
  val address: AddressResult? = null,
)
