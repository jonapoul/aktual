package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.CustomReports
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DatabaseTables.CUSTOM_REPORTS
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.withoutResult
import aktual.budget.model.CustomReportId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate

class CustomReportLoaderTest {
  @Test
  fun `Missing report`() = runLoaderTest { loader, _ ->
    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(MISSING)
    }
  }

  @Test
  fun `Deleted report`() = runLoaderTest { loader, _ ->
    insert(emptyCustomReport(ID).copy(name = "Deleted", tombstone = true))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(MISSING)
    }
  }

  @Test
  fun `Unreadable report`() = runLoaderTest { loader, _ ->
    insert(emptyCustomReport(ID).copy(name = "Unreadable", graph_type = Unknown))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(UnsupportedData(InvalidMeta, Custom, "Unreadable"))
    }
  }

  @Test
  fun `Live report without transactions covers today`() = runLoaderTest { loader, _ ->
    val report = emptyCustomReport(ID).copy(name = "Spending", date_range = AllTime)
    insert(report)

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmissionIsEqualTo(
        CustomData(
          title = "Spending",
          config = checkNotNull(report.toConfig()),
          range = TODAY..TODAY,
        ),
      )
    }
  }

  @Test
  fun `Synced changes to the report are picked up`() = runLoaderTest { loader, sync ->
    insert(emptyCustomReport(ID).copy(name = "Before"))

    loader.custom(CustomReportMeta(ID)).test {
      assertThatNextEmission().transform { (it as CustomData).title }.isEqualTo("Before")

      sync.syncChanges(localChange(CUSTOM_REPORTS, ID.value, "name", "After"))
      assertThatNextEmission().transform { (it as CustomData).title }.isEqualTo("After")

      sync.syncChanges(tombstone(CUSTOM_REPORTS, ID.value))
      assertThatNextEmissionIsEqualTo(MISSING)
    }
  }

  private suspend fun BudgetDatabase.insert(report: CustomReports) =
    customReportsQueries.withoutResult {
      insert(report)
    }

  private fun runLoaderTest(
    action: suspend BudgetDatabase.(ChartDataLoader, BudgetSyncController) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val loader =
      ChartDataLoader(
        ReportsDao(this, contexts),
        CustomReportsDao(this, contexts),
        PreferencesDao(this, contexts),
        calendar = { TODAY },
        contexts = contexts,
      )
    action(loader, sync)
  }

  private companion object {
    val ID = CustomReportId("report-1")
    val TODAY = LocalDate(2026, 5, 15)
    val MISSING = UnsupportedData(MissingReport, Custom, name = null)
  }
}
