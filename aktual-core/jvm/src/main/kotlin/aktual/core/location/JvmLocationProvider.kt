package aktual.core.location

import aktual.core.model.Coordinates
import aktual.core.utils.LocationProvider
import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class JvmLocationProvider : LocationProvider {
  override val isSupported: Boolean = false

  override fun hasPermission(): Boolean = false

  override suspend fun currentLocation(): Coordinates? = null
}
