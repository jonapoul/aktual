package aktual.api

import aktual.api.client.AktualClient
import aktual.core.model.AktualJson
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

@BindingContainer
@ContributesTo(AppScope::class)
object ClientContainer {
  @Provides
  @SingleIn(AppScope::class)
  @AktualClient
  fun client(buildConfig: BuildConfig, engine: HttpClientEngine): HttpClient =
    buildKtorClient(AktualJson, tag = "ACTUAL", engine, buildConfig.isDebug)
}
