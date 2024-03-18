package dev.jonpoulton.actual.core.connection

import alakazam.kotlin.core.collectFlow
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.serverurl.prefs.ServerUrlPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionMonitor @Inject constructor(
  private val scope: CoroutineScope,
  private val apiStateHolder: ActualApisStateHolder,
  private val serverUrlPreferences: ServerUrlPreferences,
) {
  private var job: Job? = null

  fun start() {
    job?.cancel()
    job = scope.collectFlow(serverUrlPreferences.url.asFlow()) { url ->
      Timber.v("ConnectionMonitor url=$url")
      if (url == null) {
        apiStateHolder.reset()
      } else {
        tryToBuildApis(url)
      }
    }
  }

  private fun tryToBuildApis(url: ServerUrl) = try {
    val client = buildOkHttp()
    val retrofit = buildRetrofit(client, url)
    val apis = buildApis(retrofit, url)
    apiStateHolder.set(apis)
  } catch (e: Exception) {
    Timber.w(e, "Failed building APIs for $url")
    apiStateHolder.reset()
  }

  fun stop() {
    job?.cancel()
    job = null
  }
}
