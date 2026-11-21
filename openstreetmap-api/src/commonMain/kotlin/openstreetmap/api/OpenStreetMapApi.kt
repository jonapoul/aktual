package openstreetmap.api

import aktual.core.model.Coordinates
import openstreetmap.api.model.Place

// OpenStreetMap's reverse geocoder. See https://operations.osmfoundation.org/policies/nominatim/
interface OpenStreetMapApi {
  suspend fun reverse(coordinates: Coordinates, language: String): Place
}
