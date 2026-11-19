package aktual.updates.vm

import aktual.core.model.BuildConfig
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.updates.domain.NightlyBuild
import aktual.updates.domain.NightlyUpdates
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
  updates: NightlyUpdates,
  private val buildConfig: BuildConfig,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<NightlyUpdateState> =
    updates.latest
      .map { build -> build?.toState() ?: None }
      .stateIn(viewModelScope, Eagerly, initialValue = None)

  private fun NightlyBuild.toState(): NightlyUpdateState =
    NightlyUpdateState.Available(
      installed = Version(name = buildConfig.baseVersionName, gitHash = buildConfig.gitHash),
      nightly =
        Version(
          name = commitTime.toLocalDateTime(TimeZone.UTC).date.format(VersionFormat),
          gitHash = gitHash,
        ),
      commitsAhead = commitsAhead,
      apkUrl = apkUrl,
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
