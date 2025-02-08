package actual.about.di

import actual.api.core.buildOkHttp
import actual.api.core.buildRetrofit
import actual.core.config.BuildConfig
import actual.log.Logger
import actual.url.model.ServerUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import github.api.client.GithubApi
import github.api.client.GithubJson
import retrofit2.create

@Module
@InstallIn(ViewModelComponent::class)
class GithubApiModule {
  @Provides
  fun api(
    logger: Logger,
    buildConfig: BuildConfig,
  ): GithubApi = buildRetrofit(
    client = buildOkHttp(logger, buildConfig.debug, tag = "GITHUB"),
    url = ServerUrl("https://api.github.com"),
    json = GithubJson,
  ).create()
}
