package actual.test

import actual.budget.model.BudgetId
import actual.db.AndroidSqlDriverFactory
import android.content.Context
import androidx.test.core.app.ApplicationProvider

fun androidDriverFactory(
  id: BudgetId,
  context: Context = ApplicationProvider.getApplicationContext(),
) = AndroidSqlDriverFactory(id, context)
