package aktual.budget.payees.vm.locations

import aktual.budget.payees.domain.AddressLookup
import aktual.budget.payees.domain.PayeeLocations
import alakazam.kotlin.TimeZoneProvider
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.toLocalDateTime

@Inject
internal class PayeeLocationsLoader(
  private val locations: PayeeLocations,
  private val addresses: AddressLookup,
  private val timeZones: TimeZoneProvider,
) {
  fun observe(): Flow<List<PayeeLocationItem>> =
    combine(locations.observeAll(), addresses.addresses) { all, book ->
      val timeZone = timeZones.get()
      all.map { location ->
        PayeeLocationItem(
          id = location.id,
          payee = location.payee,
          payeeName = location.payeeName,
          coordinates = location.coordinates,
          date = location.createdAt?.toLocalDateTime(timeZone)?.date,
          address = book[location.coordinates],
        )
      }
    }
}
