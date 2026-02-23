package aktual.core.theme

import aktual.api.buildKtorClient
import aktual.core.model.AktualJson
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient

@BindingContainer
@ContributesTo(AppScope::class)
object ThemeApiContainer {
  @Provides
  @ThemeClient
  fun client(buildConfig: BuildConfig): HttpClient =
    buildKtorClient(AktualJson, tag = "THEME", isDebug = buildConfig.isDebug)
}
