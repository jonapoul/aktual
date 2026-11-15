package aktual.budget.tags.domain

import aktual.budget.model.TagId

// Creates a tag with just a name, as upstream's tags-create does from a transaction's notes. A tag
// that already has the name is kept as it is, and a deleted one is brought back under its old id
interface TagCreator {
  suspend fun create(tag: String): TagId
}
