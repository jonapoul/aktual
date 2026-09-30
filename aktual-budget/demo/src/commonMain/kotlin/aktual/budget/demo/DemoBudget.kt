package aktual.budget.demo

import aktual.budget.BudgetFiles
import aktual.budget.db.SqlDriverFactory
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.core.Calendar
import aktual.di.AppScope
import aktual.di.BudgetGraph
import aktual.di.Initializable
import aktual.di.RunLevelController
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.withContext
import logcat.logcat

@Inject
@SingleIn(AppScope::class)
@ContributesIntoSet(AppScope::class, binding<@ForScope(AppScope::class) Initializable>())
class DemoBudget(
  private val files: BudgetFiles,
  private val driverFactory: SqlDriverFactory,
  private val runLevelController: RunLevelController,
  private val calendar: Calendar,
  private val contexts: CoroutineContexts,
) : Initializable {
  override fun initialize() {
    // Clear out anything left behind if the app was killed with the demo open
    delete()
  }

  suspend fun open(): BudgetGraph {
    val metadata = DbMetadata(budgetName = DEMO_BUDGET_NAME, id = ID.value)
    withContext(contexts.io) {
      delete()
      val bytes = Res.readBytes(DEMO_DATABASE_PATH)
      files.fileSystem.write(files.database(ID, mkdirs = true)) { write(bytes) }
      files.writeMetadata(ID, metadata)
      driverFactory.create(ID).use { driver ->
        driver.shiftDemoDates(from = DEMO_GENERATED_ON, to = calendar.today())
      }
    }
    logcat.i { "Opening demo budget" }
    return runLevelController.onOfflineBudget(ID, metadata)
  }

  suspend fun close() {
    logcat.i { "Closing demo budget" }
    runLevelController.onBudgetClosed()
    withContext(contexts.io) { delete() }
  }

  private fun delete() = files.fileSystem.deleteRecursively(files.directory(ID))

  private companion object {
    val ID = BudgetId.Demo
    const val DEMO_BUDGET_NAME = "Demo Budget"
    const val DEMO_DATABASE_PATH = "files/demo-budget.sqlite"
  }
}
