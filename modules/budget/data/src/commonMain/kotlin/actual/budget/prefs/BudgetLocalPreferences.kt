package actual.budget.prefs

import actual.budget.model.BudgetFiles
import actual.budget.model.DbMetadata
import actual.budget.model.writeMetadata
import alakazam.kotlin.core.CoroutineContexts
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class, ExperimentalForInheritanceCoroutinesApi::class)
class BudgetLocalPreferences(
  private val files: BudgetFiles,
  private val coroutineScope: CoroutineScope,
  private val contexts: CoroutineContexts,
  private val delegate: MutableStateFlow<DbMetadata>,
) : MutableStateFlow<DbMetadata> by delegate {
  @Inject
  constructor(
    initial: DbMetadata,
    files: BudgetFiles,
    coroutineScope: CoroutineScope,
    contexts: CoroutineContexts,
  ) : this(files, coroutineScope, contexts, delegate = MutableStateFlow(initial))

  private val isUpdating = AtomicBoolean(false)

  override fun compareAndSet(expect: DbMetadata, update: DbMetadata): Boolean {
    val updated = delegate.compareAndSet(expect, update)
    if (updated && expect != update) {
      coroutineScope.launch(contexts.io) {
        var locked = false
        while (!locked) {
          locked = isUpdating.compareAndSet(expectedValue = false, newValue = true)
        }
        files.writeMetadata(update)
        isUpdating.store(false)
      }
    }
    return updated
  }
}
