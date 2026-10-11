package aktual.budget.transactions.vm.edit

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf

class NoteTagsTest {
  @Test
  fun `The tag typed is the word up to the cursor`() {
    assertThat(tagQuery("Lunch #fo", cursor = 9)).isEqualTo(TagQuery(start = 6, end = 9, "fo"))
    assertThat(tagQuery("Lunch #food later", cursor = 9))
      .isEqualTo(TagQuery(start = 6, end = 9, "fo"))
    assertThat(tagQuery("#", cursor = 1)).isEqualTo(TagQuery(start = 0, end = 1, ""))
  }

  @Test
  fun `There's no tag without a single leading hash`() {
    assertThat(tagQuery("Lunch", cursor = 5)).isNull()
    assertThat(tagQuery("##food", cursor = 6)).isNull()
    assertThat(tagQuery("a#food", cursor = 6)).isNull()
    assertThat(tagQuery("#food ", cursor = 6)).isNull()
    assertThat(tagQuery("#food", cursor = 0)).isNull()
  }

  @Test
  fun `A lone hash offers every tag`() {
    assertThat(TAGS.matching(TagQuery(0, 1, ""))).isEqualTo(TAGS)
  }

  @Test
  fun `Tags match ignoring case, shortest and then earliest first`() {
    assertThat(TAGS.matching(TagQuery(0, 3, "OO")).map { it.tag })
      .containsExactly("food", "foods", "Lagoon")
  }

  @Test
  fun `A tag can be created unless one has the exact name`() {
    assertThat(TAGS.canCreate(TagQuery(0, 5, "food"))).isFalse()
    assertThat(TAGS.canCreate(TagQuery(0, 5, "Food"))).isTrue()
    assertThat(TAGS.canCreate(TagQuery(0, 1, ""))).isFalse()
  }

  @Test
  fun `A hash starts a new word`() {
    assertThat(insertHash("", start = 0, end = 0)).isEqualTo(NotesEdit(0, 0, "#"))
    assertThat(insertHash("Lunch ", start = 6, end = 6)).isEqualTo(NotesEdit(6, 6, "#"))
    assertThat(insertHash("Lunch", start = 5, end = 5)).isEqualTo(NotesEdit(5, 5, " #"))
    assertThat(insertHash("Lunch out", start = 5, end = 9)).isEqualTo(NotesEdit(5, 9, " #"))
  }

  @Test
  fun `Completing a tag replaces what's typed of it`() {
    assertThat(TagQuery(start = 6, end = 9, "fo").complete("food"))
      .isEqualTo(NotesEdit(6, 9, "#food "))
  }

  private companion object {
    val TAGS =
      persistentListOf(
        TagOption("Lagoon", null),
        TagOption("food", null),
        TagOption("foods", null),
        TagOption("bills", null),
      )
  }
}
