package aktual.budget.payees.domain

import aktual.budget.payees.domain.AddressResult.Failed
import aktual.budget.payees.domain.AddressResult.Found
import aktual.budget.payees.domain.AddressResult.Loading
import aktual.core.model.Coordinates
import aktual.di.BudgetCoroutineScope
import aktual.prefs.AppLockTimeout
import aktual.prefs.Preference
import aktual.prefs.SecurityPreferences
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nominatim.api.NominatimApi
import nominatim.api.model.Place

class AddressLookupTest {
  private val preferences = FakeSecurityPreferences()

  @Test
  fun `Nothing looked up while turned off`() = runTest {
    val api = FakeNominatimApi()
    val lookup = lookup(api)

    lookup.request(London)
    settle()

    assertThat(api.calls).isEmpty()
    lookup.addresses.test { assertThat(awaitItem()[London]).isNull() }
  }

  @Test
  fun `Repeated requests are looked up once`() = runTest {
    preferences.lookUpPayeeAddresses.set(true)
    val api = FakeNominatimApi()
    val lookup = lookup(api)

    lookup.request(London)
    lookup.request(London)
    lookup.request(Coordinates(London.latitude + 0.000001, London.longitude))
    settle()

    assertThat(api.calls).containsExactly(London)
    lookup.addresses.test {
      assertThat(awaitItem()[London]).isEqualTo(Found(Address("Shop", null)))
    }
  }

  @Test
  fun `Lookups are at least a second apart`() = runTest {
    preferences.lookUpPayeeAddresses.set(true)
    val times = mutableListOf<Long>()
    val api = FakeNominatimApi(onCall = { times += testScheduler.currentTime })
    val lookup = lookup(api)

    lookup.request(London)
    lookup.request(Manchester)
    settle()

    assertThat(times).containsExactly(0L, 1_000L)
  }

  @Test
  fun `Failed lookup is retried on the next request`() = runTest {
    preferences.lookUpPayeeAddresses.set(true)
    var fail = true
    val api = FakeNominatimApi(onCall = { if (fail) error("Offline") })
    val lookup = lookup(api)

    lookup.request(London)
    settle()
    lookup.addresses.test { assertThat(awaitItem()[London]).isEqualTo(Failed) }

    fail = false
    lookup.request(London)
    settle()
    lookup.addresses.test {
      assertThat(awaitItem()[London]).isEqualTo(Found(Address("Shop", null)))
    }
    assertThat(api.calls).containsExactly(London, London)
  }

  @Test
  fun `Turning off forgets addresses`() = runTest {
    preferences.lookUpPayeeAddresses.set(true)
    val api = FakeNominatimApi()
    val lookup = lookup(api)

    lookup.request(London)
    settle()
    lookup.setEnabled(false)
    settle()
    lookup.setEnabled(true)
    settle()

    lookup.addresses.test { assertThat(awaitItem()[London]).isEqualTo(Loading) }
    assertThat(api.calls).containsExactly(London)
  }

  // advanceUntilIdle stops once only background work is left, so it never runs the lookups
  private fun TestScope.settle() {
    advanceTimeBy(5.seconds)
    runCurrent()
  }

  private fun TestScope.lookup(api: NominatimApi) =
    NominatimAddressLookup(api, preferences, BudgetCoroutineScope(backgroundScope))

  private class FakeNominatimApi(private val onCall: () -> Unit = {}) : NominatimApi {
    val calls = mutableListOf<Coordinates>()

    override suspend fun reverse(coordinates: Coordinates, language: String): Place {
      calls += coordinates
      onCall()
      return Place(name = "Shop")
    }
  }

  private class FakePreference<T : Any>(override val default: T) : Preference<T> {
    private val state = MutableStateFlow(default)

    override suspend fun get(): T = state.value

    override suspend fun set(value: T?) = state.update { value ?: default }

    override fun asFlow(): Flow<T> = state
  }

  private class FakeSecurityPreferences : SecurityPreferences {
    override val appLockEnabled = FakePreference(default = false)
    override val appLockTimeout = FakePreference(AppLockTimeout.Default)
    override val hidePreviewInAppSwitcher = FakePreference(default = true)
    override val autoSavePayeeLocations = FakePreference(default = true)
    override val lookUpPayeeAddresses = FakePreference(default = false)
  }

  private companion object {
    val London = Coordinates(51.50735, -0.12776)
    val Manchester = Coordinates(53.47940, -2.24530)
  }
}
