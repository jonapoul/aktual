package aktual.core.connection

import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.ApiBuilder
import aktual.core.model.ServerUrl
import aktual.core.prefs.AppGlobalPreferences
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.collectFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import logcat.logcat

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ConnectionMonitorImpl(
  private val scope: CoroutineScope,
  private val contexts: CoroutineContexts,
  private val apiStateHolder: AktualApisStateHolder,
  private val preferences: AppGlobalPreferences,
  private val apiBuilder: ApiBuilder.Factory,
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
      tryToBuildApis(url)
    }
  }

  private fun tryToBuildApis(url: ServerUrl) {
    try {
      val apis =
        with(apiBuilder.create(url)) {
          AktualApis(
            serverUrl = url,
            account = account(),
            base = base(),
            health = health(),
            metrics = metrics(),
            sync = sync(),
          )
        }
      apiStateHolder.update { apis }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed building APIs for $url" }
      apiStateHolder.reset()
    }
  }
}
