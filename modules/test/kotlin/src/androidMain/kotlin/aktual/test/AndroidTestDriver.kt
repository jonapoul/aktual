/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.test

import aktual.budget.db.AndroidSqlDriverFactory
import aktual.budget.model.AndroidBudgetFiles
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import okio.FileSystem

fun androidDriverFactory(
  id: BudgetId,
  fileSystem: FileSystem,
  context: Context = ApplicationProvider.getApplicationContext(),
  budgetFiles: BudgetFiles = AndroidBudgetFiles(context, fileSystem),
) = AndroidSqlDriverFactory(id, context, budgetFiles)
