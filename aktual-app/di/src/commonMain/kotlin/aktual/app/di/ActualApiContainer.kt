package aktual.app.di

import aktual.api.buildKtorClient
import aktual.api.client.AktualClient
import aktual.api.client.ThemeClient
import aktual.api.model.account.FailureReason
import aktual.core.TokenExpiredNotifier
import aktual.core.model.AktualJson
import aktual.core.model.BuildConfig
import aktual.core.runCatchingCancellable
import aktual.di.AppScope
import aktual.prefs.AppPreferences
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.Serializable

@BindingContainer
@ContributesTo(AppScope::class)
object ActualApiContainer {
  @Provides
  @SingleIn(AppScope::class)
  @AktualClient
  fun aktualClient(
    buildConfig: BuildConfig,
    engine: HttpClientEngine,
    notifyTokenExpired: TokenExpiredNotifier,
    preferences: AppPreferences,
  ): HttpClient =
    buildKtorClient(AktualJson, tag = "ACTUAL", engine, buildConfig.isDebug) {
      HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
          if (exception is ResponseException) {
            runCatchingCancellable(
              attempt = {
                val isExpired =
                  exception.response.body<ErrorBody>().reason == FailureReason.TokenExpired.reason
                if (isExpired && preferences.token.get() != null) {
                  // Only for authenticated sessions. At login there's no stored token yet, so this
                  // would clear the backstack while already on the login screen
                  notifyTokenExpired()
                }
              },
              onFailure = {
                // Body wasn't parseable as an error response, ignore
              },
            )
          }
          throw exception
        }
      }
    }

  @Provides
  @ThemeClient
  fun themeClient(buildConfig: BuildConfig, engine: HttpClientEngine): HttpClient =
    buildKtorClient(AktualJson, tag = "THEME", engine, buildConfig.isDebug)
}

@Serializable private data class ErrorBody(val reason: String? = null)
