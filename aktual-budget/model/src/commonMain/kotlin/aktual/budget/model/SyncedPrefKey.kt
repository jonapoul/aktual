package aktual.budget.model

/** From packages/loot-core/src/types/prefs.d.ts, SyncedPrefs */
sealed interface SyncedPrefKey {
  val key: String

  enum class Global(override val key: String) : SyncedPrefKey {
    /** [aktual.budget.model.BudgetType] */
    BudgetType("budgetType"),

    /** [aktual.budget.model.CurrencySymbolPosition] */
    CurrencySymbolPosition("currencySymbolPosition"),

    /** [Boolean] */
    CurrencySpaceBetweenAmountAndSymbol("currencySpaceBetweenAmountAndSymbol"),

    /** [aktual.budget.model.DateFormat] */
    DateFormat("dateFormat"),

    /** [aktual.budget.model.Currency] */
    DefaultCurrencyCode("defaultCurrencyCode"),

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

    /** [Boolean] */
    ShowHiddenTags("show-hidden-tags"),

    /** [Boolean], whether editing a transfer's date also moves the other side */
    SyncTransferDate("sync-transfer-date"),

    /** Transaction table columns as JSON, for views with none of their own */
    TransactionTableColumns(TRANSACTION_TABLE_COLUMNS_KEY),

    /** [aktual.budget.model.UpcomingLength] */
    UpcomingScheduledTransactionLength("upcomingScheduledTransactionLength"),
  }

  sealed class PerAccount(val keyPrefix: String) : SyncedPrefKey {
    abstract val id: AccountId
    override val key by lazy { "$keyPrefix-$id" }

    /** [Boolean] */
    data class HideCleared(override val id: AccountId) : PerAccount("hide-cleared")

    /** [Boolean] */
    data class HideReconciled(override val id: AccountId) : PerAccount("hide-reconciled")

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

    /**
     * Transaction table columns as JSON, overriding [Global.TransactionTableColumns]. Upstream also
     * keys it by views that aren't accounts, e.g. "all-accounts"
     */
    data class TransactionTableColumns(override val id: AccountId) :
      PerAccount(TRANSACTION_TABLE_COLUMNS_KEY)

    /** [Boolean], the running balance of each transaction. Superseded by the table columns */
    data class ShowBalances(override val id: AccountId) : PerAccount("show-balances")

    /** [Boolean], the account's cleared and uncleared totals */
    data class ShowExtraBalances(override val id: AccountId) : PerAccount("show-extra-balances")

    /**
     * Epoch millis the account was last opened, against transaction sort orders. Aktual's own, not
     * upstream's
     */
    data class NewTransactionsSeen(override val id: AccountId) :
      PerAccount(NEW_TRANSACTIONS_SEEN_PREFIX)
  }

  /** [Boolean] experimental feature toggles, false when unset. In upstream's settings order */
  enum class FeatureFlag(override val key: String) : SyncedPrefKey {
    GoalTemplates("flags.goalTemplatesEnabled"),
    GoalTemplatesUi("flags.goalTemplatesUIEnabled"),
    ActionTemplating("flags.actionTemplating"),
    FormulaMode("flags.formulaMode"),
    CurrencySupport("flags.currency"),
    MobileCalculator("flags.mobileCalculator"),
    NewSidebarUi("flags.newSidebarUI"),
    SankeyReport("flags.sankeyReport"),
    BalanceForecastReport("flags.balanceForecastReport"),
    BudgetAnalysisReport("flags.budgetAnalysisReport"),
    MonteCarloReport("flags.monteCarloReport"),
  }

  data class Other(override val key: String) : SyncedPrefKey

  companion object {
    // Also in Transactions.sq
    const val NEW_TRANSACTIONS_SEEN_PREFIX = "aktual-new-transactions-seen"

    private const val TRANSACTION_TABLE_COLUMNS_KEY = "transaction-table-columns"

    fun decode(key: String): SyncedPrefKey =
      Global.entries.firstOrNull { it.key == key }
        ?: FeatureFlag.entries.firstOrNull { it.key == key }
        ?: fromId(key, "hide-cleared", PerAccount::HideCleared)
        ?: fromId(key, "hide-reconciled", PerAccount::HideReconciled)
        ?: fromId(key, "show-balances", PerAccount::ShowBalances)
        ?: fromId(key, "sync-import-notes", PerAccount::SyncImportNotes)
        ?: fromId(key, "sync-import-pending", PerAccount::SyncImportPending)
        ?: fromId(key, "sync-import-transactions", PerAccount::SyncImportTransactions)
        ?: fromId(key, "sync-reimport-deleted", PerAccount::SyncReimportDeleted)
        ?: fromId(key, "sync-update-dates", PerAccount::SyncUpdateDates)
        ?: fromId(key, "custom-sync-mappings", PerAccount::CustomSyncMappings)
        ?: fromId(key, "show-extra-balances", PerAccount::ShowExtraBalances)
        ?: fromId(key, NEW_TRANSACTIONS_SEEN_PREFIX, PerAccount::NewTransactionsSeen)
        ?: fromId(key, TRANSACTION_TABLE_COLUMNS_KEY, PerAccount::TransactionTableColumns)
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
  }
}
