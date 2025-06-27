package actual.budget.db.dao

import actual.budget.db.test.buildCustomReport
import actual.budget.model.CustomReportId
import actual.test.runDatabaseTest
import alakazam.test.core.TestCoroutineContexts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class CustomReportDaoTest {
  @Test
  fun `Fetching all IDs`() = runDaoTest { scope ->
    // given
    assertEquals(expected = emptyList(), actual = getIds())

    // when
    val a = buildCustomReport(id = CustomReportId("a"))
    val b = buildCustomReport(id = CustomReportId("b"))
    val c = buildCustomReport(id = CustomReportId("c"))
    insert(a)
    insert(b)
    insert(c)

    // then
    assertEquals(expected = listOf("a", "b", "c"), actual = getIds().map(CustomReportId::toString))
  }

  @Test
  fun `Getting ID by name`() = runDaoTest { scope ->
    // given
    val name = "hello world"
    val id = CustomReportId("abc-123")
    assertEquals(expected = null, actual = getIdByName(name))

    // when
    insert(buildCustomReport(id = CustomReportId("def-456"), name = "something else"))
    insert(buildCustomReport(id = CustomReportId("xyz-789"), name = "i dunno lol"))

    // then still nothing
    assertEquals(expected = null, actual = getIdByName(name))

    // when
    insert(buildCustomReport(id = id, name = name))

    // then
    assertEquals(expected = id, actual = getIdByName(name))
  }

  private fun runDaoTest(action: suspend CustomReportsDao.(TestScope) -> Unit) =
    runDatabaseTest { scope ->
      val dao = CustomReportsDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
      action(dao, scope)
    }
}
