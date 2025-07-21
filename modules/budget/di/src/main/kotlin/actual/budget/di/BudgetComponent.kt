@file:Suppress("ktlint:standard:parameter-list-wrapping")

package actual.budget.di

import actual.budget.db.BudgetDatabase
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.BudgetScoped
import actual.budget.model.DbMetadata
import actual.budget.prefs.BudgetLocalPreferences
import alakazam.kotlin.core.CoroutineContexts
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope

@BudgetScoped
@Component(
  modules = [
    BudgetDatabaseModule::class,
  ],
)
interface BudgetComponent {
  val database: BudgetDatabase
  val localPreferences: BudgetLocalPreferences

  val budgetId: BudgetId get() = localPreferences.value.cloudFileId

  @Component.Builder
  interface Builder {
    fun with(@BindsInstance context: Context): Builder
    fun with(@BindsInstance files: BudgetFiles): Builder
    fun with(@BindsInstance metadata: DbMetadata): Builder
    fun with(@BindsInstance scope: CoroutineScope): Builder
    fun with(@BindsInstance contexts: CoroutineContexts): Builder
    fun build(): BudgetComponent
  }

  companion object {
    fun build(
      metadata: DbMetadata,
      context: Context,
      files: BudgetFiles,
      scope: CoroutineScope,
      contexts: CoroutineContexts,
    ): BudgetComponent = DaggerBudgetComponent
      .builder()
      .with(metadata)
      .with(context)
      .with(files)
      .with(scope)
      .with(contexts)
      .build()
  }
}

fun BudgetComponent.throwIfWrongBudget(expected: BudgetId) {
  require(budgetId == expected) {
    "Loading from the wrong budget! Expected $expected, got $budgetId"
  }
}
