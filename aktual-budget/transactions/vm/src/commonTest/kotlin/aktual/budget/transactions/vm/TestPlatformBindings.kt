package aktual.budget.transactions.vm

import aktual.core.model.ServerUrl
import aktual.core.utils.AppDirectory
import aktual.core.utils.LocalNetworkPermission
import aktual.core.utils.LocationProvider
import aktual.di.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

// Stands in for aktual-core:android/jvm, whose other app bindings need a real Android Context.
// Tests override the directory with TestAppDirectoryContainer.
@BindingContainer
@ContributesTo(AppScope::class)
internal object TestPlatformBindings {
  @Provides fun appDirectory(): AppDirectory = AppDirectory { error("Not provided") }

  @Provides fun location(): LocationProvider = NoLocationProvider

  @Provides fun localNetwork(): LocalNetworkPermission = GrantedLocalNetworkPermission

  private object NoLocationProvider : LocationProvider {
    override val isSupported = false

    override fun hasPermission() = false

    override suspend fun currentLocation() = null
  }

  private object GrantedLocalNetworkPermission : LocalNetworkPermission {
    override fun isGranted() = true

    override suspend fun isRequiredFor(url: ServerUrl) = false

    override suspend fun isRequiredForHost(host: String) = false
  }
}
