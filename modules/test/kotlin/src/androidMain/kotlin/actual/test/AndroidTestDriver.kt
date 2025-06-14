package actual.test

import actual.budget.db.AndroidSqlDriverFactory
import actual.budget.model.AndroidBudgetFiles
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import okio.FileSystem

fun androidDriverFactory(
  id: BudgetId,
  fileSystem: FileSystem,
  context: Context = ApplicationProvider.getApplicationContext(),
  budgetFiles: BudgetFiles = AndroidBudgetFiles(context, fileSystem),
) = AndroidSqlDriverFactory(id, context, budgetFiles)
