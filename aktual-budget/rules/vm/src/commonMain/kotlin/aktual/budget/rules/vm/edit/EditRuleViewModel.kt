package aktual.budget.rules.vm.edit

import aktual.budget.model.RuleId
import aktual.budget.rules.vm.Rule
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("unused")
@Stable
@AssistedInject
class EditRuleViewModel(@Assisted private val id: RuleId?) : ViewModel() {
  private val mutableEvents =
    MutableSharedFlow<EditRuleEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )

  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableIsDeleting = MutableStateFlow(false)
  private val mutableRule = MutableStateFlow<Rule?>(null)

  val events: SharedFlow<EditRuleEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<EditRuleState> =
    viewModelScope.launchMolecule(Immediate) {
      val isLoading by mutableIsLoading.collectAsState()
      val isDeleting by mutableIsDeleting.collectAsState()
      val rule by mutableRule.collectAsState()

      // TBC
      EditRuleState.Loading
    }

  init {
    loadData()
  }

  private fun loadData() {
    mutableIsLoading.update { true }
    viewModelScope.launch {
      // TBC
    }
  }

  fun delete(id: RuleId) {
    mutableIsDeleting.update { true }

    // TBC

    mutableEvents.tryEmit(EditRuleEvent.DeletedRule)
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: RuleId?): EditRuleViewModel
  }
}
