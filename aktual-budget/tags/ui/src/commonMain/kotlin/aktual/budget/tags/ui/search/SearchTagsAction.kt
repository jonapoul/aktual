package aktual.budget.tags.ui.search

import aktual.budget.model.TagId

internal sealed interface SearchTagsAction

internal data object NavBack : SearchTagsAction

@JvmInline internal value class SetQuery(val query: String) : SearchTagsAction

@JvmInline internal value class OpenTag(val id: TagId) : SearchTagsAction
