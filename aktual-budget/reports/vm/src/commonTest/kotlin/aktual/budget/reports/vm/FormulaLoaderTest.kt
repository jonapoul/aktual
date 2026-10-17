package aktual.budget.reports.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.formula.ErrorValue
import aktual.budget.model.Amount
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.LocalDate

class FormulaLoaderTest {
  @Test
  fun `Number result is money`() = runLoaderTest { loader ->
    loader.formula(FormulaReportMeta(name = "Sum", formula = "=SUM(1, 2.5)")).test {
      assertThatNextEmissionIsEqualTo(data(output = FormulaOutput.Money(Amount(350)), name = "Sum"))
    }
  }

  @Test
  fun `Missing formula uses the default`() = runLoaderTest { loader ->
    loader.formula(FormulaReportMeta()).test {
      assertThatNextEmissionIsEqualTo(data(output = FormulaOutput.Money(Amount(600))))
    }
  }

  @Test
  fun `Text and boolean results are text`() = runLoaderTest { loader ->
    loader.formula(FormulaReportMeta(formula = """="a"&"b"""")).test {
      assertThatNextEmissionIsEqualTo(data(output = FormulaOutput.Text("ab")))
    }
    loader.formula(FormulaReportMeta(formula = "=1<2")).test {
      assertThatNextEmissionIsEqualTo(data(output = FormulaOutput.Text("true")))
    }
  }

  @Test
  fun `Errors are reported`() = runLoaderTest { loader ->
    loader.formula(FormulaReportMeta(formula = "=NOPE(1)")).test {
      assertThatNextEmissionIsEqualTo(
        data(output = FormulaOutput.Failure(ErrorValue(Name, UnknownFunction, "NOPE"))),
      )
    }
  }

  @Test
  fun `Color formula reads the result`() = runLoaderTest { loader ->
    val meta =
      FormulaReportMeta(
        formula = "=-5",
        colorFormula = """=IF(RESULT<0,"#ff0000","#00ff00")""",
      )
    loader.formula(meta).test {
      assertThatNextEmissionIsEqualTo(
        data(output = FormulaOutput.Money(Amount(-500)), color = "#ff0000"),
      )
    }
  }

  @Test
  fun `Color formula reads theme colours`() = runLoaderTest { loader ->
    val meta = FormulaReportMeta(formula = "=1", colorFormula = "=theme_errorText")
    loader.formula(meta).test {
      assertThatNextEmissionIsEqualTo(
        data(output = FormulaOutput.Money(Amount(100)), color = "${FORMULA_THEME_PREFIX}errorText"),
      )
    }
  }

  @Test
  fun `Invalid color formula gives no color`() = runLoaderTest { loader ->
    val meta = FormulaReportMeta(formula = "=1", colorFormula = "=NOPE()")
    loader.formula(meta).test {
      assertThatNextEmissionIsEqualTo(data(output = FormulaOutput.Money(Amount(100))))
    }
  }

  @Test
  fun `Font settings`() = runLoaderTest { loader ->
    val meta =
      FormulaReportMeta(
        formula = "=1",
        fontSizeMode = Static,
        staticFontSize = 20.0,
        showTitle = false,
      )
    loader.formula(meta).test {
      assertThatNextEmissionIsEqualTo(
        data(
          output = FormulaOutput.Money(Amount(100)),
          showTitle = false,
          fontSizeMode = Static,
          staticFontSize = 20.0,
        ),
      )
    }
  }

  private fun data(
    output: FormulaOutput,
    name: String? = null,
    color: String? = null,
    showTitle: Boolean = true,
    fontSizeMode: FontSizeMode = Dynamic,
    staticFontSize: Double = 32.0,
  ) =
    FormulaData(
      title = name,
      showTitle = showTitle,
      output = output,
      color = color,
      fontSizeMode = fontSizeMode,
      staticFontSize = staticFontSize,
      warnings = persistentListOf(),
    )

  private fun runLoaderTest(action: suspend BudgetDatabase.(ChartDataLoader) -> Unit) =
    runSyncedDatabaseTest { scope, _ ->
      val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
      val loader = chartDataLoader(contexts, calendar = { TODAY })
      this.action(loader)
    }

  private companion object {
    val TODAY = LocalDate(2026, 5, 15)
  }
}
