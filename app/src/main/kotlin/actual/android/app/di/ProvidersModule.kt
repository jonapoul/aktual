package actual.android.app.di

import actual.account.model.Password
import actual.android.app.BuildConfig
import actual.core.model.ServerUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("USELESS_ELVIS", "UNNECESSARY_SAFE_CALL")
@Module
@InstallIn(SingletonComponent::class)
internal class ProvidersModule {
  @Provides
  @Singleton
  fun password() = Password.Provider {
    BuildConfig.DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty
  }

  @Provides
  @Singleton
  fun url() = ServerUrl.Provider {
    BuildConfig.DEFAULT_URL?.let(::ServerUrl)
  }
}
