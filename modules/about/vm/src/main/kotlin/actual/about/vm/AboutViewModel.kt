package actual.about.vm

import actual.about.data.GithubRepository
import actual.about.data.LatestReleaseState
import actual.core.model.ActualVersions
import actual.core.model.ActualVersionsStateHolder
import alakazam.android.core.UrlOpener
import alakazam.kotlin.core.BuildConfig
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.toJavaInstant

@HiltViewModel
class AboutViewModel @Inject constructor(
  private val buildConfig: BuildConfig,
  private val githubRepository: GithubRepository,
  private val urlOpener: UrlOpener,
  private val actualVersionsStateHolder: ActualVersionsStateHolder,
) : ViewModel() {
  val buildState: StateFlow<BuildState> = viewModelScope.launchMolecule(Immediate) {
    val versions by actualVersionsStateHolder.collectAsState()
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
    urlOpener.openUrl(url)
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

  private fun buildState(versions: ActualVersions): BuildState {
    val zone = ZoneId.systemDefault()
    val zonedDateTime = buildConfig.buildTime.toJavaInstant().atZone(zone)
    return BuildState(
      versions = versions,
      buildDate = DateTimeFormatter.RFC_1123_DATE_TIME.format(zonedDateTime),
      year = zonedDateTime.year,
    )
  }
}
