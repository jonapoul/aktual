package aktual.budget.payees.domain

import aktual.core.model.Coordinates
import kotlinx.coroutines.flow.Flow

interface AddressLookup {
  val isEnabled: Flow<Boolean>

  // Empty while lookups are turned off
  val addresses: Flow<AddressBook>

  suspend fun setEnabled(enabled: Boolean)

  fun request(coordinates: Coordinates)
}
