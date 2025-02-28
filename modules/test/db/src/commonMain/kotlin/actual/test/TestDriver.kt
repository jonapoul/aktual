package actual.test

import actual.db.JvmSqlDriverFactory
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

fun inMemoryDriverFactory() = JvmSqlDriverFactory(JdbcSqliteDriver.IN_MEMORY)

fun fileDriverFactory(file: File) = JvmSqlDriverFactory(url = "jdbc:sqlite:${file.absolutePath}")
