package aktual.core.connection

import aktual.api.client.AccountApi
import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.AktualClient
import aktual.api.client.BaseApi
import aktual.api.client.HealthApi
import aktual.api.client.MetricsApi
import aktual.api.client.SyncApi
import aktual.api.client.SyncDownloadApi
import aktual.core.model.ServerUrl
import aktual.core.prefs.AppGlobalPreferences
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.collectFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import logcat.logcat
import okio.FileSystem

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ConnectionMonitorImpl(
  private val scope: CoroutineScope,
  private val contexts: CoroutineContexts,
  @param:AktualClient private val client: HttpClient,
  private val apiStateHolder: AktualApisStateHolder,
  private val preferences: AppGlobalPreferences,
  private val fileSystem: FileSystem,
) : ConnectionMonitor {
  private var job: Job? = null

  override fun start() {
    job?.cancel()
    job =
      scope.collectFlow(
        flow = preferences.serverUrl.asFlow(),
        context = contexts.unconfined,
        collector = ::handleServerUrl,
      )
  }

  override fun stop() {
    job?.cancel()
    job = null
  }

  private fun handleServerUrl(url: ServerUrl?) {
    logcat.v { "handleServerUrl url=$url" }
    if (url == null) {
      apiStateHolder.reset()
    } else {
      // Close previous client's resources before rebuilding
      apiStateHolder.value?.close()
      tryToBuildApis(url)
    }
  }

  private fun tryToBuildApis(url: ServerUrl) {
    try {
      val apis =
        AktualApis(
          serverUrl = url,
          client = client,
          account = AccountApi(url, client),
          base = BaseApi(url, client),
          health = HealthApi(url, client),
          metrics = MetricsApi(url, client),
          sync = SyncApi(url, client),
          syncDownload = SyncDownloadApi(url, client, fileSystem),
        )
      apiStateHolder.update { apis }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed building APIs for $url" }
      apiStateHolder.reset()
    }
  }
}
