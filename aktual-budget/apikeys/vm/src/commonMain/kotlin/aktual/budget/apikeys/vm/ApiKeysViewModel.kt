package aktual.budget.apikeys.vm

import aktual.api.client.ApiKeysApi
import aktual.api.model.apikeys.ApiKey
import aktual.api.model.apikeys.ApiKeyPermission
import aktual.api.model.apikeys.ApiKeysResponse
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.utils.BudgetLocalPreferences
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
  private val mutableSheet = MutableStateFlow<ApiKeysSheet?>(null)
  private var loadJob: Job? = null

  val state: StateFlow<ApiKeysState> = mutableState.asStateFlow()
  val sheet: StateFlow<ApiKeysSheet?> = mutableSheet.asStateFlow()

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
    mutableSheet.update { CreateKey() }
  }

  fun create(name: String, canSyncSharedBanks: Boolean) {
    val budget = localPreferences[CloudFileId] ?: return
    if ((mutableSheet.value as? CreateKey)?.isBusy == true) return
    mutableSheet.update { CreateKey(isBusy = true) }
    val permissions =
      if (canSyncSharedBanks) listOf(ApiKeyPermission.BankSyncGlobal) else emptyList()
    viewModelScope.launch {
      val response =
        runCatchingCancellable(
          attempt = { api.create(name.trim(), listOf(budget), permissions) },
          onFailure = { e ->
            log.e(e) { "Failed creating API key" }
            null
          },
        )
      when {
        response is Success -> {
          mutableSheet.update { CreatedKey(response.data.key) }
          reload()
        }

        response is Failed && response.reason == FileAccessDenied ->
          mutableSheet.update { CreateKey(error = NoBudgetAccess) }

        else -> mutableSheet.update { CreateKey(error = Failed) }
      }
    }
  }

  fun revoke(key: ApiKeyItem) {
    mutableSheet.update { RevokeKey(key) }
  }

  fun confirmRevoke() {
    val sheet = mutableSheet.value as? RevokeKey ?: return
    if (sheet.isBusy) return
    mutableSheet.update { sheet.copy(isBusy = true, hasFailed = false) }
    viewModelScope.launch {
      val response =
        runCatchingCancellable(
          attempt = { api.revoke(sheet.key.id) },
          onFailure = { e ->
            log.e(e) { "Failed revoking API key" }
            null
          },
        )
      // Not found means it's already gone, e.g. revoked from another device
      if (response is Success || response is Failed && response.reason == ApiKeyNotFound) {
        mutableSheet.update { null }
        reload()
      } else {
        mutableSheet.update { sheet.copy(hasFailed = true) }
      }
    }
  }

  fun dismissSheet() {
    mutableSheet.update { null }
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

          is Failed -> Failure(response.reason?.reason)
          ApiKeysResponse.Unsupported -> Unsupported
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
      canSyncSharedBanks = BankSyncGlobal in permissions,
    )
}
