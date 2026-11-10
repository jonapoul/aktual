package aktual.crashes.vm

import aktual.core.model.CrashFile
import aktual.crashes.data.CrashesRepository
import aktual.crashes.vm.CrashReportsState.Loaded
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class CrashReportsViewModel
internal constructor(
  private val repository: CrashesRepository,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableState = MutableStateFlow<CrashReportsState>(Loading)
  val state: StateFlow<CrashReportsState> = mutableState.asStateFlow()

  init {
    load()
  }

  fun load() {
    viewModelScope.launch {
      val reports = repository.reports()
      mutableState.update {
        if (reports.isEmpty()) Empty else Loaded(reports.toImmutableList())
      }
    }
  }

  fun delete(file: CrashFile) {
    viewModelScope.launch {
      log.i { "Deleting crash report $file" }
      repository.delete(file)
      load()
    }
  }

  fun deleteAll() {
    viewModelScope.launch {
      log.i { "Deleting all crash reports" }
      repository.deleteAll()
      load()
    }
  }
}
