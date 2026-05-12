package aktual.app.di

import aktual.api.buildKtorClient
import aktual.core.model.AktualJson
import aktual.core.model.BuildConfig
import aktual.core.theme.ThemeClient
import aktual.di.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

@BindingContainer
@ContributesTo(AppScope::class)
object ThemeApiContainer {
  @Provides
  @ThemeClient
  fun client(buildConfig: BuildConfig, engine: HttpClientEngine): HttpClient =
    buildKtorClient(AktualJson, tag = "THEME", engine, buildConfig.isDebug)
}
