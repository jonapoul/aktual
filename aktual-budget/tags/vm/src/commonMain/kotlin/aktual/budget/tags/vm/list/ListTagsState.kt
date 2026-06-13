package aktual.budget.tags.vm.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface ListTagsState

data object Loading : ListTagsState

@JvmInline value class Failure(val cause: String?) : ListTagsState

data object Empty : ListTagsState

@Immutable
data class Success(
  val tags: ImmutableList<TagItem>,
  val filterText: String,
  val isSearchActive: Boolean,
) : ListTagsState
