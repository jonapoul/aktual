package aktual.budget.db

import aktual.budget.model.BudgetId
import aktual.core.model.CloseableStateHolder
import alakazam.kotlin.CoroutineContexts
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(AppScope::class)
class BudgetDatabaseStateHolder(
  private val provider: BudgetDatabase.BuilderProvider,
  private val contexts: CoroutineContexts,
) : CloseableStateHolder<BudgetDatabase?>(initialState = null, onClose = { it?.close() }) {
  fun update(id: BudgetId): BudgetDatabase {
    val database = provider(id).buildDatabase(contexts.io)
    update { database }
    return database
  }
}

fun RoomDatabase.Builder<BudgetDatabase>.buildDatabase(context: CoroutineContext): BudgetDatabase =
  setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(context)
    .addCallback(BudgetDatabaseCallback())
    .build()
