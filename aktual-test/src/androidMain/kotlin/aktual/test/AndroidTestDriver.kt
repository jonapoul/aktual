package aktual.test

import aktual.budget.db.AndroidSqlDriverFactory
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import android.content.Context
import androidx.test.core.app.ApplicationProvider

fun androidDriverFactory(
  id: BudgetId,
  context: Context = ApplicationProvider.getApplicationContext(),
  budgetFiles: BudgetFiles,
) = AndroidSqlDriverFactory(id, context, budgetFiles)
