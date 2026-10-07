package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.MC_MAX_TAX_BAND_RATE
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McSpendingPhase
import aktual.budget.reports.vm.McTaxBand
import aktual.budget.reports.vm.McWithdrawalRule
import aktual.budget.reports.vm.TaxModel
import aktual.budget.reports.vm.WithdrawalRuleType
import aktual.budget.reports.vm.WithdrawalStrategy
import aktual.budget.reports.vm.removeSpendingPhase
import aktual.budget.reports.vm.removeTaxBand
import aktual.budget.reports.vm.updateSpendingPhase
import aktual.budget.reports.vm.updateTaxBand
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.NormalTextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.persistentListOf

// The Spending tab from MonteCarloConfiguration.tsx
@Composable
internal fun SpendingTab(config: McConfig, onAction: MonteCarloActionHandler) {
  GroupHeadingWithHelp(Strings.monteCarloGroupPhases, Strings.monteCarloHelpPhases)
  config.spendingPhases.fastForEachIndexed { index, phase ->
    key(phase.id) { PhaseCard(config, phase, index, onAction) }
  }
  NormalTextButton(text = Strings.monteCarloPhaseAdd, onClick = { onAction(AddSpendingPhase) })

  LabeledField(
    label = Strings.monteCarloFieldWithdrawalOrder,
    help = Strings.monteCarloHelpWithdrawalOrder,
  ) {
    AktualExposedDropDownMenu(
      value = config.withdrawalStrategy,
      onValueChange = { strategy -> onAction.edit { it.copy(withdrawalStrategy = strategy) } },
      options = WITHDRAWAL_STRATEGIES,
      string = { it.string() },
    )
  }

  WithdrawalRuleFields(config, onAction)
}

// MonteCarloSpendingPhases.tsx
@Composable
private fun PhaseCard(
  config: McConfig,
  phase: McSpendingPhase,
  index: Int,
  onAction: MonteCarloActionHandler,
) {
  val label = phase.name.ifEmpty { Strings.monteCarloPhaseNumbered(index + 1) }
  val edit: ((McSpendingPhase) -> McSpendingPhase) -> Unit = { transform ->
    onAction.edit { it.updateSpendingPhase(phase.id, transform) }
  }

  ItemCard(
    title = label,
    actions = {
      if (config.spendingPhases.size > 1) {
        BareIconButton(
          imageVector = MaterialIcons.Delete,
          contentDescription = Strings.monteCarloPhaseRemove,
          onClick = { onAction.edit { it.removeSpendingPhase(phase.id) } },
        )
      }
    },
  ) {
    LabeledField(Strings.monteCarloFieldPhaseName) {
      NameField(
        name = phase.name,
        placeholder = label,
        onCommit = { name -> edit { it.copy(name = name) } },
      )
    }

    FieldRow {
      LabeledField(Strings.monteCarloFieldFromAge) {
        // The first phase always starts now
        if (index == 0) {
          ReadOnlyValue(Strings.monteCarloPhaseNow(config.currentAge))
        } else {
          IntField(
            value = phase.fromAge ?: config.currentAge + 1,
            min = config.currentAge + 1,
            max = config.targetAge,
            onCommit = { age -> edit { it.copy(fromAge = age) } },
          )
        }
      }
      LabeledField(Strings.monteCarloFieldUntil) {
        val nextFrom = config.spendingPhases.getOrNull(index + 1)?.fromAge
        ReadOnlyValue(
          if (nextFrom != null) {
            Strings.monteCarloAge(nextFrom - 1)
          } else {
            Strings.monteCarloPhaseOnwards
          },
        )
      }
      LabeledField(Strings.monteCarloFieldYearlySpending) {
        AmountField(
          amount = phase.annualWithdrawal,
          onCommit = { amount -> edit { it.copy(annualWithdrawal = amount) } },
        )
      }
    }
  }
}

// MonteCarloWithdrawalRuleConfiguration.tsx. Upstream sets the inputs inside each rule's sentence,
// which doesn't work on a narrow screen, so the sentence shows the values and the fields follow
@Composable
private fun WithdrawalRuleFields(config: McConfig, onAction: MonteCarloActionHandler) {
  val rule = config.withdrawalRule
  val edit: ((McWithdrawalRule) -> McWithdrawalRule) -> Unit = { transform ->
    onAction.edit { it.copy(withdrawalRule = transform(it.withdrawalRule)) }
  }

  FieldRow {
    LabeledField(
      label = Strings.monteCarloFieldWithdrawalRule,
      help = Strings.monteCarloHelpWithdrawalRule,
    ) {
      AktualExposedDropDownMenu(
        value = rule.type,
        onValueChange = { type -> edit { it.copy(type = type) } },
        options = WITHDRAWAL_RULES,
        string = { it.string() },
      )
    }

    if (rule.type != None) {
      LabeledField(
        label = Strings.monteCarloFieldMinimumSpending,
        help = Strings.monteCarloHelpMinimumSpending,
      ) {
        AmountField(
          amount = config.minimumSpending,
          onCommit = { amount -> onAction.edit { it.copy(minimumSpending = amount) } },
        )
      }
    }
  }

  when (rule.type) {
    Guardrails -> GuardrailsFields(rule, edit)
    Ratcheting -> RatchetingFields(rule, edit)
    FloorCeiling -> FloorCeilingFields(rule, edit)
    Boundaries -> BoundariesFields(rule, edit)
    None,
    Unknown -> Unit
  }

  if (rule.type != None) {
    Text(
      text =
        if (rule.type == Guardrails) {
          Strings.monteCarloRuleNoteGuardrails
        } else {
          Strings.monteCarloRuleNote
        },
      style = typography.bodySmall,
      color = colors.pageText,
    )
  }
}

@Composable
private fun GuardrailsFields(
  rule: McWithdrawalRule,
  edit: ((McWithdrawalRule) -> McWithdrawalRule) -> Unit,
) {
  BodyText(
    Strings.monteCarloRuleSentencePreservation(
      formatRuleRate(rule.preservationTriggerPct),
      formatRuleRate(rule.preservationCutPct),
    ),
  )
  FieldRow {
    RulePercentField(Strings.monteCarloRuleFieldPreservationTrigger, rule.preservationTriggerPct) {
      value ->
      edit { it.copy(preservationTriggerPct = value) }
    }
    RulePercentField(Strings.monteCarloRuleFieldPreservationCut, rule.preservationCutPct) { value ->
      edit { it.copy(preservationCutPct = value) }
    }
  }

  BodyText(
    Strings.monteCarloRuleSentenceProsperity(
      formatRuleRate(rule.prosperityTriggerPct),
      formatRuleRate(rule.prosperityIncreasePct),
    ),
  )
  FieldRow {
    RulePercentField(Strings.monteCarloRuleFieldProsperityTrigger, rule.prosperityTriggerPct) {
      value ->
      edit { it.copy(prosperityTriggerPct = value) }
    }
    RulePercentField(Strings.monteCarloRuleFieldProsperityRaise, rule.prosperityIncreasePct) { value
      ->
      edit { it.copy(prosperityIncreasePct = value) }
    }
  }
}

@Composable
private fun RatchetingFields(
  rule: McWithdrawalRule,
  edit: ((McWithdrawalRule) -> McWithdrawalRule) -> Unit,
) {
  BodyText(
    Strings.monteCarloRuleSentenceRatcheting(
      formatMultiple(rule.balanceThresholdMultiple),
      rule.consecutiveYears,
      formatRuleRate(rule.ratchetIncreasePct),
    ),
  )
  FieldRow {
    LabeledField(Strings.monteCarloRuleFieldBalanceMultiple) {
      NumberField(
        value = rule.balanceThresholdMultiple,
        min = MIN_BALANCE_MULTIPLE,
        max = MAX_BALANCE_MULTIPLE,
        onCommit = { value ->
          if (value != null) edit { it.copy(balanceThresholdMultiple = value) }
        },
      )
    }
    LabeledField(Strings.monteCarloRuleFieldYears) {
      IntField(
        value = rule.consecutiveYears,
        min = 1,
        max = MAX_CONSECUTIVE_YEARS,
        onCommit = { years -> edit { it.copy(consecutiveYears = years) } },
      )
    }
    RulePercentField(Strings.monteCarloRuleFieldRatchetRaise, rule.ratchetIncreasePct) { value ->
      edit { it.copy(ratchetIncreasePct = value) }
    }
  }
}

@Composable
private fun FloorCeilingFields(
  rule: McWithdrawalRule,
  edit: ((McWithdrawalRule) -> McWithdrawalRule) -> Unit,
) {
  BodyText(
    Strings.monteCarloRuleSentenceFloorCeiling(
      formatRuleRate(rule.ceilingPct),
      formatRuleRate(rule.floorPct),
    ),
  )
  FieldRow {
    RulePercentField(Strings.monteCarloRuleFieldCeiling, rule.ceilingPct, max = MAX_CEILING) { value
      ->
      edit { it.copy(ceilingPct = value) }
    }
    RulePercentField(Strings.monteCarloRuleFieldFloor, rule.floorPct) { value ->
      edit { it.copy(floorPct = value) }
    }
  }
}

@Composable
private fun BoundariesFields(
  rule: McWithdrawalRule,
  edit: ((McWithdrawalRule) -> McWithdrawalRule) -> Unit,
) {
  BodyText(
    Strings.monteCarloRuleSentenceUpper(
      formatRuleRate(rule.upperRateThreshold),
      formatRuleRate(rule.upperCutPct),
    ),
  )
  FieldRow {
    RulePercentField(Strings.monteCarloRuleFieldUpperThreshold, rule.upperRateThreshold) { value ->
      edit { it.copy(upperRateThreshold = value) }
    }
    RulePercentField(Strings.monteCarloRuleFieldUpperCut, rule.upperCutPct) { value ->
      edit { it.copy(upperCutPct = value) }
    }
  }

  BodyText(
    Strings.monteCarloRuleSentenceLower(
      formatRuleRate(rule.lowerRateThreshold),
      formatRuleRate(rule.lowerIncreasePct),
    ),
  )
  FieldRow {
    RulePercentField(Strings.monteCarloRuleFieldLowerThreshold, rule.lowerRateThreshold) { value ->
      edit { it.copy(lowerRateThreshold = value) }
    }
    RulePercentField(Strings.monteCarloRuleFieldLowerRaise, rule.lowerIncreasePct) { value ->
      edit { it.copy(lowerIncreasePct = value) }
    }
  }
}

@Composable
private fun RulePercentField(
  label: String,
  value: Double,
  max: Double = PERCENT_SCALE.toDouble(),
  onCommit: (Double) -> Unit,
) = LabeledField(label) { PercentField(value = value, max = max, onCommit = onCommit) }

// MonteCarloTaxConfiguration.tsx
@Composable
internal fun TaxTab(config: McConfig, onAction: MonteCarloActionHandler) {
  LabeledField(label = Strings.monteCarloFieldTaxModel, help = Strings.monteCarloHelpTaxModel) {
    AktualExposedDropDownMenu(
      value = config.taxModel,
      onValueChange = { model -> onAction.edit { it.copy(taxModel = model) } },
      options = TAX_MODELS,
      string = { it.string() },
    )
  }

  if (config.taxModel != Bands) {
    BodyText(Strings.monteCarloTaxFlatDesc)
    return
  }

  BodyText(Strings.monteCarloTaxBandsDesc)
  config.taxBands.fastForEachIndexed { index, band ->
    key(band.id) { TaxBandRow(config, band, index, onAction) }
  }
  NormalTextButton(text = Strings.monteCarloBandAdd, onClick = { onAction(AddTaxBand) })
}

@Composable
private fun TaxBandRow(
  config: McConfig,
  band: McTaxBand,
  index: Int,
  onAction: MonteCarloActionHandler,
) =
  Row(verticalAlignment = Bottom, horizontalArrangement = Arrangement.spacedBy(BAND_SPACING)) {
    LabeledField(Strings.monteCarloFieldBandFrom) {
      // The first band always starts at zero income
      if (index == 0) {
        ReadOnlyValue(Strings.monteCarloBandFirst)
      } else {
        AmountField(
          amount = band.from,
          onCommit = { from ->
            onAction.edit { it.updateTaxBand(band.id) { b -> b.copy(from = from) } }
          },
        )
      }
    }
    LabeledField(Strings.monteCarloFieldBandRate) {
      PercentField(
        value = band.rate,
        max = MC_MAX_TAX_BAND_RATE * PERCENT_SCALE,
        onCommit = { rate ->
          onAction.edit { it.updateTaxBand(band.id) { b -> b.copy(rate = rate) } }
        },
      )
    }
    if (config.taxBands.size > 1) {
      BareIconButton(
        imageVector = MaterialIcons.Delete,
        contentDescription = Strings.monteCarloBandRemove,
        onClick = { onAction.edit { it.removeTaxBand(band.id) } },
      )
    }
  }

private val WITHDRAWAL_STRATEGIES =
  persistentListOf<WithdrawalStrategy>(Proportional, Sequential, BestPerformer, TargetMix)

private val WITHDRAWAL_RULES =
  persistentListOf<WithdrawalRuleType>(None, Guardrails, Ratcheting, FloorCeiling, Boundaries)

private val TAX_MODELS = persistentListOf<TaxModel>(Flat, Bands)

private const val MIN_BALANCE_MULTIPLE = 1.0
private const val MAX_BALANCE_MULTIPLE = 10.0
private const val MAX_CONSECUTIVE_YEARS = 30
private const val MAX_CEILING = 200.0
private val BAND_SPACING = 16.dp
