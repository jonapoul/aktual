package aktual.app.di

import aktual.api.buildKtorClient
import aktual.api.client.GithubClient
import aktual.core.model.BuildConfig
import aktual.di.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import github.api.client.GithubJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

@BindingContainer
@ContributesTo(AppScope::class)
object GithubApiContainer {
  @Provides
  @GithubClient
  @SingleIn(AppScope::class)
  fun client(buildConfig: BuildConfig, engine: HttpClientEngine): HttpClient =
    buildKtorClient(GithubJson, tag = "GITHUB", engine, buildConfig.isDebug)
}
