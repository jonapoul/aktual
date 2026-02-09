package aktual.about.di

import aktual.about.data.GithubApi
import aktual.about.data.GithubApiImpl
import aktual.about.data.GithubJson
import aktual.core.api.buildKtorClient
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object GithubApiContainer {
  @Provides
  fun githubApi(
      buildConfig: BuildConfig,
  ): GithubApi =
      GithubApiImpl(
          buildKtorClient(
              json = GithubJson,
              tag = "GITHUB",
              isDebug = buildConfig.isDebug,
          ),
      )
}
