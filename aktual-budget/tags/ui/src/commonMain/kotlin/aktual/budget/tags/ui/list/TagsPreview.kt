package aktual.budget.tags.ui.list

import aktual.budget.model.TagId
import aktual.budget.tags.vm.list.TagItem
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal object TagsPreview {
  val groceries =
    TagItem(
      id = TagId("tag-1"),
      tag = "groceries",
      color = Color(0xFF4CAF50),
      description = "Weekly food shopping",
      hidden = false,
    )

  val rent =
    TagItem(
      id = TagId("tag-2"),
      tag = "rent",
      color = Color(0xFFE91E63),
      description = "",
      hidden = false,
    )

  val archived =
    TagItem(
      id = TagId("tag-3"),
      tag = "archived",
      color = Color(0xFFFFEB3B),
      description =
        "A deliberately long description to check how the item wraps and truncates when the text " +
          "runs well beyond the available width of the row",
      hidden = true,
    )

  // no explicit color — exercises the theme note-tag fallback
  val uncolored =
    TagItem(
      id = TagId("tag-4"),
      tag = "misc",
      color = null,
      description = "",
      hidden = false,
    )

  val all: ImmutableList<TagItem> = persistentListOf(groceries, rent, archived, uncolored)
}
