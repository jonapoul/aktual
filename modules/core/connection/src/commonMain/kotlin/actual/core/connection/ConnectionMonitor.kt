package actual.core.connection

import actual.api.builder.buildOkHttp
import actual.api.builder.buildRetrofit
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.url.model.ServerUrl
import actual.url.prefs.ServerUrlPreferences
import alakazam.kotlin.core.BuildConfig
import alakazam.kotlin.core.collectFlow
import alakazam.kotlin.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import retrofit2.create
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionMonitor @Inject constructor(
  private val scope: CoroutineScope,
  private val buildConfig: BuildConfig,
  private val apiStateHolder: ActualApisStateHolder,
  private val serverUrlPreferences: ServerUrlPreferences,
) {
  private var job: Job? = null

  fun start() {
    job?.cancel()
    job = scope.collectFlow(serverUrlPreferences.url.asFlow()) { url ->
      Logger.v("ConnectionMonitor url=%s", url)
      if (url == null) {
        apiStateHolder.reset()
      } else {
        tryToBuildApis(url)
      }
    }
  }

  private fun tryToBuildApis(url: ServerUrl) = try {
    val client = buildOkHttp(isDebug = buildConfig.debug, tag = "ACTUAL HTTP")
    val retrofit = buildRetrofit(client, url, ActualJson)
    val apis = ActualApis(
      serverUrl = url,
      account = retrofit.create(),
      base = retrofit.create(),
      sync = retrofit.create(),
    )
    apiStateHolder.update { apis }
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    Logger.w(e, "Failed building APIs for $url")
    apiStateHolder.reset()
  }

  fun stop() {
    job?.cancel()
    job = null
  }
}
