package aktual.budget.tags.ui.search

import aktual.budget.model.TagId
import androidx.compose.runtime.Immutable

internal sealed interface SearchTagsAction

internal data object NavBack : SearchTagsAction

@JvmInline internal value class SetQuery(val query: String) : SearchTagsAction

@JvmInline internal value class OpenTag(val id: TagId) : SearchTagsAction

@Immutable
internal fun interface SearchTagsActionHandler {
  operator fun invoke(action: SearchTagsAction)
}
