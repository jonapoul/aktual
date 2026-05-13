package aktual.budget.db

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.di.AppScope
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.sqldelight.db.SqlDriver
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteConfiguration
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteDatabaseType
import com.eygraber.sqldelight.androidx.driver.AndroidxSqliteDriver
import com.eygraber.sqldelight.androidx.driver.File
import com.eygraber.sqldelight.androidx.driver.SqliteJournalMode
import dev.zacsweers.metro.ContributesBinding
import java.io.File
import logcat.logcat

fun interface SqlDriverFactory {
  fun create(budgetId: BudgetId): SqlDriver
}

@ContributesBinding(AppScope::class)
class AndroidxSqlDriverFactory(private val files: BudgetFiles) : SqlDriverFactory {
  override fun create(budgetId: BudgetId): SqlDriver {
    val dbFile = files.database(budgetId, mkdirs = true).toFile()
    val fileAlreadyExists = dbFile.exists()
    logcat.d(TAG) { "AndroidxSqlDriverFactory.create $dbFile fileAlreadyExists=$fileAlreadyExists" }

    return AndroidxSqliteDriver(
      driver = BundledSQLiteDriver(),
      databaseType = AndroidxSqliteDatabaseType.File(dbFile),
      schema = BudgetDatabase.Schema,
      configuration =
        AndroidxSqliteConfiguration(
          isForeignKeyConstraintsEnabled = true,
          journalMode = SqliteJournalMode.WAL,
        ),
      migrateEmptySchema = false,
      onConfigure = { logcat.d(TAG) { "onConfigure $dbFile" } },
      onCreate = { onCreate(dbFile, fileAlreadyExists) },
      onUpdate = { before, after -> logcat.d(TAG) { "onUpdate $dbFile $before $after" } },
      onOpen = { logcat.d(TAG) { "onOpen $dbFile" } },
    )
  }

  private suspend fun SqlDriver.onCreate(dbFile: File, fileAlreadyExists: Boolean) {
    logcat.d(TAG) { "onCreate $dbFile" }
    if (!fileAlreadyExists) {
      // Fresh database: schema already contains all columns/tables, so mark all migrations done
      logcat.d(TAG) { "Inserting migrations into fresh database file" }
      val values = DatabaseMigrations.joinToString { (version, _) -> "($version)" }
      val sql = "INSERT INTO __migrations__(id) VALUES $values"
      execute(identifier = null, sql = sql, parameters = 0).await()
    }
  }

  private companion object {
    const val TAG = "AndroidxSqlDriverFactory"
  }
}
