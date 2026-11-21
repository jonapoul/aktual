package aktual.budget.payees.domain

import openstreetmap.api.model.Place

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
