/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.db.dao

import aktual.budget.model.AccountId
import aktual.budget.model.SyncedPrefKey
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.core.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PreferencesDaoTest {
  @Test
  fun `Getting from empty table returns null`() = runDaoTest {
    assertThat(get(key = SyncedPrefKey.Global.DateFormat)).isNull()
  }

  @Test
  fun `Observing by flow`() = runDaoTest { scope ->
    val key = SyncedPrefKey.Global.BudgetType
    observe(key).test {
      assertThatNextEmissionIsEqualTo(null)
      set(key, "value1")
      assertThatNextEmissionIsEqualTo("value1")
      set(key, "value2")
      assertThatNextEmissionIsEqualTo("value2")

      set(SyncedPrefKey.Other("some-other-key"), "whatever")
      scope.advanceUntilIdle()
      expectNoEvents()

      set(key, null)
      assertThatNextEmissionIsEqualTo(null)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Get all`() = runDaoTest {
    assertAllPreferences()

    val id = AccountId("abc-123")
    val key1 = SyncedPrefKey.Global.LearnCategories
    val key2 = SyncedPrefKey.PerAccount.CsvMappings(id)

    set(key1, "value1")
    assertAllPreferences(
      key1 to "value1",
    )

    set(key2, "value2")
    assertAllPreferences(
      key1 to "value1",
      key2 to "value2",
    )

    set(key2, "value2.1")
    assertAllPreferences(
      key1 to "value1",
      key2 to "value2.1",
    )
  }

  private fun runDaoTest(action: suspend PreferencesDao.(TestScope) -> Unit) =
    runDatabaseTest { scope ->
      val dao = PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
      action(dao, scope)
    }

  private suspend fun PreferencesDao.assertAllPreferences(vararg expected: Pair<SyncedPrefKey, String?>) =
    assertThat(getAll()).isEqualTo(expected.toMap())
}
