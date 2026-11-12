package nominatim.api

import aktual.core.model.Coordinates
import nominatim.api.model.Place

// OpenStreetMap's reverse geocoder. See https://operations.osmfoundation.org/policies/nominatim/
interface NominatimApi {
  suspend fun reverse(coordinates: Coordinates, language: String): Place
}
