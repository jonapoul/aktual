package aktual.budget.transactions.vm

import aktual.core.utils.AppDirectory
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

  private object NoLocationProvider : LocationProvider {
    override val isSupported = false

    override fun hasPermission() = false

    override suspend fun currentLocation() = null
  }
}
