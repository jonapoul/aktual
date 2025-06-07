package actual.budget.di

import actual.budget.model.BudgetFiles
import actual.budget.model.DbMetadata
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

  fun update(metadata: DbMetadata): BudgetComponent {
    val component = BudgetComponent.build(metadata, context, files)
    update { component }
    return component
  }
}
