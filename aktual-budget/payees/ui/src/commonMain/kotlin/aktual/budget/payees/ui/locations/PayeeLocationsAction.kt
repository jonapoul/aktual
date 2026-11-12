package aktual.budget.payees.ui.locations

import aktual.budget.model.PayeeLocationId
import aktual.core.model.Coordinates

internal sealed interface ListPayeeLocationsAction

internal data object NavBack : ListPayeeLocationsAction, SearchPayeeLocationsAction

internal data object Retry : ListPayeeLocationsAction, SearchPayeeLocationsAction

internal data object OpenSearch : ListPayeeLocationsAction

@JvmInline
internal value class OpenLocation(val coordinates: Coordinates) :
  ListPayeeLocationsAction, SearchPayeeLocationsAction

@JvmInline
internal value class DeleteLocation(val id: PayeeLocationId) :
  ListPayeeLocationsAction, SearchPayeeLocationsAction

internal sealed interface SearchPayeeLocationsAction

@JvmInline internal value class SetQuery(val query: String) : SearchPayeeLocationsAction
