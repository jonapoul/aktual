package aktual.budget.tags.ui.list

import aktual.budget.model.TagId
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ListTagsAction

internal data object Reload : ListTagsAction

internal data object OpenSearch : ListTagsAction

internal data object ClearFilter : ListTagsAction

internal data object CreateTag : ListTagsAction

@JvmInline internal value class EditFilterText(val text: String) : ListTagsAction

@JvmInline internal value class EditTag(val id: TagId) : ListTagsAction

@JvmInline internal value class DeleteTag(val id: TagId) : ListTagsAction

@Immutable
internal fun interface ListTagsActionHandler {
  operator fun invoke(action: ListTagsAction)
}
