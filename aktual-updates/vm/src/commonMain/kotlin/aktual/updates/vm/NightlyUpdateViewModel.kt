package aktual.updates.vm

import aktual.core.model.BuildConfig
import aktual.core.utils.runCatchingCancellable
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import aktual.updates.domain.NightlyBuild
import aktual.updates.domain.NightlyInstaller
import aktual.updates.domain.NightlyUpdates
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class NightlyUpdateViewModel
internal constructor(
  private val updates: NightlyUpdates,
  private val installer: NightlyInstaller,
  private val buildConfig: BuildConfig,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val installState = MutableStateFlow<InstallState>(Idle)

  val state: StateFlow<NightlyUpdateState> =
    combine(updates.latest, installState) { build, install ->
        if (build == null) NightlyUpdateState.None else build.toState(install)
      }
      .stateIn(viewModelScope, Eagerly, initialValue = None)

  fun install() {
    val build = updates.latest.value ?: return
    if (installState.value is Downloading) return
    installState.update { InstallState.Downloading(progress = null) }
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          installer.install(build) { progress ->
            installState.update { InstallState.Downloading(progress) }
          }
          installState.update { Idle }
        },
        onFailure = { e ->
          log.e(e) { "Failed installing nightly ${build.gitHash}" }
          installState.update { Failed }
        },
      )
    }
  }

  private fun NightlyBuild.toState(install: InstallState) =
    NightlyUpdateState.Available(
      installed = Version(name = buildConfig.baseVersionName, gitHash = buildConfig.gitHash),
      nightly =
        Version(
          name = commitTime.toLocalDateTime(TimeZone.UTC).date.format(VersionFormat),
          gitHash = gitHash,
        ),
      commitsAhead = commitsAhead,
      install = install,
    )

  private companion object {
    // Matches the version names the build gives each commit
    val VersionFormat = LocalDate.Format {
      year()
      char('.')
      monthNumber()
      char('.')
      day()
    }
  }
}
