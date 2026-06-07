package aktual.about.vm

import aktual.about.data.GithubRepository
import aktual.about.data.LatestReleaseState
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.BuildConfig
import aktual.core.model.UrlOpener
import aktual.di.AppScope
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
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.toJavaInstant
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class AboutViewModel(
  @Assisted private val savedState: SavedStateHandle,
  private val buildConfig: BuildConfig,
  private val githubRepository: GithubRepository,
  private val urlOpener: UrlOpener,
  private val aktualVersionsStateHolder: AktualVersionsStateHolder,
) : ViewModel() {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(AboutViewModel::class)
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): AboutViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): AboutViewModel
  }

  val buildState: StateFlow<BuildState> =
    viewModelScope.launchMolecule(Immediate) {
      val versions by aktualVersionsStateHolder.collectAsState()
      buildState(versions)
    }

  private val mutableCheckUpdatesState = MutableStateFlow(restoreCheckUpdatesState())
  val checkUpdatesState: StateFlow<CheckUpdatesState> = mutableCheckUpdatesState.asStateFlow()

  private var checkUpdatesJob: Job? = null

  init {
    savedState.setSavedStateProvider(KEY_CHECK_UPDATES) {
      encodeToSavedState(mutableCheckUpdatesState.value)
    }
  }

  private fun restoreCheckUpdatesState(): CheckUpdatesState {
    val restored =
      savedState.get<SavedState>(KEY_CHECK_UPDATES)?.let {
        decodeFromSavedState<CheckUpdatesState>(it)
      }

    return when (restored) {
      null -> CheckUpdatesState.Inactive
      is CheckUpdatesState.Checking -> CheckUpdatesState.Inactive
      else -> restored
    }
  }

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

          is LatestReleaseState.UpdateAvailable ->
            CheckUpdatesState.UpdateFound(
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

  private companion object {
    const val KEY_CHECK_UPDATES = "check_updates_state"
  }
}
