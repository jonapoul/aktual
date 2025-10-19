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
package aktual.core.di

import aktual.budget.db.JvmSqlDriverFactory
import aktual.budget.model.BudgetFiles
import aktual.budget.model.DbMetadata
import aktual.budget.model.database
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class JvmBudgetGraphBuilder(
  private val files: BudgetFiles,
  private val appGraphHolder: AppGraph.Holder,
) : BudgetGraph.Builder {
  override fun invoke(metadata: DbMetadata): BudgetGraph {
    val dbFile = files.database(metadata.cloudFileId, mkdirs = true).toFile()
    val driverFactory = JvmSqlDriverFactory(dbFile)
    return appGraphHolder.get().create(metadata.cloudFileId, metadata, driverFactory)
  }
}
