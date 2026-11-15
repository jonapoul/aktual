package aktual.budget.payees.domain

import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.core.model.Coordinates
import aktual.core.model.Distance
import aktual.core.model.meters
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

data class PayeeLocation(
  val id: PayeeLocationId,
  val coordinates: Coordinates,
  val createdAt: Instant?,
)

data class NamedPayeeLocation(
  val id: PayeeLocationId,
  val payee: PayeeId,
  val payeeName: String,
  val coordinates: Coordinates,
  val createdAt: Instant?,
)

data class NearbyPayee(val payee: PayeeId, val location: PayeeLocationId, val distance: Distance)

// As upstream's payee-location-create, payee-locations-get, payee-location-delete and
// payees-get-nearby
interface PayeeLocations {
  suspend fun save(payee: PayeeId, coordinates: Coordinates): PayeeLocationId

  suspend fun delete(id: PayeeLocationId)

  suspend fun restore(id: PayeeLocationId)

  // Newest first
  suspend fun forPayee(payee: PayeeId): List<PayeeLocation>

  fun observe(payee: PayeeId): Flow<List<PayeeLocation>>

  // Every alive payee's locations, by payee name then newest first
  fun observeAll(): Flow<List<NamedPayeeLocation>>

  suspend fun hasNearby(
    payee: PayeeId,
    coordinates: Coordinates,
    maxDistance: Distance = MAX_DISTANCE,
  ): Boolean

  // The closest location of each payee in range, nearest first
  suspend fun nearby(
    coordinates: Coordinates,
    maxDistance: Distance = MAX_DISTANCE,
  ): List<NearbyPayee>

  companion object {
    // As upstream's DEFAULT_MAX_DISTANCE_METERS
    val MAX_DISTANCE: Distance = 500.meters
  }
}
