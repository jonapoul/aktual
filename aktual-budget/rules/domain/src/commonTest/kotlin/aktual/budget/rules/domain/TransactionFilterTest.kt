package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.ConditionOptions
import aktual.budget.model.Field
import aktual.budget.model.PayeeId
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TransactionFilterTest {
  @Test
  fun `No conditions match everything`() {
    val filter = filter()
    assertThat(filter.isEmpty).isTrue()
    assertThat(filter.matches(row())).isTrue()
  }

  @Test
  fun `Saved filter references and invalid conditions are skipped`() {
    val saved = cond(Notes, Is, "x").copy(customName = "Saved")
    val invalid = cond(Date, GreaterThan, "2020-08")
    assertThat(filter(saved, invalid).isEmpty).isTrue()
  }

  @Test
  fun `All conditions match with and, any with or`() {
    val food = cond(Notes, Contains, "food")
    val big = cond(Field.Amount, LessThan, -1000)
    val row = row(notes = "food", amount = -500)

    assertThat(filter(food, big).matches(row)).isFalse()
    assertThat(filter(food, big, op = Or).matches(row)).isTrue()
  }

  @Test
  fun `Negative operators match missing values`() {
    val row = row(notes = null)
    assertThat(filter(cond(Notes, IsNot, "x")).matches(row)).isTrue()
    assertThat(filter(cond(Notes, DoesNotContain, "x")).matches(row)).isTrue()
    assertThat(filter(cond(ImportedPayee, NotOneOf, listOf("x"))).matches(row)).isTrue()
    assertThat(filter(cond(Payee, IsNot, "shop")).matches(row)).isTrue()
    assertThat(filter(cond(Payee, IsNot, null)).matches(row)).isFalse()
  }

  @Test
  fun `Empty text matches missing or empty values`() {
    val filter = filter(cond(Notes, Is, ""))
    assertThat(filter.matches(row(notes = null))).isTrue()
    assertThat(filter.matches(row(notes = ""))).isTrue()
    assertThat(filter.matches(row(notes = "x"))).isFalse()
  }

  @Test
  fun `Text is matched ignoring case and accents`() {
    assertThat(filter(cond(Notes, Contains, "CAFE")).matches(row(notes = "Café"))).isTrue()
    assertThat(filter(cond(Notes, Is, "CAFÉ")).matches(row(notes = "café"))).isTrue()
    assertThat(filter(cond(Notes, Contains, "strasse")).matches(row(notes = "Straße"))).isTrue()
  }

  @Test
  fun `Text operators on ids match the name`() {
    val row = row(payee = "p1", payeeName = "Corner Shop", accountName = "Checking")
    assertThat(filter(cond(Payee, Contains, "shop")).matches(row)).isTrue()
    assertThat(filter(cond(Payee, Matches, "op$")).matches(row)).isTrue()
    // Unlike text fields, names aren't lowercased before a regex is matched
    assertThat(filter(cond(Payee, Matches, "^corner")).matches(row)).isFalse()
    assertThat(filter(cond(Account, DoesNotContain, "check")).matches(row)).isFalse()
    assertThat(filter(cond(Payee, Is, "p1")).matches(row)).isTrue()
  }

  @Test
  fun `Empty lists match nothing`() {
    assertThat(filter(cond(Payee, OneOf, emptyList<String>())).matches(row())).isFalse()
    assertThat(filter(cond(Payee, NotOneOf, emptyList<String>())).matches(row())).isFalse()
  }

  @Test
  fun `Uncategorised leaves out transfers and split parents`() {
    val filter = filter(cond(Category, Is, null))
    assertThat(filter.matches(row())).isTrue()
    assertThat(filter.matches(row(isTransfer = true))).isFalse()
    assertThat(filter.matches(row(isParent = true))).isFalse()
  }

  @Test
  fun `Categorised leaves out split parents`() {
    val filter = filter(cond(Category, IsNot, null))
    assertThat(filter.matches(row(category = "food"))).isTrue()
    assertThat(filter.matches(row(category = "food", isParent = true))).isFalse()
    assertThat(filter.matches(row())).isFalse()
  }

  @Test
  fun `Transfer and parent flags`() {
    assertThat(filter(cond(Transfer, Is, true)).matches(row(isTransfer = true))).isTrue()
    assertThat(filter(cond(Transfer, Is, false)).matches(row(isTransfer = true))).isFalse()
    assertThat(filter(cond(Parent, Is, false)).matches(row())).isTrue()
  }

  @Test
  fun `On and off budget accounts`() {
    assertThat(filter(cond(Account, OffBudget, null)).matches(row(offBudget = true))).isTrue()
    assertThat(filter(cond(Account, OnBudget, null)).matches(row(offBudget = true))).isFalse()
  }

  @Test
  fun `Outflows are compared as positive amounts`() {
    val outflow = filter(cond(Field.Amount, GreaterThan, 500, ConditionOptions(outflow = true)))
    assertThat(outflow.matches(row(amount = -1000))).isTrue()
    assertThat(outflow.matches(row(amount = 1000))).isFalse()

    val inflow = filter(cond(Field.Amount, GreaterThan, 500, ConditionOptions(inflow = true)))
    assertThat(inflow.matches(row(amount = 1000))).isTrue()
    assertThat(inflow.matches(row(amount = -1000))).isFalse()
  }

  @Test
  fun `Tags match whole tags only`() {
    val filter = filter(cond(Notes, HasTags, "#food"))
    assertThat(filter.matches(row(notes = "lunch #food"))).isTrue()
    assertThat(filter.matches(row(notes = "lunch #foodie"))).isFalse()
    assertThat(filter.matches(row(notes = "lunch ##food"))).isFalse()
    assertThat(filter(cond(Notes, HasTags, "#")).matches(row(notes = "#food"))).isFalse()
  }

  @Test
  fun `Dates compare by day`() {
    val row = row(date = "2020-08-10")
    assertThat(filter(cond(Date, GreaterThan, "2020-08-09")).matches(row)).isTrue()
    assertThat(filter(cond(Date, LessThanOrEquals, "2020-08-09")).matches(row)).isFalse()
    assertThat(filter(cond(Date, Is, "2020-08")).matches(row)).isTrue()
  }

  @Test
  fun `Invalid regex matches nothing`() {
    assertThat(filter(cond(Notes, Matches, "(")).matches(row(notes = "("))).isFalse()
  }

  private fun filter(vararg conditions: Condition, op: ConditionOp = And) =
    TransactionFilter.compile(conditions.toList(), op)

  private fun row(
    date: String = "2020-01-01",
    amount: Long = 0,
    payee: String? = null,
    payeeName: String? = null,
    accountName: String? = null,
    offBudget: Boolean = false,
    category: String? = null,
    notes: String? = null,
    isTransfer: Boolean = false,
    isParent: Boolean = false,
  ) =
    FilterRow(
      date = LocalDate.parse(date),
      amount = Amount(amount),
      account = AccountId("account"),
      accountName = accountName,
      offBudget = offBudget,
      payee = payee?.let(::PayeeId),
      payeeName = payeeName,
      category = category?.let(::CategoryId),
      categoryGroup = category?.let { CategoryGroupId("group") },
      notes = notes,
      isTransfer = isTransfer,
      isParent = isParent,
    )
}
