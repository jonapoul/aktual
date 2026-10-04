package aktual.budget.banksync.vm.settings

import aktual.budget.banksync.domain.BankAccountLinker
import aktual.budget.banksync.domain.BankSyncSettings
import aktual.budget.banksync.domain.BankSyncSettingsLoader
import aktual.budget.banksync.domain.BankSyncSettingsWriter
import aktual.budget.banksync.domain.FieldExample
import aktual.budget.banksync.domain.MappableFieldsLoader
import aktual.budget.banksync.domain.MappedField
import aktual.budget.banksync.domain.TransactionDirection
import aktual.budget.banksync.domain.with
import aktual.budget.db.dao.AccountDao
import aktual.budget.model.AccountId
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * An account's bank sync settings, saved to synced preferences.
 *
 * See packages/desktop-client/src/components/mobile/banksync/MobileBankSyncAccountEditPage.tsx
 */
@Stable
@AssistedInject
class BankSyncSettingsViewModel(
  @Assisted private val account: AccountId,
  private val accountDao: AccountDao,
  private val loader: BankSyncSettingsLoader,
  private val writer: BankSyncSettingsWriter,
  private val fieldsLoader: MappableFieldsLoader,
  private val linker: BankAccountLinker,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted account: AccountId): BankSyncSettingsViewModel
  }

  private val mutableLoaded = MutableStateFlow<Loaded?>(null)
  private val mutableFailure = MutableStateFlow<BankSyncSettingsState.Failure?>(null)
  private val mutableSettings = MutableStateFlow(BankSyncSettings())
  private val mutableDirection = MutableStateFlow(TransactionDirection.Payment)

  private val mutableEvents =
    MutableSharedFlow<BankSyncSettingsEvent>(
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<BankSyncSettingsEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<BankSyncSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val loaded by mutableLoaded.collectAsState()
      val failure by mutableFailure.collectAsState()
      val settings by mutableSettings.collectAsState()
      val direction by mutableDirection.collectAsState()
      failure?.let {
        return@launchMolecule it
      }
      loaded?.editing(settings, direction) ?: BankSyncSettingsState.Loading
    }

  init {
    viewModelScope.launch {
      try {
        val row = accountDao[account]
        if (row == null) {
          mutableFailure.update { BankSyncSettingsState.Failure(cause = null) }
          return@launch
        }
        val settings = loader.load(account)
        val fields = TransactionDirection.entries.associateWith { fieldsLoader.load(account, it) }
        mutableSettings.update { settings }
        mutableLoaded.update { Loaded(row.name, settings, fields) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading bank sync settings for $account" }
        mutableFailure.update { BankSyncSettingsState.Failure(e.requireMessage()) }
      }
    }
  }

  fun set(toggle: BankSyncToggle, value: Boolean) = mutableSettings.update { s ->
    when (toggle) {
      ImportTransactions -> s.copy(importTransactions = value)
      ImportPending -> s.copy(importPending = value)
      ImportNotes -> s.copy(importNotes = value)
      ReimportDeleted -> s.copy(reimportDeleted = value)
      UpdateDates -> s.copy(updateDates = value)
    }
  }

  fun setDirection(direction: TransactionDirection) = mutableDirection.update { direction }

  /** Maps [field] of transactions in the current direction from the downloaded [value] field. */
  fun setMapping(field: MappedField, value: String) = mutableSettings.update { s ->
    s.copy(mappings = s.mappings.with(mutableDirection.value, field, value))
  }

  fun save() {
    viewModelScope.launch {
      try {
        val settings = mutableSettings.value
        writer.save(account, settings)
        mutableLoaded.update { it?.copy(saved = settings) }
        mutableEvents.emit(BankSyncSettingsEvent.Saved)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed saving bank sync settings for $account" }
        mutableEvents.emit(BankSyncSettingsEvent.SaveFailed(e.requireMessage()))
      }
    }
  }

  /** Stops syncing the account, after which its settings no longer apply. */
  fun unlink() {
    viewModelScope.launch {
      try {
        linker.unlink(account)
        mutableEvents.emit(BankSyncSettingsEvent.Unlinked)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed unlinking $account" }
        mutableEvents.emit(BankSyncSettingsEvent.UnlinkFailed(e.requireMessage()))
      }
    }
  }
}

private data class Loaded(
  val name: String?,
  val saved: BankSyncSettings,
  val fields: Map<TransactionDirection, Map<MappedField, List<FieldExample>>?>,
)

private fun Loaded.editing(settings: BankSyncSettings, direction: TransactionDirection) =
  BankSyncSettingsState.Editing(
    accountName = name,
    importTransactions = settings.importTransactions,
    importPending = settings.importPending,
    importNotes = settings.importNotes,
    reimportDeleted = settings.reimportDeleted,
    updateDates = settings.updateDates,
    direction = direction,
    fields = fields[direction]?.let { rows(it, settings, direction) },
    hasChanges = settings != saved,
  )

private fun rows(
  examples: Map<MappedField, List<FieldExample>>,
  settings: BankSyncSettings,
  direction: TransactionDirection,
): ImmutableList<MappedFieldRow> =
  MappedField.entries
    .map { field ->
      MappedFieldRow(
        field = field,
        selected = settings.mappings[direction][field],
        options =
          examples[field].orEmpty().map { FieldOption(it.field, it.example) }.toImmutableList(),
      )
    }
    .toImmutableList()
