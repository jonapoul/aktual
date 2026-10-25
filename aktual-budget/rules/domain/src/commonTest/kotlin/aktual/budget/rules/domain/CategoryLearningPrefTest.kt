package aktual.budget.rules.domain

import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncedPrefKey.Global.LearnCategories
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher

class CategoryLearningPrefTest {
  @Test
  fun `Learning is on unless the pref says false`() = runDatabaseTest { scope ->
    // given
    val preferences =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val pref = CategoryLearningPref(preferences, TestSyncController())

    pref.observe().test {
      // then
      assertThatNextEmissionIsEqualTo(true)

      // when
      preferences[LearnCategories] = "false"

      // then
      assertThatNextEmissionIsEqualTo(false)

      // when
      preferences[LearnCategories] = "true"

      // then
      assertThatNextEmissionIsEqualTo(true)
    }
  }

  @Test
  fun `Setting the pref syncs it as a string`() = runDatabaseTest { scope ->
    // given
    val sync = TestSyncController()
    val preferences =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val pref = CategoryLearningPref(preferences, sync)

    // when
    pref.set(enabled = false)

    // then
    assertThat(sync.changes.map { Triple(it.row, it.column, it.value) })
      .containsExactly(Triple("learn-categories", "value", MessageValue.String("false")))
  }
}
