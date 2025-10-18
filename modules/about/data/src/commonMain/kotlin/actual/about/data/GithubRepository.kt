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
package actual.about.data

import actual.core.model.BuildConfig
import alakazam.kotlin.core.CoroutineContexts
import dev.zacsweers.metro.Inject
import github.api.client.GithubApi
import github.api.model.GithubRelease
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException

@Inject
class GithubRepository internal constructor(
  private val contexts: CoroutineContexts,
  private val apiFactory: GithubApi.Factory,
  private val buildConfig: BuildConfig,
) {
  suspend fun fetchLatestRelease(): LatestReleaseState {
    try {
      val releases = withContext(contexts.io) {
        apiFactory.build().use { api ->
          api.getReleases(
            user = "jonapoul",
            repo = "actual-android",
            perPage = 1,
          )
        }
      }

      val latest = releases.maxByOrNull { it.publishedAt }
      return when {
        releases.isEmpty() || latest == null ->
          LatestReleaseState.NoReleases

        buildConfig.versionName == latest.clippedVersion() ->
          LatestReleaseState.NoNewUpdate

        else -> {
          if (latest.publishedAt > buildConfig.buildTime) {
            LatestReleaseState.UpdateAvailable(latest)
          } else {
            // It's the same version as current (or earlier?)
            LatestReleaseState.NoNewUpdate
          }
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      return e.toFailure()
    }
  }

  private fun Exception.toFailure(): LatestReleaseState {
    val errorMessage = when (this) {
      is ResponseException -> when (response.status) {
        HttpStatusCode.NotFound -> return LatestReleaseState.PrivateRepo
        else -> with(response.status) { "HTTP error $value: $description" }
      }

      is SerializationException, is JsonConvertException -> "Failed decoding JSON: $message"
      is IOException -> "IO failure: $message"
      else -> "Other error - ${javaClass.simpleName}: $message"
    }
    return LatestReleaseState.Failure(errorMessage)
  }

  private fun GithubRelease.clippedVersion(): String = versionName.replace(ANY_LETTER, replacement = "")

  private companion object {
    val ANY_LETTER = "[a-zA-z]".toRegex()
  }
}
