@file:OptIn(ExperimentalCoroutinesApi::class)

package actual.budget.reports.vm

import actual.budget.db.BudgetDatabase
import actual.budget.db.GetPositionAndSize
import actual.budget.db.dao.DashboardDao
import actual.budget.db.dao.DashboardDao.Companion.DEFAULT_HEIGHT
import actual.budget.db.dao.DashboardDao.Companion.DEFAULT_WIDTH
import actual.budget.di.BudgetGraphHolder
import actual.budget.model.AndroidBudgetFiles
import actual.budget.model.BudgetFiles
import actual.budget.model.WidgetType
import actual.core.model.RandomUuidGenerator
import actual.core.model.UuidGenerator
import actual.test.assertListEmitted
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.standardDispatcher
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class ChooseReportTypeViewModelTest {
  // real
  private lateinit var viewModel: ChooseReportTypeViewModel
  private lateinit var context: Context
  private lateinit var components: BudgetGraphHolder
  private lateinit var fileSystem: FileSystem
  private lateinit var dashboardDao: DashboardDao
  private lateinit var database: BudgetDatabase
  private lateinit var contexts: CoroutineContexts

  // fake
  private lateinit var uuidGenerator: UuidGenerator
  private lateinit var files: BudgetFiles

  @BeforeTest
  fun before() {
    uuidGenerator = RandomUuidGenerator()
    context = ApplicationProvider.getApplicationContext()
    fileSystem = FileSystem.SYSTEM
    files = AndroidBudgetFiles(context, fileSystem)
  }

  @AfterTest
  fun after() {
    components.close()
  }

  @Test
  fun `Inserting each expected slot in sequence`() = runVmTest {
    getPositionAndSizeFlow().test {
      assertListEmitted()

      createReport()
      assertListEmitted(
        position(x = 0, y = 0),
      )

      createReport()
      assertListEmitted(
        position(x = 1 * DEFAULT_WIDTH, y = 0),
        position(x = 0, y = 0),
      )

      createReport()
      assertListEmitted(
        position(x = 2 * DEFAULT_WIDTH, y = 0),
        position(x = 1 * DEFAULT_WIDTH, y = 0),
        position(x = 0, y = 0),
      )

      createReport()
      assertListEmitted(
        position(x = 0, y = 2),
        position(x = 2 * DEFAULT_WIDTH, y = 0),
        position(x = 1 * DEFAULT_WIDTH, y = 0),
        position(x = 0, y = 0),
      )

      createReport()
      assertListEmitted(
        position(x = 1 * DEFAULT_WIDTH, y = 2),
        position(x = 0, y = 2),
        position(x = 2 * DEFAULT_WIDTH, y = 0),
        position(x = 1 * DEFAULT_WIDTH, y = 0),
        position(x = 0, y = 0),
      )

      // etc etc

      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun createReport() = viewModel.createReport(WidgetType.NetWorth)

  private inline fun runVmTest(crossinline testBody: suspend TestScope.() -> Unit) = runTest {
    contexts = TestCoroutineContexts(standardDispatcher)
    components = BudgetGraphHolder(context, files, this, contexts)
    val component = components.update(TEST_METADATA)
    database = component.database
    dashboardDao = DashboardDao(database)

    viewModel = ChooseReportTypeViewModel(
      uuidGenerator = uuidGenerator,
      budgetComponents = components,
      inputs = ChooseReportTypeViewModel.Inputs(TEST_TOKEN, TEST_BUDGET_ID),
    )

    testBody()
  }

  private fun getPositionAndSizeFlow(): Flow<List<GetPositionAndSize>> = database
    .dashboardQueries
    .getPositionAndSize()
    .asFlow()
    .mapToList(contexts.io)
    .distinctUntilChanged()

  private fun position(x: Long, y: Long) = GetPositionAndSize(x, y, width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT)
}
