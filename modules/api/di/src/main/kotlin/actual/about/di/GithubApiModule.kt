package actual.about.di

import actual.api.builder.buildOkHttp
import actual.api.builder.buildRetrofit
import actual.core.config.BuildConfig
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
    buildConfig: BuildConfig,
  ): GithubApi = buildRetrofit(
    client = buildOkHttp(buildConfig.debug, tag = "GITHUB"),
    url = ServerUrl("https://api.github.com"),
    json = GithubJson,
  ).create()
}
