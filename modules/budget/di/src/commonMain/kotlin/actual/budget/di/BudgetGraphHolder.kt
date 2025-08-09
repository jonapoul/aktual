package actual.budget.di

import actual.budget.model.DbMetadata
import alakazam.kotlin.core.StateHolder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(AppScope::class)
class BudgetGraphHolder(
  private val budgetGraphBuilder: BudgetGraph.Builder,
) : StateHolder<BudgetGraph?>(initialState = null), AutoCloseable {
  fun require(): BudgetGraph = requireNotNull(value) { "No budget graph loaded!" }

  fun clear() = update { null }

  fun update(metadata: DbMetadata): BudgetGraph {
    val graph = budgetGraphBuilder(metadata)
    update { graph }
    return graph
  }

  override fun close() {
    value?.close()
  }

  override fun compareAndSet(expect: BudgetGraph?, update: BudgetGraph?): Boolean {
    val previous = value
    val successfullySet = super.compareAndSet(expect, update)
    if (successfullySet) previous?.close()
    return successfullySet
  }
}
