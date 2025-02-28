package actual.test

import actual.db.BudgetDatabase
import actual.db.SqlDriverFactory
import actual.db.buildDatabase
import app.cash.sqldelight.db.SqlDriver
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

class DatabaseRule(private val driverFactory: SqlDriverFactory) : TestWatcher() {
  private var nullableDriver: SqlDriver? = null
  private var nullableDatabase: BudgetDatabase? = null

  val driver: SqlDriver
    get() = nullableDriver ?: error("Driver is null!")

  val database: BudgetDatabase
    get() = nullableDatabase ?: error("Database is null!")

  override fun starting(description: Description) {
    super.starting(description)
    nullableDriver = driverFactory.create()
    nullableDatabase = buildDatabase(driver)
  }

  override fun finished(description: Description) {
    super.finished(description)
    nullableDriver?.close()
    nullableDriver = null
    nullableDatabase = null
  }

  companion object {
    fun inMemory() = DatabaseRule(inMemoryDriverFactory())

    fun fromFile(file: File) = DatabaseRule(fileDriverFactory(file))
  }
}
