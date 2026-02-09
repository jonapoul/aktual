package aktual.budget.model

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import okio.FileSystem
import okio.Path.Companion.toOkioPath

@Inject
@ContributesBinding(AppScope::class)
class AndroidBudgetFiles(
    context: Context,
    override val fileSystem: FileSystem,
) : BudgetFiles {
  override val directoryPath =
      context.getDatabasePath("unused").parentFile?.resolve("budgets")?.toOkioPath()
          ?: error("Null budgets directory!")
}
