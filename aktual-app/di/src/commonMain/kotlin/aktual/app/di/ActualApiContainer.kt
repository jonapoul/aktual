package aktual.app.di

import aktual.api.buildKtorClient
import aktual.api.client.AktualClient
import aktual.api.client.ThemeClient
import aktual.api.model.account.FailureReason
import aktual.core.l10n.StringLookup
import aktual.core.model.AktualJson
import aktual.core.model.BuildConfig
import aktual.core.utils.LocalNetworkPermission
import aktual.core.utils.LocalNetworkPrompt
import aktual.core.utils.TokenExpiredNotifier
import aktual.core.utils.runCatchingCancellable
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
    localNetworkPermission: LocalNetworkPermission,
    localNetworkPrompt: LocalNetworkPrompt,
    strings: StringLookup,
  ): HttpClient =
    buildKtorClient(AktualJson, tag = "ACTUAL", engine, buildConfig.isDebug) {
      install(localNetworkPlugin(localNetworkPermission, localNetworkPrompt, strings))

      HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
          if (exception is ResponseException) {
            runCatchingCancellable(
              attempt = {
                val isInvalidToken =
                  exception.response.status == Unauthorized &&
                    exception.response.body<ErrorBody>().reason in INVALID_TOKEN_REASONS
                if (isInvalidToken && preferences.token.get() != null) {
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

  // See validateSession() in packages/sync-server/src/util/validate-user.ts upstream
  private val INVALID_TOKEN_REASONS =
    setOf(FailureReason.TokenExpired.reason, FailureReason.Unauthorized.reason)
}

@Serializable private data class ErrorBody(val reason: String? = null)
