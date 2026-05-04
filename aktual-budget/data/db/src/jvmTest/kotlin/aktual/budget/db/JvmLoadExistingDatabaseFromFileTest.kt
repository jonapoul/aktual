package aktual.budget.db

import aktual.core.model.AppDirectory
import aktual.core.model.JvmAppDirectory

class JvmLoadExistingDatabaseFromFileTest : LoadExistingDatabaseFromFileTest() {
  override fun appDirectory(): AppDirectory = JvmAppDirectory
}
