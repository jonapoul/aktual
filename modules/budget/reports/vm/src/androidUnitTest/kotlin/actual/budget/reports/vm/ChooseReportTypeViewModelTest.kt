@file:OptIn(ExperimentalCoroutinesApi::class)

package actual.budget.reports.vm

import actual.app.di.AndroidViewModelGraph
import actual.app.di.CoroutineContainer
import actual.app.di.GithubApiContainer
import actual.budget.db.BudgetDatabase
import actual.budget.db.GetPositionAndSize
import actual.budget.db.dao.DashboardDao
import actual.budget.db.dao.DashboardDao.Companion.DEFAULT_HEIGHT
import actual.budget.db.dao.DashboardDao.Companion.DEFAULT_WIDTH
import actual.budget.model.WidgetType
import actual.core.di.AppGraph
import actual.core.di.BudgetGraphHolder
import actual.core.di.assisted
import actual.test.DummyViewModelContainer
import actual.test.assertListEmitted
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.Flaky
import alakazam.test.core.FlakyTestRule
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.standardDispatcher
import android.content.Context
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.turbine.test
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class ChooseReportTypeViewModelTest : AppGraph.Holder {
  @get:Rule val flakyTestRule = FlakyTestRule()

  // real
  private lateinit var viewModel: ChooseReportTypeViewModel
  private lateinit var context: Context
  private lateinit var budgetGraphHolder: BudgetGraphHolder
  private lateinit var dashboardDao: DashboardDao
  private lateinit var database: BudgetDatabase
  private lateinit var contexts: CoroutineContexts

  // fake
  private lateinit var appGraph: TestAppGraph

  override fun invoke(): AppGraph = appGraph

  @AfterTest
  fun after() {
    budgetGraphHolder.close()
  }

  @Test
  @Flaky(retry = 5, reason = "Sometimes times out when awaiting emission")
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
    context = ApplicationProvider.getApplicationContext()

    val appGraphHolder = this@ChooseReportTypeViewModelTest
    appGraph = createGraphFactory<TestAppGraph.Factory>().create(
      scope = this,
      contexts = contexts,
      context = context,
      holder = appGraphHolder,
    )

    budgetGraphHolder = appGraph.budgetGraphHolder
    val budgetGraph = budgetGraphHolder.update(TEST_METADATA)
    database = budgetGraph.database
    dashboardDao = DashboardDao(database)

    viewModel = appGraph
      .create(CreationExtras.Empty)
      .assisted<ChooseReportTypeViewModel, ChooseReportTypeViewModel.Factory> { create(TEST_BUDGET_ID) }

    testBody()
  }

  private fun getPositionAndSizeFlow(): Flow<List<GetPositionAndSize>> = database
    .dashboardQueries
    .getPositionAndSize()
    .asFlow()
    .mapToList(contexts.io)
    .distinctUntilChanged()

  private fun position(x: Long, y: Long) = GetPositionAndSize(x, y, width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT)

  @DependencyGraph(
    scope = AppScope::class,
    excludes = [CoroutineContainer::class, GithubApiContainer::class],
    bindingContainers = [DummyViewModelContainer::class],
  )
  internal interface TestAppGraph : AppGraph, AndroidViewModelGraph.Factory {
    val budgetGraphHolder: BudgetGraphHolder

    @DependencyGraph.Factory
    fun interface Factory {
      fun create(
        @Provides scope: CoroutineScope,
        @Provides contexts: CoroutineContexts,
        @Provides context: Context,
        @Provides holder: AppGraph.Holder,
      ): TestAppGraph
    }
  }
}
