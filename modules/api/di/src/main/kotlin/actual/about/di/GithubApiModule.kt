package actual.about.di

import actual.api.builder.buildKtorClient
import alakazam.kotlin.core.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import github.api.client.GithubApi
import github.api.client.GithubJson
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GithubApiModule {
  @Provides
  @Singleton
  fun api(
    buildConfig: BuildConfig,
  ) = GithubApi.Factory {
    GithubApi(
      client = buildKtorClient(
        json = GithubJson,
        tag = "GITHUB",
        isDebug = buildConfig.debug,
      ),
    )
  }
}
