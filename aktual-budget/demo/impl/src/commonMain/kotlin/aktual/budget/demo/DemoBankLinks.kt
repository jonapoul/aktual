package aktual.budget.demo

import aktual.budget.model.AccountSyncSource
import aktual.budget.model.AccountSyncSource.Companion.GoCardless
import aktual.budget.model.AccountSyncSource.Companion.SimpleFin
import aktual.core.utils.UuidGenerator
import app.cash.sqldelight.db.SqlDriver

/**
 * Links a few demo accounts to made-up banks, so bank sync has something to sync through
 * [DemoBankSyncApi]. Two SimpleFIN accounts so syncing everything takes the batch path too.
 */
@Suppress("MagicNumber")
internal suspend fun SqlDriver.linkDemoBankAccounts(uuidGenerator: UuidGenerator) {
  DemoLinks.forEachIndexed { index, (account, bank, source) ->
    val bankId = uuidGenerator()
    execute(
        identifier = null,
        sql = "INSERT INTO banks (id, bank_id, name, tombstone) VALUES (?, ?, ?, 0)",
        parameters = 3,
      ) {
        bindString(0, bankId)
        bindString(1, bankId)
        bindString(2, bank)
      }
      .await()
    execute(
        identifier = null,
        sql =
          """
          UPDATE accounts SET bank = ?, account_id = ?, account_sync_source = ?
          WHERE name = ? AND tombstone = 0
          """
            .trimIndent(),
        parameters = 4,
      ) {
        bindString(0, bankId)
        bindString(1, "demo-account-$index")
        bindString(2, source.value)
        bindString(3, account)
      }
      .await()
  }
}

// banks.id is a UUID, so each bank gets a fixed one
private data class DemoLink(
  val account: String,
  val bank: String,
  val source: AccountSyncSource,
)

private val DemoLinks =
  listOf(
    DemoLink(account = "Bank of America", bank = "Bank of America", source = SimpleFin),
    DemoLink(account = "HSBC", bank = "HSBC", source = SimpleFin),
    DemoLink(account = "Deutsche Bank", bank = "Deutsche Bank", source = GoCardless),
  )
