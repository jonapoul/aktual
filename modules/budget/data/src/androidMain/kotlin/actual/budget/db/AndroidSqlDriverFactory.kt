package actual.budget.db

import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.database
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import logcat.logcat

class AndroidSqlDriverFactory(
  private val id: BudgetId,
  private val context: Context,
  private val budgetFiles: BudgetFiles,
) : SqlDriverFactory {
  override fun create(): AndroidSqliteDriver {
    val synchronousSchema = BudgetDatabase.Schema.synchronous()
    return AndroidSqliteDriver(
      schema = synchronousSchema,
      context = context,
      name = budgetFiles.database(id, mkdirs = true).toString(),
      callback = AndroidCallback(synchronousSchema),
    )
  }
}

private class AndroidCallback(
  schema: SqlSchema<QueryResult.Value<Unit>>,
) : AndroidSqliteDriver.Callback(schema) {
  override fun onOpen(db: SupportSQLiteDatabase) {
    logcat.d { "onOpen ${db.path}" }
    super.onOpen(db)
    @Suppress("KotlinConstantConditions") // DatabaseBuildConfig is compile-time constant
    db.setForeignKeyConstraintsEnabled(DatabaseBuildConfig.FOREIGN_KEY_CONSTRAINTS)
  }

  override fun onConfigure(db: SupportSQLiteDatabase) {
    logcat.d { "onConfigure ${db.path}" }
    super.onConfigure(db)
  }

  override fun onCorruption(db: SupportSQLiteDatabase) {
    logcat.d { "onCorruption ${db.path}" }
    super.onCorruption(db)
  }

  override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
    logcat.d { "onDowngrade ${db.path} oldVersion=$oldVersion newVersion=$newVersion" }
    super.onDowngrade(db, oldVersion, newVersion)
  }

  override fun onCreate(db: SupportSQLiteDatabase) {
    logcat.d { "onCreate ${db.path}" }
    super.onCreate(db)
  }

  override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
    logcat.d { "onUpgrade ${db.path} oldVersion=$oldVersion newVersion=$newVersion" }
    super.onUpgrade(db, oldVersion, newVersion)
  }
}
