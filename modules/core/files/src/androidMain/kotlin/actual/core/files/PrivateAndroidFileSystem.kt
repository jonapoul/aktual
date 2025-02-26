package actual.core.files

import android.content.Context
import java.io.File
import javax.inject.Inject

class PrivateAndroidFileSystem @Inject constructor(context: Context) : FileSystem {
  private val baseDirectory = context.dataDir
  private val budgetsDir = baseDirectory.resolve("budgets")

  override fun budgetFile(budgetId: String): File = budgetsDir.resolve("$budgetId.sqlite")
}
