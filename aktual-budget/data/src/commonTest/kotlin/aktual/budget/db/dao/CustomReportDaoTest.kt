/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.db.dao

import aktual.budget.db.test.buildCustomReport
import aktual.budget.model.CustomReportId
import aktual.test.isEqualToList
import aktual.test.runDatabaseTest
import alakazam.test.core.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class CustomReportDaoTest {
  @Test
  fun `Fetching all IDs`() = runDaoTest { scope ->
    // given
    assertThat(getIds()).isEmpty()

    // when
    val a = buildCustomReport(id = CustomReportId("a"))
    val b = buildCustomReport(id = CustomReportId("b"))
    val c = buildCustomReport(id = CustomReportId("c"))
    insert(a)
    insert(b)
    insert(c)

    // then
    assertThat(getIds())
      .transform { it.map(CustomReportId::toString) }
      .isEqualToList("a", "b", "c")
  }

  @Test
  fun `Getting ID by name`() = runDaoTest { scope ->
    // given
    val name = "hello world"
    val id = CustomReportId("abc-123")
    assertThat(getIdByName(name)).isNull()

    // when
    insert(buildCustomReport(id = CustomReportId("def-456"), name = "something else"))
    insert(buildCustomReport(id = CustomReportId("xyz-789"), name = "i dunno lol"))

    // then still nothing
    assertThat(getIdByName(name)).isNull()

    // when
    insert(buildCustomReport(id = id, name = name))

    // then
    assertThat(getIdByName(name)).isEqualTo(id)
  }

  private fun runDaoTest(action: suspend CustomReportsDao.(TestScope) -> Unit) =
    runDatabaseTest { scope ->
      val dao = CustomReportsDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
      action(dao, scope)
    }
}
