@file:OptIn(ExperimentalAtomicApi::class)

package actual.budget.model

import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.deepCopy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class BudgetLocalPreferences @Inject constructor(
  initial: DbMetadata,
  private val files: BudgetFiles,
  private val coroutineScope: CoroutineScope,
  private val contexts: CoroutineContexts,
) {
  private val metadata = DbMetadata(initial.deepCopy().toMutableMap())
  private val isUpdating = AtomicBoolean(false)

  val budgetId: BudgetId get() = metadata.cloudFileId

  fun modify(transaction: DbMetadata.() -> Unit) {
    var locked = false
    while (!locked) {
      locked = isUpdating.compareAndSet(expectedValue = false, newValue = true)
    }

    val previous = metadata.deepCopy()
    metadata.transaction()

    if (metadata == previous) {
      // no modification, no file write needed
      isUpdating.store(false)
      return
    }

    coroutineScope.launch(contexts.io) {
      files.writeMetadata(metadata)
      isUpdating.store(false)
    }
  }
}
