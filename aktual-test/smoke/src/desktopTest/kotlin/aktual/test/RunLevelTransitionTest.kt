package aktual.test

import aktual.di.BudgetGraph
import aktual.di.LoggedInGraph
import aktual.di.ServerChosenGraph
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import dev.zacsweers.metro.createDynamicGraph
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
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
}
