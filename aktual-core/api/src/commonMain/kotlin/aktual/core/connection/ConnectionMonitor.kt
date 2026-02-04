package aktual.core.connection

import aktual.core.api.ClientFactory
import aktual.api.client.AccountApi
import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.AktualJson
import aktual.api.client.BaseApi
import aktual.api.client.HealthApi
import aktual.api.client.MetricsApi
import aktual.api.client.SyncApi
import aktual.api.client.SyncDownloadApi
import aktual.core.model.ServerUrl
import aktual.core.prefs.AppGlobalPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.collectFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import logcat.logcat
import okio.FileSystem

@Inject
@SingleIn(AppScope::class)
class ConnectionMonitor(
  private val scope: CoroutineScope,
  private val contexts: CoroutineContexts,
  private val clientFactory: ClientFactory,
  private val apiStateHolder: AktualApisStateHolder,
  private val preferences: AppGlobalPreferences,
  private val fileSystem: FileSystem,
) {
  private var job: Job? = null

  fun start() {
    job?.cancel()
    job = scope.collectFlow(
      flow = preferences.serverUrl.asFlow(),
      context = contexts.unconfined,
      call = ::handleServerUrl,
    )
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

  private fun tryToBuildApis(url: ServerUrl) = try {
    val client = clientFactory(AktualJson)
    val apis = AktualApis(
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

  fun stop() {
    job?.cancel()
    job = null
  }
}
