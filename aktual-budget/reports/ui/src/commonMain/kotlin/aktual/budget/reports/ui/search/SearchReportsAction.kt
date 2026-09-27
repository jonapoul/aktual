package aktual.budget.reports.ui.search

import aktual.budget.model.WidgetId
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface SearchReportsAction

internal data object NavBack : SearchReportsAction

@JvmInline internal value class SetQuery(val query: String) : SearchReportsAction

@JvmInline internal value class OpenReport(val id: WidgetId) : SearchReportsAction

@Immutable
internal fun interface SearchReportsActionHandler {
  operator fun invoke(action: SearchReportsAction)
}
