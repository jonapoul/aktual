/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.desktop

import aktual.budget.model.BudgetFiles
import aktual.core.model.Files
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import okio.FileSystem

@Inject
@ContributesBinding(AppScope::class)
class JvmBudgetFiles(
  files: Files,
  override val fileSystem: FileSystem,
) : BudgetFiles {
  override val directoryPath = files
    .userHome()
    .resolve("budgets")
}
