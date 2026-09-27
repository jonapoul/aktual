package aktual.budget.tags.vm.search

import aktual.budget.tags.vm.list.TagItem
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchTagsState {
  data object NoQuery : SearchTagsState

  data object NoResults : SearchTagsState

  data class Results(val query: String, val tags: ImmutableList<TagItem>) : SearchTagsState
}
