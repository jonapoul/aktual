package aktual.budget.payees.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.budget.model.PayeeId
import aktual.test.inMemoryDriverFactory
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class PayeeMergerTest {
  @Test
  fun `Merged payees' transactions move to the target`() = runMergerTest {
    // given
    insertPayees()

    // when
    val merge = merger.merge(ARGOS, listOf(TESCO))

    // then
    assertThat(merge)
      .isEqualTo(
        PayeeMerge(
          target = ARGOS,
          merged = listOf(TESCO),
          oldTargets = mapOf(TESCO to TESCO, OLD_TESCO to TESCO),
        ),
      )
    assertThat(transactionCounts()).containsExactly(ARGOS to 3, TRANSFER to 0)
  }

  @Test
  fun `Undoing a merge moves the transactions back`() = runMergerTest {
    // given
    insertPayees()
    val merge = requireNotNull(merger.merge(ARGOS, listOf(TESCO)))

    // when
    merger.undo(merge)

    // then
    assertThat(transactionCounts()).containsExactly(ARGOS to 1, TRANSFER to 0, TESCO to 2)
    assertThat(payeeDao.mappingTarget(OLD_TESCO)).isEqualTo(TESCO)
  }

  @Test
  fun `Transfer payees aren't merged`() = runMergerTest {
    // given
    insertPayees()

    // when
    val intoTransfer = merger.merge(TRANSFER, listOf(TESCO))
    val fromTransfer = merger.merge(ARGOS, listOf(TRANSFER))

    // then
    assertThat(intoTransfer).isNull()
    assertThat(fromTransfer).isNull()
    assertThat(changes).isEmpty()
  }

  @Test
  fun `The target isn't merged into itself`() = runMergerTest {
    // given
    insertPayees()

    // when
    val merge = merger.merge(ARGOS, listOf(ARGOS))

    // then
    assertThat(merge).isNull()
    assertThat(changes).isEmpty()
  }

  private class MergerTestScope(
    val database: BudgetDatabase,
    private val syncDao: SyncDao,
  ) : BudgetSyncController {
    val changes = mutableListOf<LocalChange>()
    val payeeDao = PayeeDao(database)
    val merger = PayeeMerger(payeeDao, syncController = this)

    // Applied to the database, as BudgetSyncControllerImpl does
    override suspend fun syncChanges(changes: List<LocalChange>) {
      this.changes += changes
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit

    // Argos has one transaction and Tesco two, one of them through an old payee already merged
    // into Tesco
    suspend fun insertPayees() {
      AccountDao(database).insert(AccountId(ACCOUNT), name = "Current")
      payeeDao.insert(ARGOS, "Argos")
      payeeDao.insert(TESCO, "Tesco")
      database.payeesQueries.insert(
        id = OLD_TESCO,
        name = "Tesco Express",
        category = null,
        tombstone = true,
        transfer_acct = null,
        favorite = false,
        learn_categories = null,
      )
      database.payeeMappingQueries.insert(id = OLD_TESCO, targetId = TESCO)
      payeeDao.insert(TRANSFER, null, transferAccount = AccountId(ACCOUNT))
      val transactions = TransactionDao(database)
      transactions.insert("t1", ACCOUNT, null, ARGOS.value, DATE)
      transactions.insert("t2", ACCOUNT, null, TESCO.value, DATE)
      transactions.insert("t3", ACCOUNT, null, OLD_TESCO.value, DATE)
    }

    suspend fun transactionCounts(): List<Pair<PayeeId, Int>> =
      payeeDao.list().map { it.id to it.num_transactions.toInt() }
  }

  private fun runMergerTest(action: suspend MergerTestScope.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      MergerTestScope(database, SyncDao(database, driver, Clock.System)).action()
    }
  }

  private companion object {
    const val ACCOUNT = "account"
    val ARGOS = PayeeId("argos")
    val TESCO = PayeeId("tesco")
    val OLD_TESCO = PayeeId("old-tesco")
    val TRANSFER = PayeeId("transfer")
    val DATE = LocalDate(2026, 10, 1)
  }
}
