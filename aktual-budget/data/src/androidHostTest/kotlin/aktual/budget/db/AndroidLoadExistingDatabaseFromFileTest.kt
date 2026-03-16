package aktual.budget.db

import aktual.budget.model.BudgetFiles
import aktual.core.model.AndroidAppDirectory
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlin.test.BeforeTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidLoadExistingDatabaseFromFileTest : LoadExistingDatabaseFromFileTest() {
  @BeforeTest
  fun before() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val directoryPath = AndroidAppDirectory(context, fileSystem).get()
    budgetFiles = BudgetFiles(fileSystem, directoryPath)
    provider = AndroidBudgetDatabaseBuilderProvider(context, budgetFiles)
  }
}
