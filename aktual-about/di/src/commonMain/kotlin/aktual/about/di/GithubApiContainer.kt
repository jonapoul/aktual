package aktual.about.di

import aktual.about.data.GithubClient
import aktual.about.data.GithubJson
import aktual.api.buildKtorClient
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@BindingContainer
@ContributesTo(AppScope::class)
object GithubApiContainer {
  @Provides
  @GithubClient
  @SingleIn(AppScope::class)
  fun client(buildConfig: BuildConfig): HttpClient =
    buildKtorClient(json = GithubJson, tag = "GITHUB", isDebug = buildConfig.isDebug)
}
