@file:OptIn(ExperimentalAtomicApi::class, ExperimentalForInheritanceCoroutinesApi::class)

package aktual.budget.prefs

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetScope
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadata.Key
import aktual.budget.model.writeMetadata
import alakazam.kotlin.core.CoroutineContexts
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

interface BudgetLocalPreferences : MutableStateFlow<DbMetadata>

operator fun <T : Any> BudgetLocalPreferences.get(key: Key<T>): T? = value[key]

operator fun <T : Any> BudgetLocalPreferences.set(key: Key<T>, value: T?): DbMetadata = this.value.set(key, value)

fun <T : Any> BudgetLocalPreferences.observe(key: Key<T>): Flow<T?> = map { metadata -> metadata[key] }

@BindingContainer
@ContributesTo(BudgetScope::class)
object BudgetLocalPreferencesContainer {
  @Provides
  @SingleIn(BudgetScope::class)
  internal fun prefs(
    impl: BudgetLocalPreferencesImpl,
  ): BudgetLocalPreferences = impl
}

internal class BudgetLocalPreferencesImpl(
  private val files: BudgetFiles,
  private val coroutineScope: CoroutineScope,
  private val contexts: CoroutineContexts,
  private val delegate: MutableStateFlow<DbMetadata>,
) : BudgetLocalPreferences, MutableStateFlow<DbMetadata> by delegate {
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
