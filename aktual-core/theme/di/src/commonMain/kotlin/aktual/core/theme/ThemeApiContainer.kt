package aktual.core.theme

import aktual.api.client.AktualJson
import aktual.core.api.buildKtorClient
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object ThemeApiContainer {
  @Provides
  fun api(buildConfig: BuildConfig): ThemeApi {
    val client = buildKtorClient(AktualJson, tag = "THEME", isDebug = buildConfig.isDebug)
    return ThemeApiImpl(client)
  }
}
