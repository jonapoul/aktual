package aktual.budget.tags.vm.list

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ListTagsEvent {
  data class Deleted(val item: TagItem, val index: Int) : ListTagsEvent

  @JvmInline value class DeleteFailed(val tag: String?) : ListTagsEvent

  @JvmInline value class RestoreFailed(val tag: String) : ListTagsEvent
}
