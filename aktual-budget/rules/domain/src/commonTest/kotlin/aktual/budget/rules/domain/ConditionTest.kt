package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.ConditionOptions
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.messageContains
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

// Ported from packages/loot-core/src/server/rules/index.test.ts (describe 'Condition')
class ConditionTest {
  @Test
  fun `Parses date formats`() {
    assertThat(CompiledCondition.parseDateString("2020-08-10"))
      .isEqualTo(DateValue.Day(LocalDate(2020, 8, 10)))
    assertThat(CompiledCondition.parseDateString("2020-08"))
      .isEqualTo(DateValue.Month(YearMonth(2020, 8)))
    assertThat(CompiledCondition.parseDateString("2020")).isEqualTo(DateValue.Year(2020))

    // Invalid dates
    assertThat(CompiledCondition.parseDateString("2020-0")).isNull()
    assertThat(CompiledCondition.parseDateString("2020-14-01")).isNull()
    assertThat(CompiledCondition.parseDateString("2020-05-53")).isNull()
  }

  @Test
  fun `Ops handle null fields`() {
    assertThat(cond(Notes, Contains, "foo").eval(tx(notes = null))).isFalse()
    assertThat(cond(Notes, Matches, "^fo*$").eval(tx(notes = null))).isFalse()
    assertThat(cond(ImportedPayee, OneOf, listOf("foo")).eval(tx(importedPayee = null))).isFalse()
    assertThat(cond(Payee, Is, null).eval(tx(payee = null))).isTrue()
    assertThat(cond(Notes, Is, "").eval(tx(notes = null))).isTrue()
  }

  @Test
  fun `Date restricts operators for each type`() {
    listOf<Operator>(IsApprox, GreaterThan, GreaterThanOrEquals, LessThan, LessThanOrEquals)
      .forEach { op ->
        assertFailure { compile(cond(Date, op, "2020-08")) }
          .messageContains("Invalid date value for")
      }
  }

  @Test
  fun `Date conditions work with is`() {
    var c = cond(Date, Is, "2020-08-10")
    assertThat(c.eval(tx(date = "2020-08-05"))).isFalse()
    assertThat(c.eval(tx(date = "2020-08-10"))).isTrue()

    c = cond(Date, Is, "2020-08")
    assertThat(c.eval(tx(date = "2020-08-05"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-10"))).isTrue()
    assertThat(c.eval(tx(date = "2020-09-10"))).isFalse()

    c = cond(Date, Is, "2020")
    assertThat(c.eval(tx(date = "2020-08-05"))).isTrue()
    assertThat(c.eval(tx(date = "2020-09-10"))).isTrue()
    assertThat(c.eval(tx(date = "2019-09-10"))).isFalse()

    // Approximate dates
    c = cond(Date, IsApprox, "2020-08-07")
    assertThat(c.eval(tx(date = "2020-08-04"))).isFalse()
    assertThat(c.eval(tx(date = "2020-08-05"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-09"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-10"))).isFalse()
  }

  @Test
  fun `Recurring date conditions work with is`() {
    var c =
      cond(
        Date,
        Is,
        mapOf(
          "start" to "2019-01-01",
          "frequency" to "monthly",
          "patterns" to listOf(mapOf("type" to "day", "value" to 15)),
        ),
      )
    assertThat(c.eval(tx(date = "2018-03-15"))).isFalse()
    assertThat(c.eval(tx(date = "2019-03-15"))).isTrue()
    assertThat(c.eval(tx(date = "2020-05-15"))).isTrue()
    assertThat(c.eval(tx(date = "2020-06-15"))).isTrue()
    assertThat(c.eval(tx(date = "2020-06-10"))).isFalse()

    c = cond(Date, Is, mapOf("start" to "2018-01-12", "frequency" to "monthly", "interval" to 3))
    assertThat(c.eval(tx(date = "2019-01-12"))).isTrue()
    assertThat(c.eval(tx(date = "2019-04-12"))).isTrue()
    assertThat(c.eval(tx(date = "2020-07-12"))).isTrue()
    assertThat(c.eval(tx(date = "2020-06-12"))).isFalse()

    // Approximate dates
    c =
      cond(
        Date,
        IsApprox,
        mapOf(
          "start" to "2019-01-01",
          "frequency" to "monthly",
          "patterns" to listOf(mapOf("type" to "day", "value" to 15)),
        ),
      )
    assertThat(c.eval(tx(date = "2019-03-12"))).isFalse()
    assertThat(c.eval(tx(date = "2019-03-13"))).isTrue()
    assertThat(c.eval(tx(date = "2019-03-15"))).isTrue()
    assertThat(c.eval(tx(date = "2019-03-17"))).isTrue()
    assertThat(c.eval(tx(date = "2019-03-18"))).isFalse()
    assertThat(c.eval(tx(date = "2019-04-15"))).isTrue()
    assertThat(c.eval(tx(date = "2019-05-15"))).isTrue()
    assertThat(c.eval(tx(date = "2019-05-17"))).isTrue()
  }

  @Test
  fun `Recurring dates respect their end`() {
    val afterTwo =
      cond(
        Date,
        Is,
        mapOf(
          "start" to "2020-01-10",
          "frequency" to "weekly",
          "endMode" to "after_n_occurrences",
          "endOccurrences" to 2,
        ),
      )
    assertThat(afterTwo.eval(tx(date = "2020-01-17"))).isTrue()
    assertThat(afterTwo.eval(tx(date = "2020-01-24"))).isFalse()

    val onDate =
      cond(
        Date,
        Is,
        mapOf(
          "start" to "2020-01-10",
          "frequency" to "daily",
          "endMode" to "on_date",
          "endDate" to "2020-01-12",
        ),
      )
    assertThat(onDate.eval(tx(date = "2020-01-12"))).isTrue()
    assertThat(onDate.eval(tx(date = "2020-01-13"))).isFalse()
  }

  @Test
  fun `Invalid recurring dates are rejected`() {
    assertFailure {
      compile(cond(Date, Is, mapOf("start" to "2020-01-10", "frequency" to "hourly")))
    }
      .isInstanceOf<RuleValidationException>()
    assertFailure { compile(cond(Date, Is, mapOf("frequency" to "daily"))) }
      .isInstanceOf<RuleValidationException>()
  }

  @Test
  fun `Date conditions work with comparison operators`() {
    var c = cond(Date, GreaterThan, "2020-08-10")
    assertThat(c.eval(tx(date = "2020-08-11"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-10"))).isFalse()

    c = cond(Date, GreaterThanOrEquals, "2020-08-10")
    assertThat(c.eval(tx(date = "2020-08-11"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-10"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-09"))).isFalse()

    c = cond(Date, LessThan, "2020-08-10")
    assertThat(c.eval(tx(date = "2020-08-09"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-10"))).isFalse()

    c = cond(Date, LessThanOrEquals, "2020-08-10")
    assertThat(c.eval(tx(date = "2020-08-09"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-10"))).isTrue()
    assertThat(c.eval(tx(date = "2020-08-11"))).isFalse()
  }

  @Test
  fun `Id works with all operators`() {
    var c = cond(Payee, Is, "foo")
    assertThat(c.eval(tx(payee = "foo"))).isTrue()
    assertThat(c.eval(tx(payee = "FOO"))).isTrue()
    assertThat(c.eval(tx(payee = "foo2"))).isFalse()

    c = cond(Payee, OneOf, listOf("foo", "bar"))
    assertThat(c.eval(tx(payee = "foo"))).isTrue()
    assertThat(c.eval(tx(payee = "FOO"))).isTrue()
    assertThat(c.eval(tx(payee = "Bar"))).isTrue()
    assertThat(c.eval(tx(payee = "bar2"))).isFalse()

    c = cond(Payee, IsNot, "foo")
    assertThat(c.eval(tx(payee = "foo"))).isFalse()
    assertThat(c.eval(tx(payee = "bar"))).isTrue()
    assertThat(c.eval(tx(payee = null))).isTrue()

    c = cond(Category, NotOneOf, listOf("foo", "bar"))
    assertThat(c.eval(tx(category = "foo"))).isFalse()
    assertThat(c.eval(tx(category = "baz"))).isTrue()
    // Null fields never match notOneOf
    assertThat(c.eval(tx(category = null))).isFalse()
  }

  @Test
  fun `Contains on an id field reads the id, not the name`() {
    val context = context(payees = listOf(RulePayee(PayeeId("payee-1"), "Amazon")))
    assertThat(cond(Payee, Contains, "amazon").eval(tx(payee = "payee-1"), context)).isFalse()
    assertThat(cond(Payee, Contains, "yee-").eval(tx(payee = "payee-1"), context)).isTrue()
    assertThat(cond(Payee, DoesNotContain, "yee-").eval(tx(payee = "payee-1"), context)).isFalse()
    assertThat(cond(Payee, Matches, "^payee").eval(tx(payee = "payee-1"), context)).isTrue()
  }

  @Test
  fun `String works with all operators`() {
    var c = cond(Notes, Is, "foo")
    assertThat(c.eval(tx(notes = "foo"))).isTrue()
    assertThat(c.eval(tx(notes = "FOO"))).isTrue()
    assertThat(c.eval(tx(notes = "foo2"))).isFalse()

    c = cond(ImportedPayee, OneOf, listOf("foo", "bar"))
    assertThat(c.eval(tx(importedPayee = "foo"))).isTrue()
    assertThat(c.eval(tx(importedPayee = "FOO"))).isTrue()
    assertThat(c.eval(tx(importedPayee = "Bar"))).isTrue()
    assertThat(c.eval(tx(importedPayee = "bar2"))).isFalse()

    c = cond(Notes, Contains, "foo")
    assertThat(c.eval(tx(notes = "bar foo baz"))).isTrue()
    assertThat(c.eval(tx(notes = "bar FOOb"))).isTrue()
    assertThat(c.eval(tx(notes = "foo"))).isTrue()
    assertThat(c.eval(tx(notes = "foob"))).isTrue()
    assertThat(c.eval(tx(notes = "bfoo"))).isTrue()
    assertThat(c.eval(tx(notes = "bfo"))).isFalse()
    assertThat(c.eval(tx(notes = "f o o"))).isFalse()

    c = cond(Notes, Matches, "^fo*$")
    assertThat(c.eval(tx(notes = "bar foo baz"))).isFalse()
    assertThat(c.eval(tx(notes = "bar FOOb"))).isFalse()
    assertThat(c.eval(tx(notes = "foo"))).isTrue()
    assertThat(c.eval(tx(notes = "FOOOO"))).isTrue()
    assertThat(c.eval(tx(notes = "foob"))).isFalse()
    assertThat(c.eval(tx(notes = "bfoo"))).isFalse()
    assertThat(c.eval(tx(notes = "f o o"))).isFalse()

    c = cond(Notes, DoesNotContain, "foo")
    assertThat(c.eval(tx(notes = "bar"))).isTrue()
    assertThat(c.eval(tx(notes = "a FOO b"))).isFalse()
    // A missing string counts as empty, so it doesn't contain anything
    assertThat(c.eval(tx(notes = null))).isTrue()

    c = cond(ImportedPayee, NotOneOf, listOf("foo", "", null))
    assertThat(c.eval(tx(importedPayee = "Foo"))).isFalse()
    assertThat(c.eval(tx(importedPayee = "bar"))).isTrue()
  }

  @Test
  fun `Matches handles invalid regex`() {
    assertThat(cond(Notes, Matches, "fo**").eval(tx(notes = "foo"))).isFalse()
  }

  @Test
  fun `String values are validated`() {
    assertFailure { compile(cond(Notes, Contains, "")) }
      .messageContains("must have non-empty string")
    assertFailure { compile(cond(Notes, Is, null)) }.messageContains("Invalid string value")
    assertFailure { compile(cond(ImportedPayee, OneOf, "foo")) }
      .messageContains("oneOf must have an array value")
  }

  @Test
  fun `Ops are validated against the field`() {
    assertFailure { compile(cond(Notes, OneOf, listOf("foo"))) }
      .messageContains("Invalid condition operator")
    assertFailure { compile(cond(ImportedPayee, HasTags, "#foo")) }
      .messageContains("Invalid condition operator")
    assertFailure { compile(cond(Payee, OnBudget, null)) }
      .messageContains("Invalid condition operator")
    assertFailure { compile(cond(Date, IsBetween, mapOf("num1" to 1, "num2" to 2))) }
      .messageContains("Invalid condition operator")
    assertFailure { compile(cond(Unknown, Is, "foo")) }.messageContains("Invalid condition field")
  }

  @Test
  fun `Stored field names are read as their public names`() {
    assertThat(cond(Description, Is, "payee-1").eval(tx(payee = "payee-1"))).isTrue()
    assertThat(cond(ImportedDescription, Is, "kroger").eval(tx(importedPayee = "Kroger"))).isTrue()
    assertThat(cond(Acct, Is, ACCOUNT.value).eval(tx())).isTrue()
  }

  @Test
  fun `Number validates value`() {
    compile(cond(Amount, IsApprox, 34))
    compile(cond(Amount, IsBetween, mapOf("num1" to 0, "num2" to 10)))

    assertFailure { compile(cond(Amount, IsApprox, "hello")) }
      .messageContains("Value must be a number or between amount")
    assertFailure { compile(cond(Amount, Is, mapOf("num1" to 0, "num2" to 10))) }
      .messageContains("Invalid number value for")
    assertFailure { compile(cond(Amount, IsBetween, 34.22)) }
      .messageContains("Invalid between value for")
    assertFailure { compile(cond(Amount, IsBetween, mapOf("num1" to 0))) }
      .messageContains("Value must be a number or between amount")
    assertFailure { compile(cond(Amount, Is, null)) }.messageContains("Field cannot be empty")
  }

  @Test
  fun `Number works with all operators`() {
    var c = cond(Amount, Is, 155)
    assertThat(c.eval(tx(amount = 155))).isTrue()
    assertThat(c.eval(tx(amount = 167))).isFalse()

    c = cond(Amount, IsApprox, 1535)
    assertThat(c.eval(tx(amount = 1540))).isTrue()
    assertThat(c.eval(tx(amount = 1300))).isFalse()
    assertThat(c.eval(tx(amount = 1650))).isTrue()
    assertThat(c.eval(tx(amount = 1800))).isFalse()

    c = cond(Amount, IsBetween, mapOf("num1" to 32, "num2" to 86))
    assertThat(c.eval(tx(amount = 30))).isFalse()
    assertThat(c.eval(tx(amount = 32))).isTrue()
    assertThat(c.eval(tx(amount = 80))).isTrue()
    assertThat(c.eval(tx(amount = 86))).isTrue()
    assertThat(c.eval(tx(amount = 90))).isFalse()

    c = cond(Amount, IsBetween, mapOf("num1" to -16, "num2" to -20))
    assertThat(c.eval(tx(amount = -18))).isTrue()
    assertThat(c.eval(tx(amount = -12))).isFalse()

    c = cond(Amount, GreaterThan, 155)
    assertThat(c.eval(tx(amount = 155))).isFalse()
    assertThat(c.eval(tx(amount = 167))).isTrue()
    assertThat(c.eval(tx(amount = 150))).isFalse()

    c = cond(Amount, GreaterThanOrEquals, 155)
    assertThat(c.eval(tx(amount = 155))).isTrue()
    assertThat(c.eval(tx(amount = 167))).isTrue()
    assertThat(c.eval(tx(amount = 150))).isFalse()

    c = cond(Amount, LessThan, 155)
    assertThat(c.eval(tx(amount = 155))).isFalse()
    assertThat(c.eval(tx(amount = 167))).isFalse()
    assertThat(c.eval(tx(amount = 150))).isTrue()

    c = cond(Amount, LessThanOrEquals, 155)
    assertThat(c.eval(tx(amount = 155))).isTrue()
    assertThat(c.eval(tx(amount = 167))).isFalse()
    assertThat(c.eval(tx(amount = 150))).isTrue()
  }

  @Test
  fun `Inflow and outflow options compare the amount's size in that direction`() {
    val outflow = cond(Amount, GreaterThan, 1000, ConditionOptions(outflow = true))
    assertThat(outflow.eval(tx(amount = -1500))).isTrue()
    assertThat(outflow.eval(tx(amount = -500))).isFalse()
    assertThat(outflow.eval(tx(amount = 1500))).isFalse()

    val inflow = cond(Amount, GreaterThan, 1000, ConditionOptions(inflow = true))
    assertThat(inflow.eval(tx(amount = 1500))).isTrue()
    assertThat(inflow.eval(tx(amount = -1500))).isFalse()
  }

  @Test
  fun `Boolean validates value`() {
    compile(cond(Cleared, Is, true))
    assertFailure { compile(cond(Cleared, Is, "true")) }.messageContains("Value must be a boolean")
    assertFailure { compile(cond(Cleared, Is, null)) }.messageContains("Field cannot be empty")
  }

  @Test
  fun `Boolean works with is`() {
    var c = cond(Cleared, Is, true)
    assertThat(c.eval(tx(cleared = true))).isTrue()
    assertThat(c.eval(tx(cleared = false))).isFalse()

    c = cond(Cleared, Is, false)
    assertThat(c.eval(tx(cleared = false))).isTrue()
    assertThat(c.eval(tx(cleared = true))).isFalse()
  }

  @Test
  fun `Transfer and parent conditions never match while rules run`() {
    assertThat(cond(Transfer, Is, false).eval(tx())).isFalse()
    assertThat(cond(Parent, Is, false).eval(tx())).isFalse()
  }

  @Test
  fun `hasTags needs every tag and hasAnyTag needs one`() {
    val hasTags = cond(Notes, HasTags, "#food ##Travel")
    assertThat(hasTags.eval(tx(notes = "Lunch #food #travel"))).isTrue()
    assertThat(hasTags.eval(tx(notes = "Lunch #FOOD#TRAVEL"))).isTrue()
    assertThat(hasTags.eval(tx(notes = "Lunch #food"))).isFalse()
    // A tag must be the whole word, and ## escapes it
    assertThat(hasTags.eval(tx(notes = "#foodie #travel"))).isFalse()
    assertThat(hasTags.eval(tx(notes = "##food #travel"))).isFalse()

    // Without a '#' the words are still read as tags
    assertThat(cond(Notes, HasTags, "food").eval(tx(notes = "a #food"))).isTrue()

    val hasAnyTag = cond(Notes, HasAnyTag, "#food #travel")
    assertThat(hasAnyTag.eval(tx(notes = "#travel"))).isTrue()
    assertThat(hasAnyTag.eval(tx(notes = "#other"))).isFalse()
    assertThat(hasAnyTag.eval(tx(notes = null))).isFalse()
  }

  @Test
  fun `hasTags reads the payee name`() {
    val context = context(payees = listOf(RulePayee(PayeeId("p"), "Shop #online")))
    assertThat(cond(PayeeName, HasTags, "#online").eval(tx(payee = "p"), context)).isTrue()
  }

  @Test
  fun `onBudget and offBudget read the account`() {
    assertThat(cond(Account, OnBudget, null).eval(tx(account = ACCOUNT))).isTrue()
    assertThat(cond(Account, OffBudget, null).eval(tx(account = ACCOUNT))).isFalse()
    assertThat(cond(Account, OffBudget, null).eval(tx(account = OFF_BUDGET_ACCOUNT))).isTrue()
    // An unknown account is neither
    val unknown = AccountId("missing")
    assertThat(cond(Account, OnBudget, null).eval(tx(account = unknown))).isFalse()
    assertThat(cond(Account, OffBudget, null).eval(tx(account = unknown))).isFalse()
  }

  @Test
  fun `category_group reads the category's group`() {
    val context =
      context(categoryGroups = mapOf(CategoryId("electric") to CategoryGroupId("bills")))
    val bills = cond(CategoryGroup, Is, "bills")
    assertThat(bills.eval(tx(category = "electric"), context)).isTrue()
    assertThat(bills.eval(tx(category = "unknown"), context)).isFalse()

    // Without a category, nothing about its group matches
    assertThat(cond(CategoryGroup, IsNot, "bills").eval(tx(category = null), context)).isFalse()
  }

  @Test
  fun `payee_name reads the payee's name`() {
    val context = context(payees = listOf(RulePayee(PayeeId("p"), "Kroger")))
    val c = cond(PayeeName, Contains, "KROG")
    assertThat(c.eval(tx(payee = "p"), context)).isTrue()
    assertThat(c.eval(tx(payee = null), context)).isFalse()
    assertThat(cond(PayeeName, Is, "").eval(tx(payee = null), context)).isTrue()
  }

  @Test
  fun `Unknown operator for type is invalid`() {
    assertFailure { compile(cond(Cleared, Contains, "x")) }
      .all {
        isInstanceOf<RuleValidationException>()
        messageContains("Invalid condition operator")
      }
  }
}
