package aktual.budget.tags.vm.list

import aktual.budget.model.TagId
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class TagItem(
  val id: TagId,
  val tag: String,
  val color: Color,
  val description: String,
  val hidden: Boolean,
)
