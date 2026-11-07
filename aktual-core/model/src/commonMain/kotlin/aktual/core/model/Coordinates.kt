package aktual.core.model

import androidx.compose.runtime.Immutable
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Immutable
data class Coordinates(val latitude: Double, val longitude: Double) {
  init {
    require(latitude in -MAX_LATITUDE..MAX_LATITUDE) { "Invalid latitude $latitude" }
    require(longitude in -MAX_LONGITUDE..MAX_LONGITUDE) { "Invalid longitude $longitude" }
  }

  companion object {
    const val MAX_LATITUDE = 90.0
    const val MAX_LONGITUDE = 180.0
    const val EARTH_RADIUS_METERS = 6_371_000.0

    fun orNull(latitude: Double?, longitude: Double?): Coordinates? {
      if (latitude == null || longitude == null) return null
      if (latitude !in -MAX_LATITUDE..MAX_LATITUDE) return null
      if (longitude !in -MAX_LONGITUDE..MAX_LONGITUDE) return null
      return Coordinates(latitude, longitude)
    }
  }
}

// Haversine, as upstream's calculateDistance
fun Coordinates.distanceTo(other: Coordinates): Distance {
  val deltaLatitude = Math.toRadians(other.latitude - latitude)
  val deltaLongitude = Math.toRadians(other.longitude - longitude)
  val a =
    sin(deltaLatitude / 2) * sin(deltaLatitude / 2) +
      cos(Math.toRadians(latitude)) *
        cos(Math.toRadians(other.latitude)) *
        sin(deltaLongitude / 2) *
        sin(deltaLongitude / 2)
  return Distance(meters = 2 * Coordinates.EARTH_RADIUS_METERS * asin(sqrt(a.coerceAtMost(1.0))))
}
