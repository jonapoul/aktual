/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
