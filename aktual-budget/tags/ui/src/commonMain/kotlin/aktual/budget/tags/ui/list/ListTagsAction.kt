package aktual.budget.tags.ui.list

import aktual.budget.model.TagId
import aktual.budget.model.TagSort
import aktual.budget.tags.vm.list.TagItem

internal sealed interface ListTagsAction

internal data object Retry : ListTagsAction

internal data object OpenSearch : ListTagsAction

internal data object CreateTag : ListTagsAction

@JvmInline internal value class EditTag(val id: TagId) : ListTagsAction

@JvmInline internal value class ViewTransactions(val id: TagId) : ListTagsAction

@JvmInline internal value class DeleteTag(val id: TagId) : ListTagsAction

internal data class SetHidden(val item: TagItem, val hidden: Boolean) : ListTagsAction

@JvmInline internal value class SetShowHidden(val show: Boolean) : ListTagsAction

@JvmInline internal value class SetSort(val sort: TagSort) : ListTagsAction
