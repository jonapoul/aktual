package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import aktual.budget.model.TransactionSearch
import aktual.budget.rules.domain.FilterRow
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive

class TransactionSearchMatcherTest {
  @Test
  fun `Empty text matches everything`() {
    assertThat(matches("", row())).isTrue()
    assertThat(matches("  ", row())).isTrue()
  }

  @Test
  fun `Text is found in names and notes, ignoring case`() {
    assertThat(matches("tesco", row(payeeName = "Tesco Express"))).isTrue()
    assertThat(matches("SAVINGS", row(transferAccountName = "Savings"))).isTrue()
    assertThat(matches("hol", row(notes = "Holiday #travel"))).isTrue()
    assertThat(matches("groc", row(categoryName = "Groceries"))).isTrue()
    assertThat(matches("amex", row(accountName = "Amex"))).isTrue()
    assertThat(matches("tesco", row(payeeName = "Sainsbury's", notes = "food"))).isFalse()
  }

  @Test
  fun `Amounts match either side of zero`() {
    assertThat(matches("12.34", row(amount = -1234))).isTrue()
    assertThat(matches("12.34", row(amount = 1234))).isTrue()
    assertThat(matches("-12.34", row(amount = -1234))).isTrue()
    assertThat(matches("12.35", row(amount = -1234))).isFalse()
  }

  @Test
  fun `Whole amounts also match the whole part`() {
    assertThat(matches("12", row(amount = -1234))).isTrue()
    assertThat(matches("12", row(amount = 1200))).isTrue()
    assertThat(matches("1,200", row(amount = 120_050))).isTrue()
    assertThat(matches("12.30", row(amount = -1234))).isFalse()
    assertThat(matches("13", row(amount = -1234))).isFalse()
  }

  @Test
  fun `Conditions must match as well as the text`() {
    val food = Condition(Category, Is, JsonPrimitive("food"))
    val search = TransactionSearch(text = "tesco", conditions = ConditionSet(listOf(food)))
    val matcher = TransactionSearchMatcher(search)

    assertThat(matcher.matches(row(payeeName = "Tesco", category = "food"))).isTrue()
    assertThat(matcher.matches(row(payeeName = "Tesco", category = "fuel"))).isFalse()
    assertThat(matcher.matches(row(payeeName = "Aldi", category = "food"))).isFalse()
  }

  @Test
  fun `Conditions alone filter without text`() {
    val food = Condition(Category, Is, JsonPrimitive("food"))
    val matcher =
      TransactionSearchMatcher(TransactionSearch(conditions = ConditionSet(listOf(food))))

    assertThat(matcher.matches(row(category = "food"))).isTrue()
    assertThat(matcher.matches(row(category = "fuel"))).isFalse()
  }

  private fun matches(text: String, row: FilterRow) =
    TransactionSearchMatcher(TransactionSearch(text = text)).matches(row)

  private fun row(
    amount: Long = 0,
    payeeName: String? = null,
    transferAccountName: String? = null,
    accountName: String? = null,
    category: String? = null,
    categoryName: String? = null,
    notes: String? = null,
  ) =
    FilterRow(
      date = LocalDate(2025, 1, 1),
      amount = Amount(amount),
      account = AccountId("account"),
      accountName = accountName,
      payeeName = payeeName,
      transferAccountName = transferAccountName,
      category = category?.let(::CategoryId),
      categoryName = categoryName,
      notes = notes,
    )
}
