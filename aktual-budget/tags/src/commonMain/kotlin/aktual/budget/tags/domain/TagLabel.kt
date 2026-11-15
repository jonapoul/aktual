package aktual.budget.tags.domain

import aktual.budget.db.GetTags
import aktual.budget.model.TagId
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// A null color shows in the theme's tag colors
@Immutable data class TagLabel(val id: TagId, val name: String, val color: Color?)

// The visible tags, by name
fun List<GetTags>.toTagLabels(): ImmutableList<TagLabel> =
  asSequence()
    .filter { it.hidden != true }
    .mapNotNull { row -> row.tag?.let { TagLabel(row.id, it, row.color?.toColorOrNull()) } }
    .sortedBy { it.name.lowercase() }
    .toImmutableList()
