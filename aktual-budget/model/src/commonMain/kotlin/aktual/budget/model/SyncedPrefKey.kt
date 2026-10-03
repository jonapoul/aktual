package aktual.budget.model

/** From packages/loot-core/src/types/prefs.d.ts, SyncedPrefs */
sealed interface SyncedPrefKey {
  val key: String

  enum class Global(override val key: String) : SyncedPrefKey {
    /** [aktual.budget.model.BudgetType] */
    BudgetType("budgetType"),

    /** [aktual.budget.model.DateFormat] */
    DateFormat("dateFormat"),

    /** [aktual.budget.model.FirstDayOfWeek] */
    FirstDayOfWeekIdx("firstDayOfWeekIdx"),

    /** [Boolean] */
    HideFraction("hideFraction"),

    /** [Boolean] */
    IsPrivacyEnabled("isPrivacyEnabled"),

    /** [Boolean] */
    LearnCategories("learn-categories"),

    /** [aktual.budget.model.NumberFormat] */
    NumberFormat("numberFormat"),

    /** [aktual.budget.model.UpcomingLength] */
    UpcomingScheduledTransactionLength("upcomingScheduledTransactionLength"),
  }

  sealed class PerAccount(val keyPrefix: String) : SyncedPrefKey {
    abstract val id: AccountId
    override val key by lazy { "$keyPrefix-$id" }

    /** [Char] */
    data class CsvDelimiter(override val id: AccountId) : PerAccount("csv-delimiter")

    /** [Boolean] */
    data class CsvHasHeader(override val id: AccountId) : PerAccount("csv-has-header")

    /** [aktual.budget.model.CsvMappings] as JSON string */
    data class CsvMappings(override val id: AccountId) : PerAccount("csv-mappings")

    /** [Boolean] */
    data class HideCleared(override val id: AccountId) : PerAccount("hide-cleared")

    /** [Boolean] */
    data class OfxFallbackMissingPayee(override val id: AccountId) :
      PerAccount("ofx-fallback-missing-payee")

    /** [Boolean], true when unset */
    data class SyncImportNotes(override val id: AccountId) : PerAccount("sync-import-notes")

    /** [Boolean], true when unset */
    data class SyncImportPending(override val id: AccountId) : PerAccount("sync-import-pending")

    /** [Boolean], true when unset */
    data class SyncImportTransactions(override val id: AccountId) :
      PerAccount("sync-import-transactions")

    /** [Boolean], true when unset */
    data class SyncReimportDeleted(override val id: AccountId) : PerAccount("sync-reimport-deleted")

    /** [Boolean], false when unset */
    data class SyncUpdateDates(override val id: AccountId) : PerAccount("sync-update-dates")

    /**
     * Bank sync field mappings as JSON, see
     * packages/loot-core/src/server/util/custom-sync-mapping.ts
     */
    data class CustomSyncMappings(override val id: AccountId) : PerAccount("custom-sync-mappings")

    /** [Boolean] */
    data class ShowBalances(override val id: AccountId) : PerAccount("show-balances")

    /** [Boolean] */
    data class ShowExtraBalances(override val id: AccountId) : PerAccount("show-extra-balances")

    data class ParseDate(override val id: AccountId, val fileType: String) :
      PerAccount("parse-date") {
      override val key = "$keyPrefix-$id-$fileType"
    }

    data class FlipAmount(override val id: AccountId, val fileType: String) :
      PerAccount("flip-amount") {
      override val key = "$keyPrefix-$id-$fileType"
    }
  }

  data class Other(override val key: String) : SyncedPrefKey

  companion object {
    fun decode(key: String): SyncedPrefKey =
      Global.entries.firstOrNull { it.key == key }
        ?: fromId(key, "csv-delimiter", PerAccount::CsvDelimiter)
        ?: fromId(key, "csv-has-header", PerAccount::CsvHasHeader)
        ?: fromId(key, "csv-mappings", PerAccount::CsvMappings)
        ?: fromId(key, "hide-cleared", PerAccount::HideCleared)
        ?: fromId(key, "ofx-fallback-missing-payee", PerAccount::OfxFallbackMissingPayee)
        ?: fromId(key, "show-balances", PerAccount::ShowBalances)
        ?: fromId(key, "sync-import-notes", PerAccount::SyncImportNotes)
        ?: fromId(key, "sync-import-pending", PerAccount::SyncImportPending)
        ?: fromId(key, "sync-import-transactions", PerAccount::SyncImportTransactions)
        ?: fromId(key, "sync-reimport-deleted", PerAccount::SyncReimportDeleted)
        ?: fromId(key, "sync-update-dates", PerAccount::SyncUpdateDates)
        ?: fromId(key, "custom-sync-mappings", PerAccount::CustomSyncMappings)
        ?: fromId(key, "show-extra-balances", PerAccount::ShowExtraBalances)
        ?: fromIdAndType(key, "parse-date", PerAccount::ParseDate)
        ?: fromIdAndType(key, "flip-amount", PerAccount::FlipAmount)
        ?: Other(key)

    private fun fromId(
      key: String,
      prefix: String,
      factory: (AccountId) -> PerAccount,
    ): PerAccount? =
      if (key.startsWith(prefix)) {
        factory(AccountId(key.removePrefix("$prefix-")))
      } else {
        null
      }

    private fun fromIdAndType(
      key: String,
      prefix: String,
      factory: (AccountId, String) -> PerAccount,
    ): PerAccount? =
      if (key.startsWith(prefix)) {
        val withoutPrefix = key.removePrefix("$prefix-")
        val fileType = withoutPrefix.split("-").lastOrNull() ?: return null
        val accountId = withoutPrefix.removeSuffix("-$fileType")
        factory(AccountId(accountId), fileType)
      } else {
        null
      }
  }
}
