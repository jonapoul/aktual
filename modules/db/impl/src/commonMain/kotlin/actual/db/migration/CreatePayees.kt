package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1550601598648_payees.sql
 */
internal class CreatePayees : Migration(DropDbVersion.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        CREATE TABLE payees
          (id TEXT PRIMARY KEY NOT NULL,
           name TEXT,
           category TEXT,
           tombstone INTEGER DEFAULT 0,
           transfer_acct TEXT);

        CREATE TABLE payee_rules
          (id TEXT PRIMARY KEY NOT NULL,
           payee_id TEXT,
           type TEXT,
           value TEXT,
           tombstone INTEGER DEFAULT 0);

        CREATE INDEX payee_rules_lowercase_index ON payee_rules(LOWER(value));

        CREATE TABLE payee_mapping
          (id TEXT PRIMARY KEY NOT NULL,
           targetId TEXT);
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1550601598
  }
}
