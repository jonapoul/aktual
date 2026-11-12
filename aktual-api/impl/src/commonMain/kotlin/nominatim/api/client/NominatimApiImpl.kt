package nominatim.api.client

import aktual.api.client.NominatimClient
import aktual.core.model.Coordinates
import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.path
import kotlin.time.Duration.Companion.seconds
import nominatim.api.NominatimApi
import nominatim.api.model.Place

@ContributesBinding(AppScope::class)
class NominatimApiImpl(@param:NominatimClient private val client: HttpClient) : NominatimApi {
  override suspend fun reverse(coordinates: Coordinates, language: String): Place =
    client
      .get {
        url {
          protocol = HTTPS
          host = NOMINATIM_URL
          path("/reverse")
        }
        parameter("format", "jsonv2")
        parameter("lat", coordinates.latitude)
        parameter("lon", coordinates.longitude)
        parameter("zoom", BUILDING_ZOOM)
        parameter("addressdetails", 1)
        header(HttpHeaders.AcceptLanguage, language)
        timeout { requestTimeoutMillis = TIMEOUT.inWholeMilliseconds }
      }
      .body<Place>()

  private companion object {
    const val NOMINATIM_URL = "nominatim.openstreetmap.org"
    const val BUILDING_ZOOM = 18
    val TIMEOUT = 10.seconds
  }
}
