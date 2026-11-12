package aktual.budget.payees.vm.edit

import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.budget.model.localChange
import aktual.budget.payees.domain.AddressLookup
import aktual.budget.payees.domain.PayeeLocations
import aktual.budget.payees.vm.PayeesLoader
import aktual.budget.payees.vm.list.PayeeItem
import aktual.budget.rules.domain.CategoryLearningPref
import aktual.budget.utils.BudgetSyncController
import aktual.core.model.Coordinates
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.TimeZoneProvider
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime

@Stable
@AssistedInject
class EditPayeeViewModel
internal constructor(
  @Assisted private val payeeId: PayeeId,
  loader: PayeesLoader,
  private val payeeDao: PayeeDao,
  private val syncController: BudgetSyncController,
  private val payeeLocations: PayeeLocations,
  private val addressLookup: AddressLookup,
  private val budgetScope: BudgetCoroutineScope,
  learning: CategoryLearningPref,
  timeZones: TimeZoneProvider,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val loaded = MutableStateFlow<Loaded?>(null)
  private val otherNames = MutableStateFlow(emptySet<String>())
  private val mutableName = MutableStateFlow("")
  private val mutableFavorite = MutableStateFlow(false)
  private val mutableLearnCategories = MutableStateFlow(false)

  private val mutableSaveError = MutableStateFlow<String?>(null)
  val saveError: StateFlow<String?> = mutableSaveError.asStateFlow()

  val isFavorite: StateFlow<Boolean> = mutableFavorite.asStateFlow()

  val learnsCategories: StateFlow<Boolean> = mutableLearnCategories.asStateFlow()

  // The per-payee switch only matters, and only shows, while the global pref is on
  val isLearningEnabled: StateFlow<Boolean> =
    learning.observe().stateIn(viewModelScope, Eagerly, initialValue = false)

  private val mutableEvents =
    MutableSharedFlow<EditPayeeEvent>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
  val events: SharedFlow<EditPayeeEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<EditPayeeState> =
    loader
      .observe()
      .map(::toState)
      .catch { e ->
        log.e(e) { "Failed loading payee $payeeId" }
        emit(Failure(e.requireMessage()))
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  // Where the payee's been saved from the transaction editor, newest first
  val locations: StateFlow<ImmutableList<SavedLocation>> =
    combine(payeeLocations.observe(payeeId), addressLookup.addresses) { locations, book ->
        locations
          .map { location ->
            SavedLocation(
              location.id,
              location.coordinates,
              location.createdAt?.toLocalDateTime(timeZones.get())?.date,
              book[location.coordinates],
            )
          }
          .toImmutableList()
      }
      .catch { e ->
        log.e(e) { "Failed loading locations of $payeeId" }
        emit(persistentListOf())
      }
      .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())

  val hasUnsavedChanges: StateFlow<Boolean> =
    combine(loaded, mutableName, mutableFavorite, mutableLearnCategories) { l, name, favorite, learn
        ->
        l != null && (name != l.name || favorite != l.isFavorite || learn != l.learnsCategories)
      }
      .stateIn(viewModelScope, Eagerly, initialValue = false)

  // Upstream allows payees to share a name, so this only warns
  val isDuplicateName: StateFlow<Boolean> =
    combine(mutableName, otherNames) { name, others -> name.trim().lowercase() in others }
      .stateIn(viewModelScope, Eagerly, initialValue = false)

  val canSave: StateFlow<Boolean> =
    combine(state, mutableName) { s, name ->
        s is Editing && !s.payee.isTransfer && name.isNotBlank()
      }
      .stateIn(viewModelScope, Eagerly, initialValue = false)

  private fun toState(payees: List<PayeeItem>): EditPayeeState {
    val payee = payees.firstOrNull { it.id == payeeId } ?: return Failure(cause = null)
    otherNames.update {
      payees.mapNotNullTo(HashSet()) { p ->
        p.name.lowercase().takeIf { p.id != payeeId && !p.isTransfer }
      }
    }

    // Seed the working edits once, so later reloads don't overwrite what's been typed
    if (loaded.value == null) {
      mutableName.update { payee.name }
      mutableFavorite.update { payee.isFavorite }
      mutableLearnCategories.update { payee.learnsCategories }
      loaded.update { Loaded(payee.name, payee.isFavorite, payee.learnsCategories) }
    }
    return Editing(payee)
  }

  fun setName(name: String) = mutableName.update { name }

  fun setFavorite(isFavorite: Boolean) = mutableFavorite.update { isFavorite }

  fun setLearnCategories(learn: Boolean) = mutableLearnCategories.update { learn }

  fun dismissSaveError() = mutableSaveError.update { null }

  fun deleteLocation(id: PayeeLocationId) {
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          payeeLocations.delete(id)
          mutableEvents.tryEmit(EditPayeeEvent.LocationDeleted(id))
        },
        onFailure = { e ->
          log.e(e) { "Failed deleting location $id" }
          mutableEvents.tryEmit(LocationDeleteFailed)
        },
      )
    }
  }

  // The undo snackbar can outlive this screen, so the restore can't be tied to it
  fun requestAddress(coordinates: Coordinates) = addressLookup.request(coordinates)

  fun restoreLocation(id: PayeeLocationId) {
    budgetScope.launch {
      runCatchingCancellable(
        attempt = { payeeLocations.restore(id) },
        onFailure = { e ->
          log.e(e) { "Failed restoring location $id" }
          mutableEvents.tryEmit(LocationRestoreFailed)
        },
      )
    }
  }

  fun save() {
    viewModelScope.launch {
      mutableSaveError.update { null }
      runCatchingCancellable(
        attempt = {
          val name = mutableName.value.trim()
          val favorite = mutableFavorite.value
          val learn = mutableLearnCategories.value
          val row = payeeId.value
          payeeDao.update(payeeId, name, favorite, learn)
          syncController.syncChanges(
            localChange(PAYEES, row, "name", name),
            localChange(PAYEES, row, "favorite", favorite),
            localChange(PAYEES, row, "learn_categories", learn),
          )
          mutableEvents.tryEmit(FinishedSaving)
        },
        onFailure = { e ->
          log.e(e) { "Failed saving payee $payeeId" }
          mutableSaveError.update { e.requireMessage() }
        },
      )
    }
  }

  private data class Loaded(
    val name: String,
    val isFavorite: Boolean,
    val learnsCategories: Boolean,
  )

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(payeeId: PayeeId): EditPayeeViewModel
  }
}
