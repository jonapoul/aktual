package aktual.budget.db

import aktual.core.model.AndroidAppDirectory
import aktual.core.model.AppDirectory
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidLoadExistingDatabaseFromFileTest : LoadExistingDatabaseFromFileTest() {
  override fun appDirectory(): AppDirectory =
    AndroidAppDirectory(
      context = ApplicationProvider.getApplicationContext(),
      fileSystem = fileSystem,
    )
}
