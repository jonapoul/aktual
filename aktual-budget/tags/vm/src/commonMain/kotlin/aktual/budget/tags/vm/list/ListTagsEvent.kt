package aktual.budget.tags.vm.list

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ListTagsEvent {
  // the tombstoned tag's name, so the UI can show a confirmation toast
  @JvmInline value class Deleted(val tag: String) : ListTagsEvent
}
