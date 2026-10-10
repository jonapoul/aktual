package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.V_schedules
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.DatabaseTables.SCHEDULES
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.localChange
import aktual.test.inMemoryDriverFactory
import app.cash.sqldelight.async.coroutines.awaitAsOne
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest

internal class SyncDaoTest {
  @Test
  fun `Schedule fields resolve for a rule that arrives by sync`() = runSyncDaoTest { database ->
    sendMessages(schedule() + rule(conditions = CONDITIONS))

    assertThat(database.schedule()).all {
      prop(V_schedules::_account).isEqualTo(ACCOUNT)
      prop(V_schedules::_amount).isEqualTo("-2500")
      prop(V_schedules::_date).isEqualTo("2026-09-23")
    }
  }

  @Test
  fun `Schedule fields follow the conditions when the rule changes`() = runSyncDaoTest { database ->
    sendMessages(schedule() + rule(conditions = CONDITIONS))
    sendMessages(listOf(LocalChange(RULES, RULE, "conditions", REORDERED_CONDITIONS.string())))

    assertThat(database.schedule()).all {
      prop(V_schedules::_account).isEqualTo(ACCOUNT)
      prop(V_schedules::_amount).isEqualTo("-2500")
      prop(V_schedules::_date).isNull()
    }
  }

  @Test
  fun `Rebuilding fills in schedules that have no paths`() = runSyncDaoTest { database ->
    // A budget file downloaded from the server arrives without any paths
    sendMessages(schedule() + rule(conditions = CONDITIONS))
    database.schedulesJsonPathsQueries.deleteAll()
    assertThat(database.schedule()).prop(V_schedules::_account).isNull()

    rebuildScheduleJsonPaths()

    assertThat(database.schedule()).all {
      prop(V_schedules::_account).isEqualTo(ACCOUNT)
      prop(V_schedules::_amount).isEqualTo("-2500")
      prop(V_schedules::_date).isEqualTo("2026-09-23")
    }
  }

  private fun schedule() =
    listOf(
      LocalChange(SCHEDULES, SCHEDULE, "rule", RULE.string()),
      localChange(SCHEDULES, SCHEDULE, "completed", 0),
      localChange(SCHEDULES, SCHEDULE, "tombstone", 0),
    )

  private fun rule(conditions: String) =
    listOf(
      LocalChange(RULES, RULE, "conditions", conditions.string()),
      LocalChange(RULES, RULE, "actions", ACTIONS.string()),
      localChange(RULES, RULE, "tombstone", 0),
    )

  private fun String.string() = MessageValue.String(this)

  private suspend fun BudgetDatabase.schedule(): V_schedules =
    schedulesQueries.getFromVSchedules().awaitAsOne()

  private fun runSyncDaoTest(action: suspend SyncDao.(BudgetDatabase) -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      SyncDao(database, driver, Clock.System).action(database)
    }
  }

  private companion object {
    const val RULE = "6e02242a-ebe0-4c7b-83e2-50a0501ded39"
    const val SCHEDULE = "a2aea0d7-00fb-4cf1-ab38-8aa7fb19ab54"
    const val ACCOUNT = "78055dbe-680f-4605-bcd5-46a67feedcec"
    const val PAYEE = "2745db0b-b454-4877-a728-3c9f2b7056e5"

    const val ACTIONS = """[{"op":"link-schedule","value":"$SCHEDULE"}]"""

    const val CONDITIONS =
      """[{"op":"is","field":"description","value":"$PAYEE"},""" +
        """{"op":"is","field":"acct","value":"$ACCOUNT"},""" +
        """{"op":"isapprox","field":"date","value":"2026-09-23"},""" +
        """{"op":"isapprox","field":"amount","value":-2500}]"""

    // No usable date, and "account" wins over the "acct" before it
    const val REORDERED_CONDITIONS =
      """[{"op":"isapprox","field":"amount","value":-2500},""" +
        """{"op":"is","field":"acct","value":"other"},""" +
        """{"op":"is","field":"account","value":"$ACCOUNT"},""" +
        """{"op":"gt","field":"date","value":"2026-09-23"}]"""
  }
}
