package aktual.app.di

import aktual.api.buildKtorClient
import aktual.api.client.NominatimClient
import aktual.core.model.BuildConfig
import aktual.di.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import nominatim.api.client.NominatimJson

@BindingContainer
@ContributesTo(AppScope::class)
object NominatimApiContainer {
  @Provides
  @NominatimClient
  @SingleIn(AppScope::class)
  fun client(buildConfig: BuildConfig, engine: HttpClientEngine): HttpClient =
    buildKtorClient(NominatimJson, tag = "NOMINATIM", engine, buildConfig.isDebug) {
      // Nominatim's usage policy asks for an identifying user agent
      defaultRequest {
        headers.append(
          HttpHeaders.UserAgent,
          "Aktual/${buildConfig.versionName} (+${buildConfig.repoUrl})",
        )
      }
    }
}
