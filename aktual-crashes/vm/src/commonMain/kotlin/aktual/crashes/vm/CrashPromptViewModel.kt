package aktual.crashes.vm

import aktual.core.logging.CrashReport
import aktual.crashes.data.CrashesRepository
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import aktual.prefs.AppPreferences
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Offers to show the newest crash report on launch, if the user hasn't seen it yet
@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class CrashPromptViewModel
internal constructor(
  private val repository: CrashesRepository,
  private val preferences: AppPreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableUnseen = MutableStateFlow<CrashReport?>(null)
  val unseen: StateFlow<CrashReport?> = mutableUnseen.asStateFlow()

  init {
    viewModelScope.launch {
      val newest = repository.reports().firstOrNull() ?: return@launch
      val lastSeen = preferences.lastSeenCrash.get()
      if (lastSeen == null || newest.file.name > lastSeen.name) {
        log.i { "Unseen crash report ${newest.file}" }
        mutableUnseen.update { newest }
      }
    }
  }

  fun markSeen() {
    val report = mutableUnseen.value ?: return
    mutableUnseen.update { null }
    viewModelScope.launch { preferences.lastSeenCrash.set(report.file) }
  }
}
