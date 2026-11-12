package aktual.budget.payees.domain

import aktual.budget.payees.domain.AddressResult.Failed
import aktual.budget.payees.domain.AddressResult.Found
import aktual.budget.payees.domain.AddressResult.Loading
import aktual.budget.payees.domain.AddressResult.NotFound
import aktual.core.model.Coordinates
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import aktual.log
import aktual.prefs.SecurityPreferences
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nominatim.api.NominatimApi

interface AddressLookup {
  val isEnabled: Flow<Boolean>

  // Empty while lookups are turned off
  val addresses: Flow<AddressBook>

  suspend fun setEnabled(enabled: Boolean)

  fun request(coordinates: Coordinates)
}

// Looks up addresses of payee locations one at a time, at most once a second as Nominatim's usage
// policy asks. Results are only kept in memory.
@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class)
class NominatimAddressLookup
internal constructor(
  private val api: NominatimApi,
  private val preferences: SecurityPreferences,
  scope: BudgetCoroutineScope,
) : AddressLookup {
  private val results =
    MutableStateFlow<PersistentMap<Coordinates, AddressResult>>(persistentMapOf())
  private val queue = Channel<Coordinates>(UNLIMITED)

  override val isEnabled: Flow<Boolean> = preferences.lookUpPayeeAddresses.asFlow()

  override val addresses: Flow<AddressBook> =
    combine(isEnabled, results) { enabled, results ->
      if (enabled) AddressBook(results, isEnabled = true) else AddressBook.Empty
    }

  init {
    scope.launch { for (key in queue) lookUp(key) }
    scope.launch {
      isEnabled.collect { enabled -> if (!enabled) results.update { persistentMapOf() } }
    }
  }

  override suspend fun setEnabled(enabled: Boolean) = preferences.lookUpPayeeAddresses.set(enabled)

  override fun request(coordinates: Coordinates) {
    val key = coordinates.rounded()
    var isNew = false
    results.update { current ->
      val existing = current[key]
      isNew = existing == null || existing == Failed
      if (isNew) current.putting(key, Loading) else current
    }
    if (isNew) queue.trySend(key)
  }

  private suspend fun lookUp(key: Coordinates) {
    if (results.value[key] != Loading) return
    if (!preferences.lookUpPayeeAddresses.get()) {
      results.update { it.removing(key) }
      return
    }

    val result =
      runCatchingCancellable(
        attempt = {
          val place = api.reverse(key, defaultLanguageTag())
          place.toAddress()?.let(::Found) ?: NotFound
        },
        onFailure = { e ->
          log.w(e) { "Failed looking up address" }
          Failed
        },
      )
    results.update { current ->
      if (current[key] == Loading) current.putting(key, result) else current
    }
    delay(MIN_INTERVAL)
  }

  private companion object {
    val MIN_INTERVAL = 1.seconds
  }
}

internal expect fun defaultLanguageTag(): String
