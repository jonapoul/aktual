package aktual.budget.payees.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.PAYEE_LOCATIONS
import aktual.budget.db.dao.PayeeLocationDao
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.core.model.Coordinates
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class PayeeLocationsTest {
  @Test
  fun `Saving writes each column`() = runDatabaseTest {
    val sync = TestSyncController()

    val id = locations(sync).save(PayeeId(SHOP), Coordinates(51.5074, -0.1278))

    assertThat(id).isEqualTo(PayeeLocationId(GENERATED_ID))
    assertThat(sync.changes)
      .containsExactly(
        LocalChange(PAYEE_LOCATIONS, GENERATED_ID, "payee_id", MessageValue.String(SHOP)),
        LocalChange(PAYEE_LOCATIONS, GENERATED_ID, "latitude", MessageValue.Real(51.5074)),
        LocalChange(PAYEE_LOCATIONS, GENERATED_ID, "longitude", MessageValue.Real(-0.1278)),
        LocalChange(PAYEE_LOCATIONS, GENERATED_ID, "created_at", MessageValue.Number(NOW_MILLIS)),
      )
  }

  @Test
  fun `Deleting tombstones the location`() = runDatabaseTest {
    val sync = TestSyncController()

    locations(sync).delete(PayeeLocationId("loc"))

    assertThat(sync.changes)
      .containsExactly(LocalChange(PAYEE_LOCATIONS, "loc", "tombstone", MessageValue.Number(1)))
  }

  @Test
  fun `Nearby payees are nearest first with their closest location`() = runDatabaseTest {
    insertPayee(SHOP)
    insertPayee(CAFE)
    insertLocation("shop-far", SHOP, HERE.north(meters = 400))
    insertLocation("shop-near", SHOP, HERE.north(meters = 50))
    insertLocation("cafe", CAFE, HERE.north(meters = 200))

    val nearby = locations().nearby(HERE)

    assertThat(nearby)
      .extracting(NearbyPayee::location)
      .containsExactly(PayeeLocationId("shop-near"), PayeeLocationId("cafe"))
  }

  @Test
  fun `Nearby payees are found across the antimeridian`() = runDatabaseTest {
    insertPayee(SHOP)
    insertLocation("shop", SHOP, Coordinates(latitude = 0.0, longitude = 179.999))

    val nearby = locations().nearby(Coordinates(latitude = 0.0, longitude = -179.999))

    assertThat(nearby).extracting(NearbyPayee::location).containsExactly(PayeeLocationId("shop"))
  }

  @Test
  fun `Nearby payees are found near a pole`() = runDatabaseTest {
    insertPayee(SHOP)
    insertLocation("shop", SHOP, Coordinates(latitude = 89.9999, longitude = 90.0))

    val nearby = locations().nearby(Coordinates(latitude = 89.9999, longitude = -90.0))

    assertThat(nearby).extracting(NearbyPayee::location).containsExactly(PayeeLocationId("shop"))
  }

  @Test
  fun `Nearby skips what's out of range or deleted`() = runDatabaseTest {
    insertPayee(SHOP)
    insertPayee(CAFE, tombstone = true)
    insertPayee(BAKERY)
    insertLocation("shop", SHOP, HERE.north(meters = 600))
    insertLocation("cafe", CAFE, HERE.north(meters = 10))
    insertLocation("bakery", BAKERY, HERE.north(meters = 10), tombstone = true)

    assertThat(locations().nearby(HERE)).isEmpty()
  }

  @Test
  fun `Restoring untombstones the location`() = runDatabaseTest {
    val sync = TestSyncController()

    locations(sync).restore(PayeeLocationId("loc"))

    assertThat(sync.changes)
      .containsExactly(LocalChange(PAYEE_LOCATIONS, "loc", "tombstone", MessageValue.Number(0)))
  }

  @Test
  fun `Observing lists the payee's live locations, newest first`() = runDatabaseTest {
    insertPayee(SHOP)
    insertPayee(CAFE)
    insertLocation("old", SHOP, HERE, createdAt = NOW_MILLIS - 1)
    insertLocation("new", SHOP, HERE.north(meters = 10))
    insertLocation("deleted", SHOP, HERE, tombstone = true)
    insertLocation("cafe", CAFE, HERE)

    locations().observe(PayeeId(SHOP)).test {
      assertThat(awaitItem())
        .extracting(PayeeLocation::id)
        .containsExactly(PayeeLocationId("new"), PayeeLocationId("old"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Observing all lists live locations by payee name, newest first`() = runDatabaseTest {
    insertPayee(SHOP)
    insertPayee(CAFE)
    insertPayee(BAKERY, tombstone = true)
    insertLocation("shop-old", SHOP, HERE, createdAt = NOW_MILLIS - 1)
    insertLocation("shop-new", SHOP, HERE)
    insertLocation("shop-deleted", SHOP, HERE, tombstone = true)
    insertLocation("cafe", CAFE, HERE)
    insertLocation("bakery", BAKERY, HERE)

    locations().observeAll().test {
      assertThat(awaitItem())
        .extracting(NamedPayeeLocation::id)
        .containsExactly(
          PayeeLocationId("cafe"),
          PayeeLocationId("shop-new"),
          PayeeLocationId("shop-old"),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Has nearby checks only that payee`() = runDatabaseTest {
    insertPayee(SHOP)
    insertPayee(CAFE)
    insertLocation("cafe", CAFE, HERE.north(meters = 10))
    insertLocation("shop", SHOP, HERE.north(meters = 700))

    assertThat(locations().hasNearby(PayeeId(CAFE), HERE)).isTrue()
    assertThat(locations().hasNearby(PayeeId(SHOP), HERE)).isFalse()
  }

  private fun BudgetDatabase.locations(sync: TestSyncController = TestSyncController()) =
    PayeeLocations(
      dao = PayeeLocationDao(this, TestCoroutineContexts(UnconfinedTestDispatcher())),
      syncController = sync,
      uuidGenerator = { GENERATED_ID },
      clock =
        object : Clock {
          override fun now() = Instant.fromEpochMilliseconds(NOW_MILLIS)
        },
    )

  private suspend fun BudgetDatabase.insertPayee(id: String, tombstone: Boolean = false) =
    payeesQueries.insert(PayeeId(id), id, null, tombstone, null, false, 1)

  private suspend fun BudgetDatabase.insertLocation(
    id: String,
    payee: String,
    coordinates: Coordinates,
    tombstone: Boolean = false,
    createdAt: Long = NOW_MILLIS,
  ) =
    payeeLocationsQueries.insert(
      id = PayeeLocationId(id),
      payee_id = PayeeId(payee),
      latitude = coordinates.latitude,
      longitude = coordinates.longitude,
      created_at = Instant.fromEpochMilliseconds(createdAt),
      tombstone = tombstone,
    )

  // One degree of latitude is about 111km
  private fun Coordinates.north(meters: Int) =
    Coordinates(latitude + meters / METERS_PER_DEGREE, longitude)

  private companion object {
    const val GENERATED_ID = "generated-id"
    const val NOW_MILLIS = 1_762_500_000_000L
    const val SHOP = "shop"
    const val CAFE = "cafe"
    const val BAKERY = "bakery"
    const val METERS_PER_DEGREE = 111_195.0
    val HERE = Coordinates(51.5074, -0.1278)
  }
}
