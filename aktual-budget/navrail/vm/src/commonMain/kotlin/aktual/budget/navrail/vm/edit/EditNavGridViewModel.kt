package aktual.budget.navrail.vm.edit

import aktual.core.nav.BudgetTab
import aktual.di.AppScope
import aktual.prefs.AppPreferences
import aktual.prefs.asStateFlow
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class EditNavGridViewModel(private val prefs: AppPreferences) : ViewModel() {
  // null = "follow the saved order"; non-null = the user is editing a working copy
  private val working = MutableStateFlow<ImmutableList<BudgetTab>?>(null)
  private val savedNames = prefs.navGridOrder.asStateFlow(viewModelScope)

  val state: StateFlow<EditNavGridState> =
    viewModelScope.launchMolecule(Immediate) {
      val names by savedNames.collectAsState()
      val workingOrder by working.collectAsState()
      val saved = reconcileNavGridOrder(names)
      val current = workingOrder ?: saved
      EditNavGridState(
        items = current,
        hasChanges = current != saved,
        isDefault = current == DEFAULT_NAV_GRID_ORDER,
      )
    }

  fun move(from: Int, to: Int) {
    working.update { current ->
      val base = current ?: reconcileNavGridOrder(savedNames.value)
      base.toMutableList().apply { add(to, removeAt(from)) }.toImmutableList()
    }
  }

  fun save() {
    val toSave = working.value ?: return
    viewModelScope.launch {
      prefs.navGridOrder.set(toSave.map { it.name })
      working.value = null
    }
  }

  fun resetToSaved() {
    working.value = null
  }

  fun resetToDefault() {
    working.value = DEFAULT_NAV_GRID_ORDER
  }
}
