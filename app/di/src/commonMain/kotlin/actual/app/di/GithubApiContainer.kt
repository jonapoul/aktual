package actual.app.di

import actual.api.builder.buildKtorClient
import actual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import github.api.client.GithubApi
import github.api.client.GithubJson

@BindingContainer
@ContributesTo(AppScope::class)
object GithubApiContainer {
  @Provides
  fun apiFactory(
    buildConfig: BuildConfig,
  ): GithubApi.Factory = GithubApi.Factory {
    GithubApi(
      client = buildKtorClient(
        json = GithubJson,
        tag = "GITHUB",
        isDebug = buildConfig.isDebug,
      ),
    )
  }
}
