@file:Suppress("ktlint:standard:parameter-list-wrapping", "DEPRECATION")

package actual.budget.di

import actual.budget.model.BudgetId
import actual.budget.model.BudgetScope
import actual.core.files.BudgetFiles
import actual.db.BudgetDatabase
import android.content.Context
import dagger.BindsInstance
import dagger.Component

@BudgetScope
@Component(
  modules = [
    BudgetDatabaseModule::class,
  ],
)
interface BudgetComponent {
  val database: BudgetDatabase

  @Deprecated("Don't use this directly", ReplaceWith("budgetId"))
  val idAlias: BudgetIdAlias

  val budgetId: BudgetId
    get() = BudgetId(idAlias)

  @Component.Builder
  interface Builder {
    fun with(@BindsInstance budgetId: BudgetIdAlias): Builder
    fun with(@BindsInstance context: Context): Builder
    fun with(@BindsInstance files: BudgetFiles): Builder
    fun build(): BudgetComponent
  }

  companion object {
    fun build(
      id: BudgetId,
      context: Context,
      files: BudgetFiles,
    ): BudgetComponent = DaggerBudgetComponent
      .builder()
      .with(id.value)
      .with(context)
      .with(files)
      .build()
  }
}
