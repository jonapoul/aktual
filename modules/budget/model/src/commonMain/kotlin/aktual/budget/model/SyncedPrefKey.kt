/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.model

/**
 * From packages/loot-core/src/types/prefs.d.ts, SyncedPrefs
 */
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

    /** [aktual.budget.model.UpcomingLengthOptions] */
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
    data class OfxFallbackMissingPayee(override val id: AccountId) : PerAccount("ofx-fallback-missing-payee")

    /** [Boolean] */
    data class ShowBalances(override val id: AccountId) : PerAccount("show-balances")

    /** [Boolean] */
    data class ShowExtraBalances(override val id: AccountId) : PerAccount("show-extra-balances")

    data class ParseDate(override val id: AccountId, val fileType: String) : PerAccount("parse-date") {
      override val key = "$keyPrefix-$id-$fileType"
    }

    data class FlipAmount(override val id: AccountId, val fileType: String) : PerAccount("flip-amount") {
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
        ?: fromId(key, "show-extra-balances", PerAccount::ShowExtraBalances)
        ?: fromIdAndType(key, "parse-date", PerAccount::ParseDate)
        ?: fromIdAndType(key, "flip-amount", PerAccount::FlipAmount)
        ?: Other(key)

    private fun fromId(key: String, prefix: String, factory: (AccountId) -> PerAccount): PerAccount? =
      if (key.startsWith(prefix)) {
        factory(key.removePrefix("$prefix-").let(::AccountId))
      } else {
        null
      }

    private fun fromIdAndType(key: String, prefix: String, factory: (AccountId, String) -> PerAccount): PerAccount? =
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
