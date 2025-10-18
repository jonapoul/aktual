/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.core.connection

import actual.api.builder.ClientFactory
import actual.api.client.AccountApi
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.client.BaseApi
import actual.api.client.HealthApi
import actual.api.client.MetricsApi
import actual.api.client.SyncApi
import actual.api.client.SyncDownloadApi
import actual.core.model.ServerUrl
import actual.prefs.AppGlobalPreferences
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
  private val apiStateHolder: ActualApisStateHolder,
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
    val client = clientFactory(ActualJson)
    val apis = ActualApis(
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
