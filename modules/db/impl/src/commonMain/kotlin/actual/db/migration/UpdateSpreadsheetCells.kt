package actual.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * https://github.com/actualbudget/actual/blob/master/packages/loot-core/migrations/1567699552727_budget.sql
 */
internal class UpdateSpreadsheetCells : Migration(CreateTransactionIndices.VERSION, VERSION) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      """
        DELETE FROM spreadsheet_cells WHERE
          name NOT LIKE '%!budget\_%' ESCAPE '\' AND
          name NOT LIKE '%!carryover\_%' ESCAPE '\' AND
          name NOT LIKE '%!buffered';

        UPDATE OR REPLACE spreadsheet_cells SET name = REPLACE(name, '_', '-');

        UPDATE OR REPLACE spreadsheet_cells SET
          name =
            SUBSTR(name, 1, 28) ||
            '-' ||
            SUBSTR(name, 29, 4) ||
            '-' ||
            SUBSTR(name, 33, 4) ||
            '-' ||
            SUBSTR(name, 37, 4) ||
            '-' ||
            SUBSTR(name, 41, 12)
        WHERE name LIKE '%!budget-%' AND LENGTH(name) = 52;

        UPDATE OR REPLACE spreadsheet_cells SET
          name =
            SUBSTR(name, 1, 31) ||
            '-' ||
            SUBSTR(name, 32, 4) ||
            '-' ||
            SUBSTR(name, 36, 4) ||
            '-' ||
            SUBSTR(name, 40, 4) ||
            '-' ||
            SUBSTR(name, 44, 12)
        WHERE name LIKE '%!carryover-%' AND LENGTH(name) = 55;

        UPDATE spreadsheet_cells SET expr = SUBSTR(expr, 2) WHERE name LIKE '%!carryover-%';
      """.trimIndent(),
    )
  }

  companion object {
    const val VERSION: Int = 1567699552
  }
}
