package aktual.budget.transactions.domain

import aktual.budget.db.Transactions
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionType
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.TransactionRule
import aktual.budget.rules.domain.TransactionRulesLoader
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive

internal class RuleMatcherTest {
  private var isSetUp = false
  private var nextId = 1

  @Test
  fun `Finds what the conditions match and what the actions change`() = runWriterTest {
    val shop = insert(notes = "weekly shop", date = LocalDate(2026, 1, 1))
    val newer = insert(notes = "another shop", date = LocalDate(2026, 2, 1))
    insert(notes = "rent")

    val matches = matcher().find(rule(SET_GROCERIES))

    assertThat(matches).isNotNull().all {
      transform { m -> m.matches.map { it.before.id } }.containsExactly(newer, shop)
      transform { m -> m.matches.map { it.after.category } }.containsExactly(GROCERIES, GROCERIES)
      transform { it.names[GROCERIES.value] }.isEqualTo("Groceries")
      transform { it.names[ACCOUNT.value] }.isEqualTo(ACCOUNT.value)
    }
  }

  @Test
  fun `No conditions match nothing`() = runWriterTest {
    insert(notes = "weekly shop")
    val matches = matcher().find(rule(SET_GROCERIES).copy(conditions = emptyList()))
    assertThat(matches?.matches).isEqualTo(emptyList())
  }

  @Test
  fun `Invalid actions show nothing`() = runWriterTest {
    insert(notes = "weekly shop")
    val emptyAccount =
      RuleAction(value = JsonPrimitive(""), op = RuleAction.Op.Set, field = Account, type = Id)
    assertThat(matcher().find(rule(emptyAccount))).isNull()
  }

  @Test
  fun `Applying writes only the selected transactions that change`() = runWriterTest {
    val shop = insert(notes = "weekly shop")
    val unchanged = insert(notes = "shop again", category = GROCERIES)
    val unselected = insert(notes = "shop later")
    val matcher = matcher()

    assertThat(matcher.apply(rule(SET_GROCERIES), setOf(shop, unchanged))).isEqualTo(1)
    assertThat(transactionDao.row(shop)?.category).isEqualTo(GROCERIES)
    assertThat(transactionDao.row(unselected)?.category).isNull()
    matcher.apply(rule(SET_GROCERIES), setOf(unselected))
    assertThat(matcher.find(rule(SET_GROCERIES))?.matches?.none { it.isChanged })
      .isNotNull()
      .isTrue()
  }

  @Test
  fun `Applying a split rule splits the transaction`() = runWriterTest {
    val shop = insert(notes = "weekly shop", amount = Amount(-1000))
    val split =
      listOf(
        RuleAction(
          value = JsonPrimitive(400),
          op = SetSplitAmount,
          options = RuleAction.Options(method = FixedAmount, splitIndex = 1),
        ),
        SET_GROCERIES.copy(options = RuleAction.Options(splitIndex = 1)),
        RuleAction(
          value = null,
          op = SetSplitAmount,
          options = RuleAction.Options(method = Remainder, splitIndex = 2),
        ),
      )

    assertThat(matcher().apply(rule(*split.toTypedArray()), setOf(shop))).isEqualTo(1)

    assertThat(transactionDao.row(shop)).isNotNull().prop(Transactions::isParent).isEqualTo(true)
    val children = transactionDao.childIds(listOf(shop)).mapNotNull { transactionDao.row(it) }
    assertThat(children).hasSize(2)
    assertThat(children.map { it.amount }.toSet()).isEqualTo(setOf(Amount(400), Amount(-1400)))
  }

  private suspend fun WriterTestScope.insert(
    notes: String,
    date: LocalDate = LocalDate(2026, 1, 1),
    amount: Amount = Amount(-1000),
    category: CategoryId? = null,
  ): TransactionId {
    if (!isSetUp) {
      insertAccount(ACCOUNT)
      insertCategory(GROCERIES, name = "Groceries")
      isSetUp = true
    }
    return writer.write {
      insert(
        NewTransaction(
          account = ACCOUNT,
          date = date,
          amount = amount,
          notes = notes,
          category = category,
        ),
      )
    }
  }

  private fun WriterTestScope.matcher() =
    RuleMatcher(
      transactionDao = transactionDao,
      accountDao = accountDao,
      categoryDao = categoryDao,
      payeeDao = payeeDao,
      rulesLoader =
        TransactionRulesLoader(RulesDao(database), RuleContextDao(database)) { "new-${nextId++}" },
      writer = writer,
      uuidGenerator = { "split-${nextId++}" },
    )

  private fun rule(vararg actions: RuleAction) =
    TransactionRule(
      id = RuleId("r"),
      conditions = listOf(SHOP),
      actions = actions.toList(),
    )

  private companion object {
    val ACCOUNT = AccountId("acct")
    val GROCERIES = CategoryId("groceries")
    val SHOP = Condition(Notes, Contains, JsonPrimitive("shop"), type = ConditionType.String)
    val SET_GROCERIES =
      RuleAction(
        value = JsonPrimitive(GROCERIES.value),
        op = RuleAction.Op.Set,
        field = Category,
        type = Id,
      )
  }
}
