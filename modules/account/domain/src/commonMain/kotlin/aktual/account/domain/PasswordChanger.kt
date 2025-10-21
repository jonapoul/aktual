/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.account.domain

import aktual.api.client.AktualApisStateHolder
import aktual.api.model.account.ChangePasswordRequest
import aktual.api.model.account.ChangePasswordResponse
import aktual.core.model.Password
import aktual.prefs.AppGlobalPreferences
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
  private val apisStateHolder: AktualApisStateHolder,
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
