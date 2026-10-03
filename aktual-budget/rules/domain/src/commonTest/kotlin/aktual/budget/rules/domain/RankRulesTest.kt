package aktual.budget.rules.domain

import aktual.budget.model.Field
import aktual.budget.model.RuleId
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

// Ported from 'rules are deterministically ranked' in
// packages/loot-core/src/server/rules/index.test.ts
class RankRulesTest {
  @Test
  fun `Rules are deterministically ranked`() {
    var rules =
      listOf(
        rule("id1", listOf(cond(Notes, Contains, "sar"))),
        rule("id2", listOf(cond(Notes, Contains, "jim"))),
        rule("id3", listOf(cond(Notes, Is, "James"))),
      )
    assertThat(rankRules(rules).ids()).containsExactly("id1", "id2", "id3")
    assertThat(rankRules(rules.reversed()).ids()).containsExactly("id1", "id2", "id3")

    rules =
      listOf(
        rule("id1", listOf(cond(Notes, Contains, "sar"))),
        rule("id2", listOf(cond(ImportedPayee, OneOf, listOf("jim", "sar")))),
        rule("id3", listOf(cond(Notes, Is, "James"))),
        rule("id4", listOf(cond(Notes, Is, "James"), cond(Field.Amount, GreaterThan, 5))),
        rule(
          "id5",
          listOf(
            cond(Notes, Is, "James"),
            cond(Field.Amount, GreaterThan, 5),
            cond(Field.Amount, LessThan, 10),
          ),
        ),
      )
    assertThat(rankRules(rules).ids()).containsExactly("id1", "id4", "id5", "id2", "id3")
  }

  @Test
  fun `Stages run pre, then unstaged, then post`() {
    val rules =
      listOf(
        rule("post", listOf(cond(Notes, Contains, "a")), stage = Post),
        rule("default", listOf(cond(Notes, Is, "a")), stage = Default),
        rule("null", listOf(cond(Notes, Contains, "a")), stage = null),
        rule("unknown", listOf(cond(Notes, Contains, "b")), stage = Unknown),
        rule("pre", listOf(cond(Notes, Is, "a"), cond(Notes, Is, "b")), stage = Pre),
      )
    assertThat(rankRules(rules).ids()).containsExactly("pre", "null", "unknown", "default", "post")
    assertThat(RulesEngine(rules).rankedRules.map(RuleId::value))
      .containsExactly("pre", "null", "unknown", "default", "post")
  }

  private fun List<TransactionRule>.ids() = map { it.id.value }
}
