package aktual.budget.tags.vm.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface ListTagsState

data object Loading : ListTagsState

@JvmInline value class Failure(val cause: String?) : ListTagsState

@Immutable @JvmInline value class Success(val tags: ImmutableList<TagItem>) : ListTagsState
