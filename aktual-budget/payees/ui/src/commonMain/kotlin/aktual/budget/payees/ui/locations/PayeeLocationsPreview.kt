package aktual.budget.payees.ui.locations

import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.domain.Address
import aktual.budget.payees.domain.AddressResult.Found
import aktual.budget.payees.domain.AddressResult.Loading
import aktual.budget.payees.domain.AddressResult.NotFound
import aktual.budget.payees.vm.locations.PayeeLocationItem
import aktual.core.model.Coordinates
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

internal object PayeeLocationsPreview {
  val argos =
    PayeeLocationItem(
      id = PayeeLocationId("loc-1"),
      payee = PayeeId("payee-2"),
      payeeName = "Argos",
      coordinates = Coordinates(latitude = 51.51322, longitude = -0.08806),
      date = LocalDate(2026, 10, 21),
    )

  val tesco =
    PayeeLocationItem(
      id = PayeeLocationId("loc-2"),
      payee = PayeeId("payee-1"),
      payeeName = "Tesco",
      coordinates = Coordinates(latitude = 51.50735, longitude = -0.12776),
      date = LocalDate(2026, 11, 7),
    )

  val noDate =
    PayeeLocationItem(
      id = PayeeLocationId("loc-3"),
      payee = PayeeId("payee-1"),
      payeeName = "Tesco",
      coordinates = Coordinates(latitude = 51.49012, longitude = -0.20841),
      date = null,
    )

  val longName =
    PayeeLocationItem(
      id = PayeeLocationId("loc-4"),
      payee = PayeeId("payee-4"),
      payeeName = "A deliberately long payee name to check how the row truncates its text",
      coordinates = Coordinates(latitude = -33.86882, longitude = 151.20929),
      date = LocalDate(2026, 1, 2),
    )

  val all: ImmutableList<PayeeLocationItem> = persistentListOf(longName, argos, tesco, noDate)

  val withAddresses: ImmutableList<PayeeLocationItem> =
    persistentListOf(
      argos.copy(address = Found(Address("Argos", "Cheapside, City of London"))),
      tesco.copy(address = Found(Address("Hungerford Lane", "Covent Garden, WC2N 5HS"))),
      noDate.copy(address = Loading),
      longName.copy(address = NotFound),
    )
}
