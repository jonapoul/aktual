package aktual.api

import aktual.api.client.AktualClient
import aktual.api.client.TokenExpiredNotifier
import aktual.api.model.account.FailureReason
import aktual.core.model.AktualJson
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

@BindingContainer
@ContributesTo(AppScope::class)
object ClientContainer {
  @Provides
  @SingleIn(AppScope::class)
  @AktualClient
  fun client(
    buildConfig: BuildConfig,
    engine: HttpClientEngine,
    notifier: TokenExpiredNotifier,
  ): HttpClient {
    return buildKtorClient(AktualJson, tag = "ACTUAL", engine, buildConfig.isDebug) {
      HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
          if (exception is ResponseException) {
            try {
              val failure = exception.response.body<ErrorBody>()
              if (failure.reason == FailureReason.TokenExpired.reason) {
                notifier.notifyExpired()
              }
            } catch (e: CancellationException) {
              throw e
            } catch (_: Exception) {
              // Body wasn't parseable as an error response, ignore
            }
          }
          // Always re-throw the original exception
          throw exception
        }
      }
    }
  }
}

@Serializable private data class ErrorBody(val reason: String? = null)
