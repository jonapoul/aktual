package aktual.budget.reports.vm.report

import aktual.budget.model.TagId
import aktual.budget.reports.vm.CategoryGroupChoice
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.CustomReportConfig
import aktual.budget.reports.vm.QueryEditorData
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
sealed interface CustomReportState {
  data object Loading : CustomReportState

  data object NotFound : CustomReportState

  // The draft is null when the saved report can't be read, so it can't be edited either. It keeps
  // its changes after the editor closes, until they're reverted. The preview is null until the
  // draft's first chart loads
  data class Loaded(
    val name: String,
    val draft: CustomReportConfig?,
    val preview: ChartData?,
    val isEditing: Boolean,
    val hasChanges: Boolean,
  ) : CustomReportState
}

// What the editor picks categories, tags and filters from
@Immutable
data class CustomReportEditorData(
  val query: QueryEditorData,
  val categories: ImmutableList<CategoryGroupChoice>,
  // The visible tags a split by tag can be scoped to, by name
  val tags: ImmutableList<TagChoice> = persistentListOf(),
)

data class TagChoice(val id: TagId, val name: String)
