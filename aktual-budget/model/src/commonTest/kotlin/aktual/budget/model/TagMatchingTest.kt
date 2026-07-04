package aktual.budget.model

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

// Mostly pulled from packages/loot-core/src/server/transactions/transaction-rules.test.ts
class TagMatchingTest {
  @Test
  fun `No tags`() {
    assertThat(tagsInNotes("")).isEmpty()
    assertThat(tagsInNotes("just some plain notes")).isEmpty()
    assertThat(tagsInNotes("a # b")).isEmpty()
  }

  @Test
  fun `Single tag`() {
    assertThat(tagsInNotes("#food")).containsExactlyInAnyOrder("food")
    assertThat(tagsInNotes("lunch #food today")).containsExactlyInAnyOrder("food")
  }

  @Test
  fun `Multiple tags`() {
    assertThat(tagsInNotes("#food #travel groceries #home"))
      .containsExactlyInAnyOrder("food", "travel", "home")
  }

  @Test
  fun `Tags are lowercased and de-duplicated`() {
    assertThat(tagsInNotes("#Food #FOOD #food")).containsExactlyInAnyOrder("food")
  }

  @Test
  fun `Tag terminated by another hash`() {
    assertThat(tagsInNotes("#food#travel")).containsExactlyInAnyOrder("food", "travel")
  }

  @Test
  fun `Double hash is skipped`() {
    // '##' does not start a tag (lookbehind blocks it), matching upstream Actual
    assertThat(tagsInNotes("##food")).isEmpty()
    assertThat(tagsInNotes("a ##comment #real")).containsExactlyInAnyOrder("real")
  }

  @Test
  fun `No left whitespace boundary required`() {
    // Mirrors upstream: "word#tag" still matches the tag
    assertThat(tagsInNotes("ref#food purchase")).containsExactlyInAnyOrder("food")
  }

  @Test
  fun `Tag bounded by punctuation is not split`() {
    // Whitespace and '#' are the only terminators, so trailing punctuation is part of the token
    assertThat(tagsInNotes("#food, please")).containsExactlyInAnyOrder("food,")
  }

  @Test
  fun `Tag content is a whole token, not a substring`() {
    // "#tagContent" must not satisfy the tag "tag" (mirrors upstream hasTags)
    assertThat(tagsInNotes("Follow up #tagContent")).containsExactlyInAnyOrder("tagcontent")
    assertThat(notesContainTag("Follow up #tagContent", "tag")).isFalse()
  }

  @Test
  fun `Tag containing regex-special characters`() {
    // Upstream has a "#$Bug_Test" case; '$' must be treated literally
    assertThat(tagsInNotes("Follow up #\$Bug_Test issue")).containsExactlyInAnyOrder("\$bug_test")
    assertThat(notesContainTag("Follow up #\$Bug_Test issue", "\$bug_test")).isTrue()
  }

  @Test
  fun `notesContainTag is case-insensitive`() {
    assertThat(notesContainTag("lunch #Food", "food")).isTrue()
    assertThat(notesContainTag("lunch #food", "FOOD")).isTrue()
  }

  @Test
  fun `notesContainTag false when absent`() {
    assertThat(notesContainTag("lunch #food", "travel")).isFalse()
    assertThat(notesContainTag("", "food")).isFalse()
  }

  @Test
  fun `notesContainTag matches word-adjacent tag`() {
    assertThat(notesContainTag("ref#food", "food")).isTrue()
    assertThat(notesContainTag("##food", "food")).isFalse()
  }
}
