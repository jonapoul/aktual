package aktual.test

import aktual.di.BudgetGraph
import aktual.di.LoggedInGraph
import aktual.di.ServerChosenGraph
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsNone
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import dev.zacsweers.metro.createDynamicGraph
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.runTest
import logcat.LogcatLogger
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

// Regression coverage for #1336, where switching budget / re-logging in stacked duplicate run-level
// graphs
class RunLevelTransitionTest {
  private lateinit var rootDir: Path
  private lateinit var appGraph: TestJvmAppGraph

  @BeforeTest
  fun before() {
    rootDir = createTempDirectory().toOkioPath()
    appGraph =
      createDynamicGraph<TestJvmAppGraph>(
        TestAppDirectoryContainer(rootDir),
        TestBudgetFilesContainer(rootDir),
      )
    with(appGraph.runLevelController) {
      init(listOf(appGraph))
      onServerChosen(SERVER_URL)
      onLoggedIn(LOGIN_TOKEN)
      onBudget(DB_METADATA)
    }
  }

  @AfterTest
  fun after() {
    LogcatLogger.uninstall()
    appGraph.close()
    FileSystem.SYSTEM.deleteRecursively(rootDir)
  }

  // Switching budget opened a second budget while the first was still open, stacking two
  // BudgetGraphs and tripping assertAllDistinct
  @Test
  fun switchingBudgetKeepsSingleBudgetLevel() = runTest {
    appGraph.runLevelState.all().test {
      assertEmissionSize(4) // app, server-chosen, logged-in, budget A

      appGraph.runLevelController.onBudget(SECOND_DB_METADATA)

      val levels = awaitItem()
      assertThat(levels).hasSize(4)
      assertThat(levels.filterIsInstance<BudgetGraph>()).hasSize(1)
      cancelAndIgnoreRemainingEvents()
    }
  }

  // Closing a budget graph also ran the parent graphs' closeables, cancelling the app-wide scope
  @Test
  fun switchingBudgetKeepsAppScopeActive() {
    appGraph.runLevelController.onBudget(SECOND_DB_METADATA)
    assertThat(appGraph.coroutineScope.isActive).isTrue()
  }

  @Test
  fun closingBudgetKeepsAppScopeActive() {
    appGraph.runLevelController.onBudgetClosed()
    assertThat(appGraph.coroutineScope.isActive).isTrue()
  }

  @Test
  fun loggingOutKeepsAppScopeActive() {
    appGraph.runLevelController.onLoggedOut()
    assertThat(appGraph.coroutineScope.isActive).isTrue()
  }

  @Test
  fun closingBudgetCancelsBudgetScope() {
    val budgetScope = requireNotNull(appGraph.runLevelState[BudgetGraph::class]).coroutineScope
    appGraph.runLevelController.onBudgetClosed()
    assertThat(budgetScope.isActive).isFalse()
    assertThat(appGraph.coroutineScope.isActive).isTrue()
  }

  @Test
  fun switchingBudgetCancelsPreviousBudgetScope() {
    val budgetScope = requireNotNull(appGraph.runLevelState[BudgetGraph::class]).coroutineScope
    appGraph.runLevelController.onBudget(SECOND_DB_METADATA)
    assertThat(budgetScope.isActive).isFalse()
    val newBudgetScope = requireNotNull(appGraph.runLevelState[BudgetGraph::class]).coroutineScope
    assertThat(newBudgetScope.isActive).isTrue()
  }

  @Test
  fun loggingOutCancelsLoggedInAndBudgetScopes() {
    val serverChosenScope =
      requireNotNull(appGraph.runLevelState[ServerChosenGraph::class]).coroutineScope
    val loggedInScope = requireNotNull(appGraph.runLevelState[LoggedInGraph::class]).coroutineScope
    val budgetScope = requireNotNull(appGraph.runLevelState[BudgetGraph::class]).coroutineScope
    appGraph.runLevelController.onLoggedOut()
    assertThat(loggedInScope.isActive).isFalse()
    assertThat(budgetScope.isActive).isFalse()
    assertThat(serverChosenScope.isActive).isTrue()
    assertThat(appGraph.coroutineScope.isActive).isTrue()
  }

  // Child graphs inherited their parents' lifecycle hooks, so each hook also ran for every level
  // below its own
  @Test
  fun childGraphsDontHoldParentLifecycleHooks() {
    val serverChosen = requireNotNull(appGraph.runLevelState[ServerChosenGraph::class])
    val loggedIn = requireNotNull(appGraph.runLevelState[LoggedInGraph::class])
    val budget = requireNotNull(appGraph.runLevelState[BudgetGraph::class])
    val parentCloseables = serverChosen.closeables.toTypedArray()
    val parentInitializables = serverChosen.initializables.toTypedArray()
    assertThat(parentCloseables).isNotEmpty()
    assertThat(parentInitializables).isNotEmpty()

    for (child in listOf(loggedIn, budget)) {
      assertThat(child.closeables).containsNone(*parentCloseables)
      assertThat(child.initializables).containsNone(*parentInitializables)
    }
  }

  // Logging out from a budget then back in re-entered the server-chosen level without first popping
  // the open budget/logged-in levels, stacking a second ServerChosenGraph
  @Test
  fun reChoosingServerCollapsesToSingleServerChosenLevel() = runTest {
    appGraph.runLevelState.all().test {
      assertEmissionSize(4) // app, server-chosen, logged-in, budget

      appGraph.runLevelController.onServerChosen(SERVER_URL)

      val levels = awaitItem()
      assertThat(levels).hasSize(2) // collapsed back to app, server-chosen
      assertThat(levels.filterIsInstance<ServerChosenGraph>()).hasSize(1)
      assertThat(levels.filterIsInstance<LoggedInGraph>()).isEmpty()
      assertThat(levels.filterIsInstance<BudgetGraph>()).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  // Re-authenticating while a budget is open should pop the open budget and replace the logged-in
  // level, rather than stacking a second LoggedInGraph
  @Test
  fun reLoggingInCollapsesToSingleLoggedInLevel() = runTest {
    appGraph.runLevelState.all().test {
      assertEmissionSize(4) // app, server-chosen, logged-in, budget

      appGraph.runLevelController.onLoggedIn(LOGIN_TOKEN)

      val levels = awaitItem()
      assertThat(levels).hasSize(3) // collapsed back to app, server-chosen, logged-in
      assertThat(levels.filterIsInstance<LoggedInGraph>()).hasSize(1)
      assertThat(levels.filterIsInstance<BudgetGraph>()).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
