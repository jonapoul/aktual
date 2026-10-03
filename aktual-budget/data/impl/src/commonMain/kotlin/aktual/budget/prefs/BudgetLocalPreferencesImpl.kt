package aktual.budget.prefs

import aktual.budget.BudgetFiles
import aktual.budget.BudgetLocalPreferences
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.di.AppCoroutineScope
import aktual.di.BudgetScope
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class, binding<BudgetLocalPreferences>())
class BudgetLocalPreferencesImpl
private constructor(
  private val id: BudgetId,
  private val files: BudgetFiles,
  private val coroutineScope: AppCoroutineScope,
  private val contexts: CoroutineContexts,
  private val delegate: MutableStateFlow<DbMetadata>,
) : BudgetLocalPreferences, MutableStateFlow<DbMetadata> by delegate {
  private val writeMutex = Mutex()

  @Inject
  constructor(
    id: BudgetId,
    initial: DbMetadata,
    files: BudgetFiles,
    coroutineScope: AppCoroutineScope,
    contexts: CoroutineContexts,
  ) : this(id, files, coroutineScope, contexts, delegate = MutableStateFlow(initial))

  override fun compareAndSet(expect: DbMetadata, update: DbMetadata): Boolean {
    val updated = delegate.compareAndSet(expect, update)
    if (updated && expect != update) {
      coroutineScope.launch(contexts.io) {
        writeMutex.withLock {
          files.writeMetadata(id, update)
        }
      }
    }
    return updated
  }
}
