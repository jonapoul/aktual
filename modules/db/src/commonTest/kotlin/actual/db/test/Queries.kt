package actual.db.test

import actual.budget.model.AccountId
import actual.db.Accounts
import actual.db.BudgetDatabase
import actual.db.Preferences
import actual.db.withResult
import actual.db.withoutResult
import kotlin.test.assertContentEquals

internal suspend fun BudgetDatabase.getAccountById(id: AccountId): Accounts? =
  accountsQueries.withResult { getById(id).executeAsOneOrNull() }

internal suspend fun BudgetDatabase.insertAccount(account: Accounts) = accountsQueries.withoutResult {
  with(account) {
    insert(id, account_id, name, official_name, bank, offbudget, account_sync_source)
  }
}

internal suspend fun BudgetDatabase.getMetaValue(key: String): String? =
  metaQueries.withResult { getValue(key).executeAsOneOrNull()?.value_ }

internal suspend fun BudgetDatabase.insertMeta(key: String, value: String) =
  metaQueries.withoutResult { insert(key, value) }

internal suspend fun BudgetDatabase.assertAllPreferences(vararg expected: Preferences) =
  assertContentEquals(
    actual = preferencesQueries.withResult { getAll().executeAsList() },
    expected = expected.asList(),
  )

internal suspend fun BudgetDatabase.getPreference(key: String): String? =
  preferencesQueries.withResult { getValue(key).executeAsOneOrNull()?.value_ }

internal suspend fun BudgetDatabase.setPreference(key: String, value: String?) =
  preferencesQueries.withoutResult { setValue(key, value) }
