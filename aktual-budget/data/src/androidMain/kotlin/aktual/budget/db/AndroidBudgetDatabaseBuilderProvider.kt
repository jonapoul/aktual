package aktual.budget.db

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.database
import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class AndroidBudgetDatabaseBuilderProvider(
  private val context: Context,
  private val files: BudgetFiles,
) : BudgetDatabase.BuilderProvider {
  override fun invoke(id: BudgetId): RoomDatabase.Builder<BudgetDatabase> {
    val path = files.database(id, mkdirs = true)
    return Room.databaseBuilder(context, path.toString())
  }
}
