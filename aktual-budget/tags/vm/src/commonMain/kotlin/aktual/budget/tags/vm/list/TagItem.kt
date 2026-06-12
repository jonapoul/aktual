package aktual.budget.tags.vm.list

import aktual.budget.model.TagId
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class TagItem(
  val id: TagId,
  val tag: String,
  // null when the tag has no explicit color — the UI falls back to the theme's note-tag colors
  val color: Color?,
  val description: String,
  val hidden: Boolean,
)
