package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.DEFAULT_INFLATION_MEAN
import aktual.budget.reports.vm.MC_MAX_AGE
import aktual.budget.reports.vm.MC_MAX_SIMULATION_COUNT
import aktual.budget.reports.vm.MC_MIN_AGE
import aktual.budget.reports.vm.MC_MIN_SIMULATION_COUNT
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McContribution
import aktual.budget.reports.vm.McIncomeStream
import aktual.budget.reports.vm.McPot
import aktual.budget.reports.vm.McSpendingPhase
import aktual.budget.reports.vm.McTaxBand
import aktual.budget.reports.vm.McWithdrawalRule
import aktual.budget.reports.vm.ReturnModel
import aktual.budget.reports.vm.isHistorical
import aktual.budget.reports.vm.keepsSurplus
import aktual.budget.reports.vm.montecarlo.MonteCarloState
import aktual.budget.reports.vm.montecarlo.previewMonteCarloState
import aktual.budget.reports.vm.surplusPot
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryTextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.persistentListOf

// MonteCarloConfiguration.tsx
@Composable
internal fun MonteCarloConfiguration(
  state: MonteCarloState.Loaded,
  onAction: MonteCarloActionHandler,
  modifier: Modifier = Modifier,
  initialTab: ConfigTab = Plan,
) =
  SectionCard(modifier = modifier) {
    var tab by rememberSaveable { mutableStateOf(initialTab) }
    val focusManager = LocalFocusManager.current
    val config = state.config

    SectionTitle(Strings.monteCarloConfigTitle)

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(TAB_SPACING),
      verticalArrangement = Arrangement.spacedBy(TAB_SPACING),
    ) {
      ConfigTab.entries.fastForEach { entry ->
        val select = {
          // Commits the field being edited before it leaves the screen
          focusManager.clearFocus()
          tab = entry
        }
        if (entry == tab) {
          PrimaryTextButton(text = entry.string(), onClick = select)
        } else {
          NormalTextButton(text = entry.string(), onClick = select)
        }
      }
    }

    Row(
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(TAB_SPACING),
    ) {
      BodyText(modifier = Modifier.weight(1f, fill = false), text = tab.description())
      tab.help()?.let { HelpTooltip(it) }
    }

    when (tab) {
      Plan -> PlanTab(config, onAction)
      Pots -> PotsTab(config, state.accounts, onAction)
      Income -> IncomeTab(config, onAction)
      Contributions -> ContributionsTab(config, onAction)
      Spending -> SpendingTab(config, onAction)
      Tax -> TaxTab(config, onAction)
    }
  }

@Composable
private fun PlanTab(config: McConfig, onAction: MonteCarloActionHandler) {
  GroupHeading(Strings.monteCarloGroupPlan)
  FieldRow {
    LabeledField(Strings.monteCarloFieldCurrentAge) {
      IntField(
        value = config.currentAge,
        min = MC_MIN_AGE,
        max = MC_MAX_AGE - 1,
        onCommit = { age -> onAction.edit { it.copy(currentAge = age) } },
      )
    }
    LabeledField(Strings.monteCarloFieldTargetAge) {
      IntField(
        value = config.targetAge,
        min = config.currentAge + 1,
        max = MC_MAX_AGE,
        onCommit = { age -> onAction.edit { it.copy(targetAge = age) } },
      )
    }
  }

  GroupHeading(Strings.monteCarloGroupSimulation)
  FieldRow {
    LabeledField(Strings.monteCarloFieldReturnModel, help = Strings.monteCarloHelpReturnModel) {
      AktualExposedDropDownMenu(
        value = config.returnModel,
        onValueChange = { model -> onAction.edit { it.copy(returnModel = model) } },
        options = RETURN_MODELS,
        string = { it.string() },
      )
    }
    LabeledField(Strings.monteCarloFieldSimulations, help = Strings.monteCarloHelpSimulations) {
      IntField(
        value = config.simulationCount,
        min = MC_MIN_SIMULATION_COUNT,
        max = MC_MAX_SIMULATION_COUNT,
        // Sequence replay runs one scenario per historical start year
        isEnabled = config.returnModel != HistoricalSequence,
        onCommit = { count -> onAction.edit { it.copy(simulationCount = count) } },
      )
    }
  }

  GroupHeading(Strings.monteCarloGroupInflation)
  if (config.returnModel.isHistorical) {
    // Historical models take each sampled year's actual inflation, so the only real choice is
    // on or off
    LabeledCheckbox(
      label = Strings.monteCarloInflationAdjust,
      help = Strings.monteCarloHelpInflationAdjust,
      checked = config.inflationMean != null,
      onCheckedChange = { checked ->
        onAction.edit { it.copy(inflationMean = if (checked) DEFAULT_INFLATION_MEAN else null) }
      },
    )
  } else {
    FieldRow {
      LabeledField(
        label = Strings.monteCarloFieldInflationMean,
        help = Strings.monteCarloHelpInflationMean,
      ) {
        NumberField(
          value = config.inflationMean,
          min = 0.0,
          max = MAX_INFLATION_MEAN,
          scale = PERCENT_SCALE,
          allowEmpty = true,
          placeholder = Strings.monteCarloPlaceholderNone,
          onCommit = { mean -> onAction.edit { it.copy(inflationMean = mean) } },
        )
      }
      LabeledField(
        label = Strings.monteCarloFieldInflationStdDev,
        help = Strings.monteCarloHelpInflationStdDev,
      ) {
        PercentField(
          value = config.inflationStdDev,
          max = MAX_INFLATION_STD_DEV,
          isEnabled = config.inflationMean != null,
          onCommit = { stdDev -> onAction.edit { it.copy(inflationStdDev = stdDev) } },
        )
      }
    }
  }

  GroupHeading(Strings.monteCarloGroupSurplus)
  BodyText(Strings.monteCarloDescSurplus)
  LabeledCheckbox(
    label = Strings.monteCarloSurplusSpent,
    help = Strings.monteCarloHelpSurplus,
    checked = !config.keepsSurplus,
    onCheckedChange = { spent -> onAction(MonteCarloAction.SetKeepSurplus(!spent)) },
  )
}

private val RETURN_MODELS =
  persistentListOf<ReturnModel>(Normal, HistoricalBootstrap, HistoricalSequence)

private const val MAX_INFLATION_MEAN = 100.0
private const val MAX_INFLATION_STD_DEV = 50.0
private val TAB_SPACING = 6.dp

@PortraitPreview
@Composable
private fun PreviewMonteCarloConfiguration(
  @PreviewParameter(ConfigTabProvider::class) params: ColoredParams<ConfigTab>
) =
  PreviewWithColoredParams(params) {
    MonteCarloConfiguration(
      state = previewMonteCarloState(config = PREVIEW_CONFIG),
      onAction = {},
      initialTab = this,
    )
  }

private class ConfigTabProvider :
  ColoredParameterProvider<ConfigTab>(
    Plan,
    Pots,
    Income,
    Contributions,
    Spending,
    Tax,
  )

private val PREVIEW_CONFIG =
  McConfig(
    pots =
      persistentListOf(
        surplusPot("surplus"),
        McPot(id = "pot-1", name = "Pension", accessAge = 67),
        McPot(id = "pot-2", allocationPreset = CustomMix, allocationStocks = 0.5),
      ),
    incomeStreams = persistentListOf(McIncomeStream(id = "income-1", name = "State pension")),
    contributions =
      persistentListOf(
        McContribution(
          id = "contribution-1",
          potId = "pot-1",
          sourceIncomeStreamId = "income-1",
          annualAmount = 2_000_000.0,
        )
      ),
    spendingPhases =
      persistentListOf(McSpendingPhase(), McSpendingPhase(id = "phase-2", fromAge = 75)),
    withdrawalRule = McWithdrawalRule(type = Guardrails),
    taxModel = Bands,
    taxBands =
      persistentListOf(McTaxBand(), McTaxBand(id = "band-2", from = 1_257_000.0, rate = 0.2)),
    simulationCount = 1000,
  )
