package aktual.about.vm.logs

import aktual.about.data.LogsRepository
import aktual.about.vm.logs.LogEntriesState.Loaded
import aktual.core.logging.LogLine
import aktual.test.viewModelContext
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class LogEntriesViewModelTest {
  private val repository = mockk<LogsRepository>()

  private fun TestScope.buildViewModel(date: LocalDate): LogEntriesViewModel {
    every { repository.today() } returns TODAY
    return LogEntriesViewModel(
      date = date,
      initialFilters = Default,
      repository = repository,
      exporter = LogExporter(repository),
      contexts = TestCoroutineContexts(standardDispatcher),
      viewModelContext = viewModelContext,
    )
  }

  @Test
  fun `Verbose is hidden by default`() = runTest {
    every { repository.watch(YESTERDAY) } returns flowOf(listOf(VERBOSE_LINE, INFO_LINE))
    val viewModel = buildViewModel(YESTERDAY)

    viewModel.state.test {
      assertThat(lastLoaded()).prop(Loaded::lines).isEqualTo(persistentListOf(INFO_LINE))
    }
  }

  @Test
  fun `Filter by query and tag`() = runTest {
    every { repository.watch(YESTERDAY) } returns flowOf(listOf(INFO_LINE, ERROR_LINE))
    val viewModel = buildViewModel(YESTERDAY)

    viewModel.state.test {
      viewModel.setQuery("fail")
      assertThat(lastLoaded()).prop(Loaded::lines).isEqualTo(persistentListOf(ERROR_LINE))

      viewModel.setQuery("")
      viewModel.addTag("Main")
      assertThat(lastLoaded()).prop(Loaded::lines).isEqualTo(persistentListOf(INFO_LINE))
    }
  }

  @Test
  fun `Pausing today counts pending lines`() = runTest {
    val lines = MutableStateFlow(listOf(INFO_LINE))
    every { repository.watch(TODAY) } returns lines
    val viewModel = buildViewModel(TODAY)

    viewModel.state.test {
      assertThat(lastLoaded()).prop(Loaded::live).isEqualTo(Following)

      viewModel.pause()
      lines.value = listOf(INFO_LINE, ERROR_LINE)
      assertThat(lastLoaded()).all {
        prop(Loaded::live).isEqualTo(LiveState.Paused(pending = 1))
        prop(Loaded::lines).isEqualTo(persistentListOf(INFO_LINE))
      }

      viewModel.resume()
      assertThat(lastLoaded())
        .prop(Loaded::lines)
        .isEqualTo(persistentListOf(INFO_LINE, ERROR_LINE))
    }
  }

  @Test
  fun `Export shown lines`() = runTest {
    every { repository.watch(YESTERDAY) } returns flowOf(listOf(INFO_LINE, ERROR_LINE))
    coEvery { repository.dates() } returns emptyList()
    val viewModel = buildViewModel(YESTERDAY)

    viewModel.state.test {
      viewModel.setQuery("fail")
      lastLoaded()

      viewModel.exports.test {
        viewModel.export(Shown, Copy)
        assertThat(awaitItem())
          .isEqualTo(
            LogExport(
              fileName = "aktual-logs-2026-10-17.txt",
              text = "2026-10-17 12:00:00.000 / E / Api / Sync failed\n",
              target = Copy,
            ),
          )
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  context(scope: TestScope)
  private fun TurbineTestContext<LogEntriesState>.lastLoaded(): Loaded {
    scope.advanceUntilIdle()
    val item = expectMostRecentItem()
    assertThat(item).isInstanceOf<Loaded>()
    return item as Loaded
  }

  private companion object {
    val TODAY = LocalDate(2026, 10, 18)
    val YESTERDAY = LocalDate(2026, 10, 17)
    val TIME = LocalDateTime(2026, 10, 17, 12, 0, 0)
    val VERBOSE_LINE = LogLine(TIME, VERBOSE, "Main", "Noise")
    val INFO_LINE = LogLine(TIME, INFO, "Main", "App started")
    val ERROR_LINE = LogLine(TIME, ERROR, "Api", "Sync failed")
  }
}
