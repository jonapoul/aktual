package aktual.budget.payees.domain

import aktual.budget.db.GetAllWithPayee
import aktual.budget.db.GetForPayee
import aktual.budget.db.dao.DatabaseTables.PAYEE_LOCATIONS
import aktual.budget.db.dao.PayeeLocationDao
import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.utils.BudgetSync
import aktual.core.model.Coordinates
import aktual.core.model.Coordinates.Companion.MAX_LONGITUDE
import aktual.core.model.Distance
import aktual.core.model.distanceTo
import aktual.core.utils.UuidGenerator
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.math.cos
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(BudgetScope::class)
class PayeeLocationsImpl(
  private val dao: PayeeLocationDao,
  private val budgetSync: BudgetSync,
  private val uuidGenerator: UuidGenerator,
  private val clock: Clock,
) : PayeeLocations {
  override suspend fun save(payee: PayeeId, coordinates: Coordinates): PayeeLocationId {
    val id = uuidGenerator(::PayeeLocationId)
    budgetSync(
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

  override suspend fun delete(id: PayeeLocationId) {
    budgetSync(listOf(tombstone(PAYEE_LOCATIONS, id.value)))
  }

  override suspend fun restore(id: PayeeLocationId) {
    budgetSync(listOf(untombstone(PAYEE_LOCATIONS, id.value)))
  }

  override suspend fun forPayee(payee: PayeeId): List<PayeeLocation> =
    dao.forPayee(payee).mapNotNull { it.toLocation() }

  override fun observe(payee: PayeeId): Flow<List<PayeeLocation>> =
    dao.observeForPayee(payee).map { rows -> rows.mapNotNull { it.toLocation() } }

  override fun observeAll(): Flow<List<NamedPayeeLocation>> =
    dao.observeAll().map { rows -> rows.mapNotNull { it.toLocation() } }

  private fun GetAllWithPayee.toLocation(): NamedPayeeLocation? {
    val payee = payee_id ?: return null
    val coordinates = Coordinates.orNull(latitude, longitude) ?: return null
    return NamedPayeeLocation(id, payee, payee_name.orEmpty(), coordinates, created_at)
  }

  private fun GetForPayee.toLocation(): PayeeLocation? =
    Coordinates.orNull(latitude, longitude)?.let { PayeeLocation(id, it, created_at) }

  override suspend fun hasNearby(
    payee: PayeeId,
    coordinates: Coordinates,
    maxDistance: Distance,
  ): Boolean = forPayee(payee).any { it.coordinates.distanceTo(coordinates) <= maxDistance }

  override suspend fun nearby(
    coordinates: Coordinates,
    maxDistance: Distance,
  ): List<NearbyPayee> {
    val latitudeDelta = Math.toDegrees(maxDistance.meters / Coordinates.EARTH_RADIUS_METERS)
    val longitudeDelta = latitudeDelta / cos(Math.toRadians(coordinates.latitude))
    val minLongitude = coordinates.longitude - longitudeDelta
    val maxLongitude = coordinates.longitude + longitudeDelta
    // A box that crosses the antimeridian, or spans it near a pole, can't be one BETWEEN, so
    // longitude is left to the exact distance
    val wraps = minLongitude < -MAX_LONGITUDE || maxLongitude > MAX_LONGITUDE
    val rows =
      dao.inBounds(
        minLatitude = coordinates.latitude - latitudeDelta,
        maxLatitude = coordinates.latitude + latitudeDelta,
        minLongitude = if (wraps) -MAX_LONGITUDE else minLongitude,
        maxLongitude = if (wraps) MAX_LONGITUDE else maxLongitude,
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
      .values
      .asSequence()
      .map { locations -> locations.minBy { it.distance } }
      .sortedBy { it.distance }
      .take(MAX_NEARBY)
      .toList()
  }

  private companion object {
    const val MAX_NEARBY = 10
  }
}
