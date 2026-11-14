package nominatim.api.client

import aktual.core.model.Coordinates
import aktual.test.NominatimResponses
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpHeaders
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import nominatim.api.NominatimApi
import nominatim.api.model.Address
import nominatim.api.model.Place

class NominatimApiTest {
  private lateinit var api: NominatimApi
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    api = NominatimApiImpl(testHttpClient(mockEngine, NominatimJson))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Request has coordinates and language`() = runTest {
    mockEngine += { respondJson(NominatimResponses.REVERSE_SHOP_200) }

    api.reverse(Coordinates(latitude = 51.5081, longitude = -0.1248), language = "en-GB")

    val request = mockEngine.requestHistory.last()
    assertThat(request.url.toString())
      .isEqualTo(
        "https://nominatim.openstreetmap.org/reverse" +
          "?format=jsonv2&lat=51.5081&lon=-0.1248&zoom=18&addressdetails=1",
      )
    assertThat(request.headers[HttpHeaders.AcceptLanguage]).isEqualTo("en-GB")
  }

  @Test
  fun `Parse unnamed place`() = runTest {
    mockEngine += { respondJson(NominatimResponses.REVERSE_SHOP_200) }

    val place = api.reverse(Coordinates(latitude = 51.5081, longitude = -0.1248), "en")

    assertThat(place).all {
      prop(Place::name).isNotNull().isEmpty()
      prop(Place::error).isNull()
      prop(Place::address)
        .isEqualTo(
          Address(
            road = "Hungerford Lane",
            suburb = "Covent Garden",
            city = "City of Westminster",
            postcode = "WC2N 5HS",
            country = "United Kingdom",
          ),
        )
    }
  }

  @Test
  fun `Parse named road`() = runTest {
    mockEngine += { respondJson(NominatimResponses.REVERSE_HOUSE_200) }

    val place = api.reverse(Coordinates(latitude = 53.4794, longitude = -2.2453), "en")

    assertThat(place)
      .isEqualTo(
        Place(
          name = "Albert Square",
          displayName =
            "Albert Square, Civic Quarter, City Centre, Manchester, Greater Manchester, " +
              "England, M60 2WN, United Kingdom",
          address =
            Address(
              road = "Albert Square",
              suburb = "City Centre",
              city = "Manchester",
              postcode = "M60 2WN",
              country = "United Kingdom",
            ),
        ),
      )
  }

  @Test
  fun `Parse not found`() = runTest {
    mockEngine += { respondJson(NominatimResponses.REVERSE_NOT_FOUND_200) }

    val place = api.reverse(Coordinates(latitude = 0.0, longitude = 0.0), "en")

    assertThat(place).isEqualTo(Place(error = "Unable to geocode"))
  }
}
