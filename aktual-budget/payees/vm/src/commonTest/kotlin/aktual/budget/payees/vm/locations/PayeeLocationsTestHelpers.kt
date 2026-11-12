package aktual.budget.payees.vm.locations

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.PayeeLocationDao
import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.domain.AddressBook
import aktual.budget.payees.domain.AddressLookup
import aktual.budget.payees.domain.PayeeLocations
import aktual.core.model.Coordinates
import aktual.di.BudgetCoroutineScope
import aktual.test.TestSyncController
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.TimeZone

internal val HERE = Coordinates(latitude = 51.5074, longitude = -0.1278)

internal fun BudgetDatabase.payeeLocations(scope: TestScope, sync: TestSyncController) =
  PayeeLocations(
    dao = PayeeLocationDao(this, TestCoroutineContexts(scope.standardDispatcher)),
    syncController = sync,
    uuidGenerator = { "location" },
    clock = Clock.System,
  )

internal fun BudgetDatabase.loader(
  scope: TestScope,
  sync: TestSyncController,
  addresses: AddressLookup = FakeAddressLookup(),
) = PayeeLocationsLoader(payeeLocations(scope, sync), addresses, timeZones = { TimeZone.UTC })

internal fun BudgetDatabase.deleter(scope: TestScope, sync: TestSyncController) =
  PayeeLocationDeleter(payeeLocations(scope, sync), BudgetCoroutineScope(scope))

internal suspend fun BudgetDatabase.insertPayee(id: String, name: String = id) =
  payeesQueries.insert(PayeeId(id), name, null, false, null, false, 1)

internal suspend fun BudgetDatabase.insertLocation(
  id: String,
  payee: String,
  createdAt: Instant = Instant.parse("2026-11-07T12:00:00Z"),
) =
  payeeLocationsQueries.insert(
    id = PayeeLocationId(id),
    payee_id = PayeeId(payee),
    latitude = HERE.latitude,
    longitude = HERE.longitude,
    created_at = createdAt,
    tombstone = false,
  )

internal class FakeAddressLookup : AddressLookup {
  val enabled = MutableStateFlow(false)
  val book = MutableStateFlow(AddressBook.Empty)
  val requests = mutableListOf<Coordinates>()

  override val isEnabled: Flow<Boolean> = enabled
  override val addresses: Flow<AddressBook> = book

  override suspend fun setEnabled(enabled: Boolean) = this.enabled.update { enabled }

  override fun request(coordinates: Coordinates) {
    requests += coordinates
  }
}
