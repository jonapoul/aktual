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
import kotlinx.coroutines.runBlocking
import logcat.logcat

fun interface SqlDriverFactory {
  fun create(budgetId: BudgetId): SqlDriver
}

@ContributesBinding(AppScope::class)
class AndroidxSqlDriverFactory(private val files: BudgetFiles) : SqlDriverFactory {
  override fun create(budgetId: BudgetId): SqlDriver {
    val dbFile = files.database(budgetId, mkdirs = true).toFile()
    logcat.d(TAG) { "AndroidxSqlDriverFactory.create $dbFile" }

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
      onCreate = { logcat.d(TAG) { "onCreate $dbFile" } },
      onUpdate = { before, after -> onUpdate(dbFile, before, after) },
      onOpen = { logcat.d(TAG) { "onOpen $dbFile" } },
    )
  }

  private fun SqlDriver.onUpdate(dbFile: File, before: Long, after: Long) {
    logcat.i(TAG) { "onUpdate $dbFile from $before to $after" }
    runBlocking {
      val query =
        execute(
          identifier = null,
          sql = "INSERT INTO __migrations__(id) VALUES (?)",
          parameters = 1,
          binders = { bindLong(index = 0, long = after) },
        )
      query.await()
    }
  }

  private companion object {
    const val TAG = "AndroidxSqlDriverFactory"
  }
}
