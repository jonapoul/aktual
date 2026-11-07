package aktual.core.utils

import aktual.core.model.Coordinates

interface LocationProvider {
  // False where there's no way to get a location, like desktop
  val isSupported: Boolean

  fun hasPermission(): Boolean

  // Null when it can't be found, like with location turned off or no fix in time
  suspend fun currentLocation(): Coordinates?
}
