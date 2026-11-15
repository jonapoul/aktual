package aktual.budget.rules.domain

import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test

// Ported from packages/loot-core/src/server/rules/index.test.ts (describe 'split actions')
class SplitActionsTest {
  private val fixedAmountRule =
    splitRule(splitAmount(1, FixedAmount, 100), splitAmount(2, FixedAmount, 100))

  private val prioritizationRule =
    splitRule(
      splitAmount(1, FixedAmount, 100),
      splitAmount(2, FixedPercent, 50),
      splitAmount(3, Remainder),
    )

  @Test
  fun `Splits can change the payee`() {
    val rule =
      rule(
        "r",
        listOf(cond(Payee, Is, "123")),
        listOf(splitAmount(1, FixedAmount, 100), set(Payee, "456", splitIndex = 1)),
      )
    val result = rule.exec(tx(payee = "123", amount = 100))
    assertThat(result.subtransactions.map { it.payee }).isEqualTo(listOf(PayeeId("456")))
  }

  @Test
  fun `Splitting clears the parent's payee and copies the parent to each child`() {
    val rule =
      rule(
        "r",
        listOf(cond(Payee, Is, "123")),
        listOf(
          set(Category, "food"),
          splitAmount(1, FixedAmount, 60),
          set(Notes, "first", splitIndex = 1),
          splitAmount(2, Remainder),
          set(Category, "fun", splitIndex = 2),
        ),
      )
    val parent = tx(payee = "123", amount = 100, notes = "parent", cleared = false)
    val result = rule.exec(parent)

    assertThat(result.isParent).isTrue()
    assertThat(result.payee).isNull()
    assertThat(result.category).isEqualTo(CategoryId("food"))
    assertThat(result.notes).isEqualTo("parent")
    assertThat(result.splitDifference).isNull()
    assertThat(result.subtransactions)
      .isEqualTo(
        listOf(
          RuleTransaction(
            account = ACCOUNT,
            date = parent.date,
            amount = Amount(60),
            payee = PayeeId("123"),
            category = CategoryId("food"),
            notes = "first",
            cleared = false,
            isChild = true,
          ),
          RuleTransaction(
            account = ACCOUNT,
            date = parent.date,
            amount = Amount(40),
            payee = PayeeId("123"),
            category = CategoryId("fun"),
            cleared = false,
            isChild = true,
          ),
        ),
      )
  }

  @Test
  fun `Basic fixed-amount`() {
    assertThat(fixedAmountRule.amounts(200)).isEqualTo(listOf(100L, 100L))
  }

  @Test
  fun `Basic fixed-percent`() {
    val rule = splitRule(splitAmount(1, FixedPercent, 50), splitAmount(2, FixedPercent, 50))
    assertThat(rule.amounts(200)).isEqualTo(listOf(100L, 100L))
  }

  @Test
  fun `Basic remainder`() {
    val rule = splitRule(splitAmount(1, Remainder), splitAmount(2, Remainder))
    assertThat(rule.amounts(200)).isEqualTo(listOf(100L, 100L))
  }

  @Test
  fun `Percent is of the post-fixed-amount total`() {
    assertThat(prioritizationRule.amounts(200)).isEqualTo(listOf(100L, 50L, 50L))
  }

  @Test
  fun `Remainder and percent go negative if less than expected after fixed amounts`() {
    assertThat(prioritizationRule.amounts(50)).isEqualTo(listOf(100L, -25L, -25L))
  }

  @Test
  fun `Remainder zeroes out if nothing left`() {
    val rule =
      splitRule(
        splitAmount(1, FixedAmount, 100),
        splitAmount(2, FixedPercent, 100),
        splitAmount(3, Remainder),
      )
    assertThat(rule.amounts(150)).isEqualTo(listOf(100L, 50L, 0L))
  }

  @Test
  fun `Remainder rounds correctly and only if necessary`() {
    val rule = splitRule(splitAmount(1, Remainder), splitAmount(2, Remainder))
    assertThat(rule.amounts(-2397)).isEqualTo(listOf(-1198L, -1199L))
    assertThat(rule.amounts(123)).isEqualTo(listOf(62L, 61L))
    assertThat(rule.amounts(100)).isEqualTo(listOf(50L, 50L))
  }

  @Test
  fun `Fixed amounts exceeding the total leave a difference`() {
    val result = fixedAmountRule.exec(tx(importedPayee = "James", amount = 100))
    assertThat(result.splitDifference).isEqualTo(Amount(-100))
    assertThat(result.subtransactions.map { it.amount.toLong() }).isEqualTo(listOf(100L, 100L))
  }

  @Test
  fun `Fixed amounts undershooting the total leave a difference`() {
    val result = fixedAmountRule.exec(tx(importedPayee = "James", amount = 300))
    assertThat(result.splitDifference).isEqualTo(Amount(100))
    assertThat(result.subtransactions.map { it.amount.toLong() }).isEqualTo(listOf(100L, 100L))
  }

  @Test
  fun `Formula split amounts are skipped`() {
    val rule =
      splitRule(
        action(
          SetSplitAmount,
          value = 0,
          options = RuleAction.Options(splitIndex = 1, method = Formula, formula = "=300"),
        ),
        splitAmount(2, Remainder),
      )
    assertThat(rule.amounts(1000)).isEqualTo(listOf(0L, 1000L))
  }

  @Test
  fun `Later splits copy the payee of the split before`() {
    val rule =
      splitRule(
        splitAmount(1, FixedAmount, 10),
        set(Payee, "456", splitIndex = 1),
        splitAmount(2, Remainder),
      )
    val result = rule.exec(tx(importedPayee = "James", payee = "123", amount = 30))
    assertThat(result.subtransactions.map { it.payee })
      .isEqualTo(listOf(PayeeId("456"), PayeeId("456")))
  }

  private fun splitRule(vararg actions: RuleAction) =
    rule("r", listOf(cond(ImportedPayee, Is, "James")), actions.toList())

  private fun TransactionRule.amounts(amount: Long): List<Long> =
    exec(tx(importedPayee = "James", amount = amount)).subtransactions.map { it.amount.toLong() }
}
