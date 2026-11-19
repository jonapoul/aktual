package aktual.updates.domain

import aktual.core.model.BuildConfig
import aktual.core.utils.runCatchingCancellable
import aktual.di.AppCoroutineScope
import aktual.di.AppScope
import aktual.log
import aktual.prefs.AppPreferences
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import github.api.GithubApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class NightlyUpdatesImpl
internal constructor(
  private val githubApi: GithubApi,
  private val buildConfig: BuildConfig,
  private val contexts: CoroutineContexts,
  installer: ApkInstaller,
  preferences: AppPreferences,
  scope: AppCoroutineScope,
) : NightlyUpdates {
  // Nightlies are release builds, so they'd only install over another release build
  override val isSupported: Boolean = buildConfig.isRelease && installer.isSupported

  override val latest: StateFlow<NightlyBuild?> =
    preferences.checkForNightlyUpdates
      .asFlow()
      .distinctUntilChanged()
      .mapLatest { enabled -> if (enabled && isSupported) fetch() else null }
      .stateIn(scope, Lazily, initialValue = null)

  private suspend fun fetch(): NightlyBuild? =
    runCatchingCancellable(
      attempt = { withContext(contexts.io) { findNewer() } },
      onFailure = { e ->
        log.w(e) { "Failed checking for nightly builds" }
        null
      },
    )

  private suspend fun findNewer(): NightlyBuild? {
    val (user, repo) = buildConfig.repoName.split("/")
    val run =
      githubApi
        .getWorkflowRuns(user, repo, WORKFLOW, branch = BRANCH, status = SUCCESS, perPage = 1)
        .runs
        .firstOrNull() ?: return null
    if (run.headSha.startsWith(buildConfig.gitHash)) return null

    // Not just a different commit, since a local build can be newer than the nightly
    val comparison = githubApi.compare(user, repo, base = buildConfig.gitHash, head = run.headSha)
    if (comparison.status != AHEAD) return null

    return NightlyBuild(
      runId = run.id,
      gitHash = run.headSha.take(buildConfig.gitHash.length),
      commitTime = run.headCommit.timestamp,
      commitsAhead = comparison.aheadBy,
    )
  }

  private companion object {
    const val WORKFLOW = "nightly.yml"
    const val BRANCH = "main"
    const val SUCCESS = "success"
    const val AHEAD = "ahead"
  }
}
