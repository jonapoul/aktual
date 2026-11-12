package aktual.budget.payees.domain

import aktual.core.model.Coordinates
import androidx.compose.runtime.Immutable
import kotlin.math.roundToLong
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nominatim.api.model.Place

@Immutable data class Address(val title: String, val subtitle: String?)

@Immutable
sealed interface AddressResult {
  data object Loading : AddressResult

  @JvmInline value class Found(val address: Address) : AddressResult

  data object NotFound : AddressResult

  data object Failed : AddressResult
}

// Lookups keyed by coordinates rounded to about a metre, so near-identical saves share one
@Immutable
data class AddressBook(
  private val results: ImmutableMap<Coordinates, AddressResult>,
  private val isEnabled: Boolean,
) {
  // Null while lookups are turned off. Not looked up yet counts as loading, since showing it starts
  // the lookup.
  operator fun get(coordinates: Coordinates): AddressResult? =
    if (isEnabled) results[coordinates.rounded()] ?: Loading else null

  companion object {
    val Empty = AddressBook(persistentMapOf(), isEnabled = false)
  }
}

internal fun Coordinates.rounded(): Coordinates =
  Coordinates(latitude.fiveDecimals(), longitude.fiveDecimals())

private fun Double.fiveDecimals(): Double = (this * FIVE_DECIMALS).roundToLong() / FIVE_DECIMALS

private const val FIVE_DECIMALS = 100_000.0

internal fun Place.toAddress(): Address? {
  if (error != null) return null
  val street =
    listOfNotNull(address?.houseNumber, address?.road).joinToString(" ").takeIf { it.isNotBlank() }
  val locality = address?.run { suburb ?: village ?: town ?: city ?: hamlet }
  val name = name?.takeIf { it.isNotBlank() }
  val title =
    name
      ?: street
      ?: locality
      ?: displayName?.substringBefore(",")?.trim()?.takeIf { it.isNotBlank() }
      ?: return null

  val parts =
    when (title) {
      name -> listOfNotNull(street, locality)
      street -> listOfNotNull(locality, address?.postcode)
      else -> listOfNotNull(address?.postcode)
    }
  return Address(title, parts.filter { it != title }.joinToString(", ").takeIf { it.isNotBlank() })
}
