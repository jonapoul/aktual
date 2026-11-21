package aktual.budget.reports.vm

import aktual.test.FakeBudgetFeatureFlags
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isInstanceOf
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate

class ReportFeatureFlagsTest {
  @Test
  fun `Flagged reports load as disabled while their flag is off`() {
    val flags = FakeBudgetFeatureFlags()
    runLoaderTest(flags) { loader ->
      loader.load(SankeyReportMeta(name = "Flow")).test {
        assertThatNextEmissionIsEqualTo(
          UnsupportedData(FeatureDisabled, Sankey, name = "Flow"),
        )

        flags[SankeyReport].set(true)
        assertThat(awaitItem()).isInstanceOf<SankeyData>()
      }
    }
  }

  @Test
  fun `Unflagged reports ignore the flags`() =
    runLoaderTest(FakeBudgetFeatureFlags()) { loader ->
      loader.load(MarkdownReportMeta(content = "Hi")).test {
        assertThat(awaitItem()).isInstanceOf<TextData>()
        awaitComplete()
      }
    }

  private fun runLoaderTest(
    flags: FakeBudgetFeatureFlags,
    action: suspend (ChartDataLoader) -> Unit,
  ) = runSyncedDatabaseTest { scope, _ ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    action(chartDataLoader(contexts, calendar = { LocalDate(2026, 6, 15) }, flags = flags))
  }
}
