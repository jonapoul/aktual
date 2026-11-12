package aktual.budget.payees.domain

import aktual.core.model.Coordinates
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.collections.immutable.persistentMapOf
import nominatim.api.model.Address
import nominatim.api.model.Place

class AddressTest {
  @Test
  fun `Named place shows street and locality`() {
    val place =
      Place(
        name = "Tesco Express",
        address =
          Address(
            houseNumber = "22",
            road = "Strand",
            suburb = "St. James's",
            city = "London",
            postcode = "WC2N 5HR",
          ),
      )
    assertThat(place.toAddress()).isEqualTo(Address("Tesco Express", "22 Strand, St. James's"))
  }

  @Test
  fun `House shows street then locality and postcode`() {
    val place =
      Place(
        name = "",
        address =
          Address(
            houseNumber = "10",
            road = "Oldham Street",
            suburb = "Northern Quarter",
            city = "Manchester",
            postcode = "M1 1JQ",
          ),
      )
    assertThat(place.toAddress()).isEqualTo(Address("10 Oldham Street", "Northern Quarter, M1 1JQ"))
  }

  @Test
  fun `Road without a number`() {
    val place = Place(address = Address(road = "A1", village = "Stilton"))
    assertThat(place.toAddress()).isEqualTo(Address("A1", "Stilton"))
  }

  @Test
  fun `Locality only`() {
    val place = Place(address = Address(village = "Stilton"))
    assertThat(place.toAddress()).isEqualTo(Address("Stilton", null))
  }

  @Test
  fun `Falls back to the first part of the display name`() {
    val place = Place(displayName = "North Sea, Europe")
    assertThat(place.toAddress()).isEqualTo(Address("North Sea", null))
  }

  @Test
  fun `Error is not an address`() {
    assertThat(Place(error = "Unable to geocode").toAddress()).isNull()
  }

  @Test
  fun `Address book is empty while turned off`() {
    assertThat(AddressBook.Empty[Coordinates(51.50735, -0.12776)]).isNull()
  }

  @Test
  fun `Address book matches nearby coordinates`() {
    val book =
      AddressBook(
        persistentMapOf(
          Coordinates(51.50735, -0.12776).rounded() to AddressResult.NotFound,
        ),
        isEnabled = true,
      )
    assertThat(book[Coordinates(51.507351, -0.127762)]).isEqualTo(NotFound)
    assertThat(book[Coordinates(51.50745, -0.12776)]).isEqualTo(Loading)
  }
}
