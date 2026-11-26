package aktual.budget.db

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.SORT_INCREMENT
import aktual.budget.model.Timestamp
import aktual.budget.utils.BudgetCreator
import aktual.budget.utils.BudgetFiles
import aktual.core.model.AktualJson
import aktual.core.utils.UuidGenerator
import aktual.core.utils.runCatchingCancellable
import aktual.di.AppScope
import aktual.log
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Like createBudget in packages/loot-core/src/server/budgetfiles/app.ts. Upstream copies a bundled
 * database and runs every migration on it, so this seeds the rows those leave behind.
 */
@Inject
@ContributesBinding(AppScope::class)
class BudgetCreatorImpl
internal constructor(
  private val driverFactory: SqlDriverFactory,
  private val files: BudgetFiles,
  private val uuids: UuidGenerator,
  private val contexts: CoroutineContexts,
) : BudgetCreator {
  override suspend fun invoke(id: BudgetId, name: String): DbMetadata? =
    runCatchingCancellable(
      attempt = { withContext(contexts.io) { create(id, name) } },
      onFailure = { e ->
        log.e(e) { "Failed creating budget $id" }
        files.fileSystem.deleteRecursively(files.directory(id))
        null
      },
    )

  private suspend fun create(id: BudgetId, name: String): DbMetadata {
    driverFactory.create(id).use { driver -> driver.seed() }
    // getDefaultPrefs in packages/loot-core/src/server/prefs.ts
    val metadata = DbMetadata(id = id.value, budgetName = name)
    files.writeMetadata(id, metadata)
    return metadata
  }

  private suspend fun SqlDriver.seed() {
    for (statement in STARTER_CATEGORIES) {
      execute(identifier = null, sql = statement, parameters = 0).await()
    }
    insertClock()
    insertDashboard()
  }

  // loadClock in packages/loot-core/src/server/db/index.ts, with makeClientId from
  // packages/crdt/src/crdt/timestamp.ts
  private suspend fun SqlDriver.insertClock() {
    val node = uuids().replace("-", "").takeLast(NODE_LENGTH)
    val clock = buildJsonObject {
      put("timestamp", Timestamp.fromMilliseconds(millis = 0, node = node).toString())
      put("merkle", JsonObject(emptyMap()))
    }
    execute(null, "INSERT INTO messages_clock (id, clock) VALUES (1, ?)", 1) {
        bindString(0, clock.toString())
      }
      .await()
  }

  // Migrations 1722804019000_create_dashboard_table and 1765518577215_multiple_dashboards
  private suspend fun SqlDriver.insertDashboard() {
    val pageId = uuids()
    execute(null, "INSERT INTO dashboard_pages (id, name, sort_order) VALUES (?, ?, ?)", 3) {
        bindString(0, pageId)
        bindString(1, DASHBOARD_PAGE_NAME)
        bindDouble(2, SORT_INCREMENT.toDouble())
      }
      .await()

    val widgets = AktualJson.parseToJsonElement(DEFAULT_DASHBOARD_WIDGETS).jsonArray
    for (widget in widgets.map { it.jsonObject }) {
      insertWidget(widget, pageId)
    }
  }

  @Suppress("MagicNumber")
  private suspend fun SqlDriver.insertWidget(widget: JsonObject, pageId: String) {
    val meta = widget["meta"]?.takeIf { it != JsonNull }
    execute(null, INSERT_WIDGET, parameters = 8) {
        bindString(0, uuids())
        bindString(1, widget.getValue("type").jsonPrimitive.content)
        bindLong(2, widget.long("width"))
        bindLong(3, widget.long("height"))
        bindLong(4, widget.long("x"))
        bindLong(5, widget.long("y"))
        bindString(6, meta?.toString())
        bindString(7, pageId)
      }
      .await()
  }

  private fun JsonObject.long(key: String): Long = getValue(key).jsonPrimitive.int.toLong()

  private companion object {
    const val NODE_LENGTH = 16
    const val DASHBOARD_PAGE_NAME = "Main"

    const val INSERT_WIDGET =
      "INSERT INTO dashboard (id, type, width, height, x, y, meta, dashboard_page_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"

    // The rows in upstream's packages/loot-core/default-db.sqlite, with the same IDs
    val STARTER_CATEGORIES =
      listOf(
        """
        INSERT INTO category_groups (id, name, is_income, sort_order, tombstone) VALUES
          ('fc3825fd-b982-4b72-b768-5b30844cf832', 'Usual Expenses', 0, 16384.0, 0),
          ('2E1F5BDB-209B-43F9-AF2C-3CE28E380C00', 'Income', 1, 32768.0, 0),
          ('a137772f-cf2f-4089-9432-822d2ddc1466', 'Investments and Savings', 0, 32768.0, 0)
        """
          .trimIndent(),
        """
        INSERT INTO categories (id, name, is_income, cat_group, sort_order, tombstone) VALUES
          ('541836f1-e756-4473-a5d0-6c1d3f06c7fa', 'Food', 0, 'fc3825fd-b982-4b72-b768-5b30844cf832', 16384.0, 0),
          ('af375fd4-d759-46b3-bffe-74a856151d57', 'General', 0, 'fc3825fd-b982-4b72-b768-5b30844cf832', 32768.0, 0),
          ('d4b0f075-3343-4408-91ed-fae94f74e5bf', 'Bills', 0, 'fc3825fd-b982-4b72-b768-5b30844cf832', 49152.0, 0),
          ('29ec2c58-8cd3-42fe-9187-7b5dfe0f70a6', 'Bills (Flexible)', 0, 'fc3825fd-b982-4b72-b768-5b30844cf832', 65536.0, 0),
          ('3c1699a5-522a-435e-86dc-93d900a14f0e', 'Income', 1, '2E1F5BDB-209B-43F9-AF2C-3CE28E380C00', 32768.0, 0),
          ('506e8d9d-7ed0-4397-84e4-07a9185dc6b2', 'Starting Balances', 1, '2E1F5BDB-209B-43F9-AF2C-3CE28E380C00', 32768.0, 0),
          ('6bbd8472-25d4-4cee-8a11-5bd9f7e83d61', 'Savings', 0, 'a137772f-cf2f-4089-9432-822d2ddc1466', 32768.0, 0)
        """
          .trimIndent(),
        """
        INSERT INTO category_mapping (id, transferId)
          SELECT id, id FROM categories
        """
          .trimIndent(),
      )
  }
}
