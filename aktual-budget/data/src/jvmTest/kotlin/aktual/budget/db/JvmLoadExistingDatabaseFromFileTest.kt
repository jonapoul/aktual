package aktual.budget.db

import aktual.budget.data.JvmBudgetDatabaseBuilderProvider
import aktual.budget.model.BudgetFiles
import aktual.core.model.JvmAppDirectory
import aktual.test.TemporaryFolder
import app.cash.burst.InterceptTest
import kotlin.test.BeforeTest

class JvmLoadExistingDatabaseFromFileTest : LoadExistingDatabaseFromFileTest() {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  @BeforeTest
  fun before() {
    val directoryPath = JvmAppDirectory(temporaryFolder.root).get()
    budgetFiles = BudgetFiles(fileSystem, directoryPath)
    provider = JvmBudgetDatabaseBuilderProvider(budgetFiles)
  }
}
