package aktual.about.vm

import aktual.about.data.GithubRepository
import aktual.about.data.LatestReleaseState
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.BuildConfig
import aktual.core.model.UrlOpener
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.toJavaInstant

@Inject
@ViewModelKey(AboutViewModel::class)
@ContributesIntoMap(AppScope::class)
class AboutViewModel(
  private val buildConfig: BuildConfig,
  private val githubRepository: GithubRepository,
  private val urlOpener: UrlOpener,
  private val aktualVersionsStateHolder: AktualVersionsStateHolder,
) : ViewModel() {
  val buildState: StateFlow<BuildState> = viewModelScope.launchMolecule(Immediate) {
    val versions by aktualVersionsStateHolder.collectAsStateWithLifecycle()
    buildState(versions)
  }

  private val mutableCheckUpdatesState = MutableStateFlow<CheckUpdatesState>(CheckUpdatesState.Inactive)
  val checkUpdatesState: StateFlow<CheckUpdatesState> = mutableCheckUpdatesState.asStateFlow()

  private var checkUpdatesJob: Job? = null

  fun openRepo() {
    openUrl(url = buildConfig.repoUrl)
  }

  fun reportIssues() {
    openUrl(url = "${buildConfig.repoUrl}/issues/new")
  }

  fun openUrl(url: String) {
    logcat.d { "openUrl $url" }
    urlOpener(url)
  }

  fun fetchLatestRelease() {
    logcat.d { "fetchLatestRelease" }
    checkUpdatesJob?.cancel()
    mutableCheckUpdatesState.update { CheckUpdatesState.Checking }

    checkUpdatesJob = viewModelScope.launch {
      val state = githubRepository.fetchLatestRelease()
      mutableCheckUpdatesState.update {
        when (state) {
          LatestReleaseState.NoNewUpdate -> CheckUpdatesState.NoUpdateFound

          LatestReleaseState.NoReleases -> CheckUpdatesState.NoUpdateFound

          LatestReleaseState.PrivateRepo -> CheckUpdatesState.Failed(cause = "Repo inaccessible")

          is LatestReleaseState.Failure -> CheckUpdatesState.Failed(state.errorMessage)

          is LatestReleaseState.UpdateAvailable -> CheckUpdatesState.UpdateFound(
            version = state.release.versionName,
            url = state.release.htmlUrl,
          )
        }
      }
    }
  }

  fun cancelUpdateCheck() {
    logcat.d { "cancelUpdateCheck" }
    checkUpdatesJob?.cancel()
    checkUpdatesJob = null
    mutableCheckUpdatesState.update { CheckUpdatesState.Inactive }
  }

  private fun buildState(versions: AktualVersions): BuildState {
    val zone = ZoneId.systemDefault()
    val zonedDateTime = buildConfig.buildTime.toJavaInstant().atZone(zone)
    return BuildState(
      versions = versions,
      buildDate = DateTimeFormatter.RFC_1123_DATE_TIME.format(zonedDateTime),
      year = zonedDateTime.year,
    )
  }
}
