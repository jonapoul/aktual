package aktual.budget.rules.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.model.RuleId
import aktual.budget.model.tombstone
import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.RulesLoader
import aktual.budget.rules.vm.list.CheckboxesState.Active
import aktual.budget.rules.vm.list.CheckboxesState.Inactive
import aktual.budget.rules.vm.list.ListRulesState.Empty
import aktual.budget.rules.vm.list.ListRulesState.Failure
import aktual.budget.rules.vm.list.ListRulesState.Loading
import aktual.budget.rules.vm.list.ListRulesState.Success
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logcat.logcat

@Stable
@AssistedInject
class ListRulesViewModel(
  @Assisted private val savedState: SavedStateHandle,
  private val loader: RulesLoader,
  private val syncController: BudgetSyncController,
  val nameFetcher: NameFetcher,
) : ViewModel() {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(ListRulesViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): ListRulesViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): ListRulesViewModel
  }

  private val mutableRules = MutableStateFlow<ImmutableList<Rule>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private val mutableCheckboxes = MutableStateFlow(restoreCheckboxes())

  val checkboxes: StateFlow<CheckboxesState> = mutableCheckboxes.asStateFlow()

  val state: StateFlow<ListRulesState> =
    viewModelScope.launchMolecule(Immediate) {
      val rules by mutableRules.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        rules.isEmpty() -> Empty
        else -> Success(rules)
      }
    }

  init {
    savedState.setSavedStateProvider(KEY_CHECKBOXES) {
      encodeToSavedState(mutableCheckboxes.value.toSnapshot())
    }
    reload()
  }

  private fun restoreCheckboxes(): CheckboxesState =
    savedState.get<SavedState>(KEY_CHECKBOXES)?.let {
      decodeFromSavedState<CheckboxesSnapshot>(it).toState()
    } ?: Inactive

  fun reload() {
    mutableIsLoading.update { true }
    viewModelScope.launch {
      try {
        val rules = loader.load()
        mutableRules.update { rules }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading all rules" }
        mutableFailure.update { e.requireMessage() }
      } finally {
        mutableIsLoading.update { false }
      }
    }
  }

  fun delete(id: RuleId) = delete(persistentSetOf(id))

  fun delete(ids: ImmutableSet<RuleId>) {
    viewModelScope.launch {
      val changes = ids.map { id -> tombstone(dataset = RULES, row = id.toString()) }
      syncController.syncChanges(changes)
      mutableCheckboxes.update { Inactive }
      reload()
      logcat.d { "Tombstoned ${ids.size} rules: $ids" }
    }
  }

  fun showCheckboxes() = mutableCheckboxes.update { Active(ids = persistentSetOf()) }

  fun hideCheckboxes() = mutableCheckboxes.update { Inactive }

  fun check(id: RuleId) = mutableCheckboxes.update { it + id }

  fun uncheck(id: RuleId) = mutableCheckboxes.update { it - id }

  fun uncheckAll() = showCheckboxes()

  @Serializable
  private data class CheckboxesSnapshot(val active: Boolean, val ids: Set<RuleId>) {
    fun toState(): CheckboxesState = if (active) Active(ids.toImmutableSet()) else Inactive
  }

  private fun CheckboxesState.toSnapshot(): CheckboxesSnapshot =
    when (this) {
      Inactive -> CheckboxesSnapshot(active = false, ids = emptySet())
      is Active -> CheckboxesSnapshot(active = true, ids = ids)
    }

  private companion object {
    const val KEY_CHECKBOXES = "checkboxes_state"
  }
}
