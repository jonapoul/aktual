package actual.budget.di

import actual.budget.model.BudgetScope
import actual.budget.model.DbMetadata
import actual.budget.model.MutableDbMetadata
import actual.core.files.BudgetFiles
import actual.db.AndroidSqlDriverFactory
import actual.db.buildDatabase
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class BudgetDatabaseModule {
  @Provides
  @BudgetScope
  fun database(
    metadata: DbMetadata,
    context: Context,
    files: BudgetFiles,
  ) = buildDatabase(
    factory = AndroidSqlDriverFactory(metadata.cloudFileId, context, files),
  )

  @Provides
  @BudgetScope
  fun metadata(metadata: DbMetadata) = MutableDbMetadata(metadata)
}
