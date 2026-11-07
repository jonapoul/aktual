package aktual.budget.payees.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.PAYEE_LOCATIONS
import aktual.budget.db.dao.PayeeLocationDao
import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.core.model.Coordinates
import aktual.core.model.Distance
import aktual.core.model.meters
import aktual.core.utils.UuidGenerator
import dev.zacsweers.metro.Inject
import kotlin.math.cos
import kotlin.time.Clock

data class PayeeLocation(val id: PayeeLocationId, val coordinates: Coordinates)

data class NearbyPayee(val payee: PayeeId, val location: PayeeLocationId, val distance: Distance)

// As upstream's payee-location-create, payee-locations-get, payee-location-delete and
// payees-get-nearby
@Inject
class PayeeLocations(
  private val dao: PayeeLocationDao,
  private val syncController: BudgetSyncController,
  private val uuidGenerator: UuidGenerator,
  private val clock: Clock,
) {
  suspend fun save(payee: PayeeId, coordinates: Coordinates): PayeeLocationId {
    val id = uuidGenerator(::PayeeLocationId)
    syncController.syncChanges(
      listOf(
        localChange(PAYEE_LOCATIONS, id.value, "payee_id", payee.value),
        localChange(PAYEE_LOCATIONS, id.value, "latitude", coordinates.latitude),
        localChange(PAYEE_LOCATIONS, id.value, "longitude", coordinates.longitude),
        localChange(
          PAYEE_LOCATIONS,
          id.value,
          "created_at",
          clock.now().toEpochMilliseconds(),
        ),
      ),
    )
    return id
  }

  suspend fun delete(id: PayeeLocationId) {
    syncController.syncChanges(listOf(tombstone(PAYEE_LOCATIONS, id.value)))
  }

  // Newest first
  suspend fun forPayee(payee: PayeeId): List<PayeeLocation> =
    dao.forPayee(payee).mapNotNull { row ->
      Coordinates.orNull(row.latitude, row.longitude)?.let { PayeeLocation(row.id, it) }
    }

  suspend fun hasNearby(
    payee: PayeeId,
    coordinates: Coordinates,
    maxDistance: Distance = MAX_DISTANCE,
  ): Boolean = forPayee(payee).any { it.coordinates.distanceTo(coordinates) <= maxDistance }

  // The closest location of each payee in range, nearest first
  suspend fun nearby(
    coordinates: Coordinates,
    maxDistance: Distance = MAX_DISTANCE,
  ): List<NearbyPayee> {
    val latitudeDelta = Math.toDegrees(maxDistance.meters / Coordinates.EARTH_RADIUS_METERS)
    val longitudeDelta = latitudeDelta / cos(Math.toRadians(coordinates.latitude))
    val rows =
      dao.inBounds(
        minLatitude = coordinates.latitude - latitudeDelta,
        maxLatitude = coordinates.latitude + latitudeDelta,
        minLongitude = coordinates.longitude - longitudeDelta,
        maxLongitude = coordinates.longitude + longitudeDelta,
      )
    return rows
      .asSequence()
      .mapNotNull { row ->
        val payee = row.payee_id ?: return@mapNotNull null
        val location = Coordinates.orNull(row.latitude, row.longitude) ?: return@mapNotNull null
        NearbyPayee(payee, row.id, location.distanceTo(coordinates))
      }
      .filter { it.distance <= maxDistance }
      .groupBy { it.payee }
      .map { (_, locations) -> locations.minBy { it.distance } }
      .sortedBy { it.distance }
      .take(MAX_NEARBY)
      .toList()
  }

  companion object {
    // As upstream's DEFAULT_MAX_DISTANCE_METERS
    val MAX_DISTANCE: Distance = 500.meters

    private const val MAX_NEARBY = 10
  }
}
