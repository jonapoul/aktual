@file:Suppress("ktlint:standard:parameter-list-wrapping")

package actual.budget.di

import actual.budget.db.BudgetDatabase
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.BudgetScope
import actual.budget.model.DbMetadata
import actual.budget.model.MutableDbMetadata
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
  val metadata: MutableDbMetadata

  val budgetId: BudgetId get() = metadata.value.cloudFileId

  @Component.Builder
  interface Builder {
    fun with(@BindsInstance context: Context): Builder
    fun with(@BindsInstance files: BudgetFiles): Builder
    fun with(@BindsInstance metadata: DbMetadata): Builder
    fun build(): BudgetComponent
  }

  companion object {
    fun build(
      metadata: DbMetadata,
      context: Context,
      files: BudgetFiles,
    ): BudgetComponent = DaggerBudgetComponent
      .builder()
      .with(metadata)
      .with(context)
      .with(files)
      .build()
  }
}
