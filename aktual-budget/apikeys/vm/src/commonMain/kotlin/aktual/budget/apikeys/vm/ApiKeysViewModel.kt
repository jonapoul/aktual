package aktual.budget.apikeys.vm

import aktual.api.client.ApiKeysApi
import aktual.api.model.apikeys.ApiKey
import aktual.api.model.apikeys.ApiKeysResponse
import aktual.budget.BudgetLocalPreferences
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.TimeZoneProvider
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Instant
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime

/**
 * The budget API keys made for the open budget, to add or revoke them.
 *
 * See packages/desktop-client/src/components/settings/ApiKeys.tsx
 */
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ApiKeysViewModel
internal constructor(
  private val api: ApiKeysApi,
  private val localPreferences: BudgetLocalPreferences,
  private val timeZones: TimeZoneProvider,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableState = MutableStateFlow<ApiKeysState>(Loading)
  private val mutableDialog = MutableStateFlow<ApiKeysDialog?>(null)
  private var loadJob: Job? = null

  val state: StateFlow<ApiKeysState> = mutableState.asStateFlow()
  val dialog: StateFlow<ApiKeysDialog?> = mutableDialog.asStateFlow()

  init {
    reload()
  }

  fun reload() {
    loadJob?.cancel()
    loadJob = viewModelScope.launch {
      mutableState.update { Loading }
      val state = load()
      mutableState.update { state }
    }
  }

  fun add() {
    mutableDialog.update { CreateKey() }
  }

  fun create(name: String) {
    val budget = localPreferences[CloudFileId] ?: return
    if ((mutableDialog.value as? CreateKey)?.isBusy == true) return
    mutableDialog.update { CreateKey(isBusy = true) }
    viewModelScope.launch {
      val response =
        runCatchingCancellable(
          attempt = { api.create(name.trim(), listOf(budget)) },
          onFailure = { e ->
            log.e(e) { "Failed creating API key" }
            null
          },
        )
      if (response is Success) {
        mutableDialog.update { CreatedKey(response.data.key) }
        reload()
      } else {
        mutableDialog.update { CreateKey(hasFailed = true) }
      }
    }
  }

  fun revoke(key: ApiKeyItem) {
    mutableDialog.update { RevokeKey(key) }
  }

  fun confirmRevoke() {
    val dialog = mutableDialog.value as? RevokeKey ?: return
    if (dialog.isBusy) return
    mutableDialog.update { dialog.copy(isBusy = true, hasFailed = false) }
    viewModelScope.launch {
      val response =
        runCatchingCancellable(
          attempt = { api.revoke(dialog.key.id) },
          onFailure = { e ->
            log.e(e) { "Failed revoking API key" }
            null
          },
        )
      if (response is Success) {
        mutableDialog.update { null }
        reload()
      } else {
        mutableDialog.update { dialog.copy(hasFailed = true) }
      }
    }
  }

  fun dismissDialog() {
    mutableDialog.update { null }
  }

  private suspend fun load(): ApiKeysState {
    val budget = localPreferences[CloudFileId] ?: return Unsupported
    return runCatchingCancellable(
      attempt = {
        when (val response = api.list()) {
          is Success -> {
            val keys = response.data.filter { budget in it.budgets }.map { it.toItem() }
            if (keys.isEmpty()) Empty else Loaded(keys.toImmutableList())
          }

          is Failed ->
            if (response.reason == ApiKeysResponse.UNSUPPORTED) {
              Unsupported
            } else {
              Failure(response.reason)
            }
        }
      },
      onFailure = { e ->
        log.e(e) { "Failed loading API keys" }
        Failure(e.requireMessage())
      },
    )
  }

  private fun ApiKey.toItem() =
    ApiKeyItem(
      id = id,
      name = name,
      created = Instant.fromEpochSeconds(createdAtSeconds).toLocalDateTime(timeZones.get()).date,
    )
}
