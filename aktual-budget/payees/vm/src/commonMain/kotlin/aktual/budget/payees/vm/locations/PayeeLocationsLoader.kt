package aktual.budget.payees.vm.locations

import aktual.budget.payees.domain.PayeeLocations
import alakazam.kotlin.TimeZoneProvider
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.toLocalDateTime

@Inject
internal class PayeeLocationsLoader(
  private val locations: PayeeLocations,
  private val timeZones: TimeZoneProvider,
) {
  fun observe(): Flow<List<PayeeLocationItem>> =
    locations.observeAll().map { all ->
      val timeZone = timeZones.get()
      all.map { location ->
        PayeeLocationItem(
          id = location.id,
          payee = location.payee,
          payeeName = location.payeeName,
          coordinates = location.coordinates,
          date = location.createdAt?.toLocalDateTime(timeZone)?.date,
        )
      }
    }
}
