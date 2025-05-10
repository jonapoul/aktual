package actual.about.di

import actual.api.builder.ClientFactory
import actual.api.builder.buildKtorClient
import alakazam.kotlin.core.BuildConfig
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Inject

@InstallIn(SingletonComponent::class)
@Module
internal interface HttpModule {
  @Binds
  fun clientFactory(impl: ClientFactoryImpl): ClientFactory
}

internal class ClientFactoryImpl @Inject constructor(private val buildConfig: BuildConfig) : ClientFactory {
  override fun build(json: Json) = buildKtorClient(json, isDebug = buildConfig.debug, tag = "ACTUAL")
}
