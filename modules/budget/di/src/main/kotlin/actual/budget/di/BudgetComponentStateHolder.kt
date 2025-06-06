package actual.budget.di

import actual.budget.model.BudgetId
import actual.core.files.BudgetFiles
import alakazam.kotlin.core.StateHolder
import android.content.Context
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetComponentStateHolder @Inject constructor(
  private val context: Context,
  private val files: BudgetFiles,
) : StateHolder<BudgetComponent?>(initialState = null) {
  fun require(): BudgetComponent = value ?: error("No budget component loaded!")

  fun clear() = update { null }

  fun update(id: BudgetId): BudgetComponent {
    val component = BudgetComponent.build(id, context, files)
    update { component }
    return component
  }
}
