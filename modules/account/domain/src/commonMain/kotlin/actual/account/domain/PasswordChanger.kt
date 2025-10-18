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
package actual.account.domain

import actual.api.client.ActualApisStateHolder
import actual.api.model.account.ChangePasswordRequest
import actual.api.model.account.ChangePasswordResponse
import actual.core.model.Password
import actual.prefs.AppGlobalPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat
import java.io.IOException

@Inject
class PasswordChanger internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val preferences: AppGlobalPreferences,
) {
  suspend fun submit(password: Password): ChangePasswordResult {
    val apis = apisStateHolder.value
    val token = preferences.loginToken.get()
    if (apis == null || token == null) {
      return ChangePasswordResult.NotLoggedIn
    }

    return try {
      val request = ChangePasswordRequest(password)
      val response = withContext(contexts.io) { apis.account.changePassword(request, token) }
      logcat.v { "Received response: $response" }
      ChangePasswordResult.Success
    } catch (e: CancellationException) {
      throw e
    } catch (e: ResponseException) {
      val body = e.response.body<ChangePasswordResponse.Failure>()
      logcat.e(e) { "HTTP failure changing password: status=${e.response.status}, body=$body" }
      ChangePasswordResult.HttpFailure(e.response.status.value, body.reason.reason)
    } catch (e: IOException) {
      logcat.e(e) { "Network failure changing password" }
      ChangePasswordResult.NetworkFailure
    } catch (e: Exception) {
      logcat.e(e) { "Failed changing password" }
      ChangePasswordResult.OtherFailure(e.requireMessage())
    }
  }
}
