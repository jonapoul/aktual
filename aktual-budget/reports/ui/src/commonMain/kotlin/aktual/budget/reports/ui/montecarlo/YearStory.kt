package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.McRuleExplanation
import aktual.budget.reports.vm.McRunDetailRow
import aktual.budget.reports.vm.McWithdrawalRule
import aktual.budget.reports.vm.WithdrawalRuleType
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable

// Ported from
// packages/desktop-client/src/components/reports/reports/monte-carlo/monteCarloYearStory.ts.
// The plain-language summary of one simulated year, following cause to effect: what the withdrawal
// rule decided and why, whether the minimum spending floor stepped in, how the year's spending was
// funded, and where any unspent money ended up. The numbers behind each decision live in the
// working, not here
@Composable
internal fun yearStory(
  row: McRunDetailRow,
  rule: McWithdrawalRule,
  surplusPotName: String,
  hasSurplusPot: Boolean,
): List<String> = buildList {
  val ruleSentence = row.ruleExplanation?.let { ruleDecisionSentence(it, rule) }
  ruleSentence?.let(::add)

  if (row.minimumApplied) {
    val planned = row.plannedSpending.money()
    add(
      if (ruleSentence != null) {
        Strings.monteCarloStoryMinimumThen(planned)
      } else {
        Strings.monteCarloStoryMinimum(planned)
      }
    )
  }

  addAll(fundingSentences(row))

  if (hasSurplusPot && row.surplusSaved > 0) {
    add(Strings.monteCarloStorySurplusSaved(row.surplusSaved.money(), surplusPotName))
  } else if (row.unspentIncome > 0) {
    add(Strings.monteCarloStoryUnspent(row.unspentIncome.money()))
  }
}

// Factor explanations only come from guardrails, ratcheting and boundaries
@Composable
private fun ruleName(rule: WithdrawalRuleType): String =
  when (rule) {
    Ratcheting -> Strings.monteCarloRuleRatcheting
    Boundaries -> Strings.monteCarloRuleBoundaries
    Guardrails,
    None,
    FloorCeiling,
    Unknown -> Strings.monteCarloRuleGuardrails
  }

// What the rule decided, with the reason in words; null when the rule did nothing worth telling
@Composable
private fun ruleDecisionSentence(explanation: McRuleExplanation, rule: McWithdrawalRule): String? =
  when (explanation) {
    is Anchor -> {
      Strings.monteCarloStoryAnchor
    }

    is FloorCeiling -> {
      val unclamped = explanation.unclamped.money()
      when (explanation.applied) {
        Floor -> Strings.monteCarloStoryFloor(explanation.floor.money(), unclamped)
        Ceiling -> Strings.monteCarloStoryCeiling(explanation.ceiling.money(), unclamped)
        Rate -> null
      }
    }
    is Factor -> {
      factorSentence(explanation, rule)
    }
  }

@Composable
private fun factorSentence(explanation: McRuleExplanation.Factor, rule: McWithdrawalRule): String? {
  val name = ruleName(explanation.rule)
  val planned = explanation.planned.money()
  val adjusted = explanation.adjusted.money()
  return when (explanation.action) {
    Cut ->
      if (explanation.rule == Boundaries) {
        Strings.monteCarloStoryCutBoundaries(
          name,
          planned,
          adjusted,
          formatRuleRate(rule.upperRateThreshold),
        )
      } else {
        Strings.monteCarloStoryCut(name, planned, adjusted)
      }

    Raise ->
      when (explanation.rule) {
        Boundaries ->
          Strings.monteCarloStoryRaiseBoundaries(
            name,
            planned,
            adjusted,
            formatRuleRate(rule.lowerRateThreshold),
          )
        Ratcheting ->
          Strings.monteCarloStoryRaiseRatcheting(
            name,
            planned,
            adjusted,
            formatMultiple(rule.balanceThresholdMultiple),
            rule.consecutiveYears,
          )
        Guardrails,
        None,
        FloorCeiling,
        Unknown -> Strings.monteCarloStoryRaise(name, planned, adjusted)
      }

    None ->
      if (explanation.factor != 1.0) {
        Strings.monteCarloStoryEarlier(name, adjusted, planned)
      } else {
        null
      }
  }
}

// How the year's spending was paid for, or that it couldn't be
@Composable
private fun fundingSentences(row: McRunDetailRow): List<String> {
  val spent = row.spent.money()
  if (row.spent < row.plannedSpending) {
    return listOfNotNull(
      Strings.monteCarloStoryShortfall(spent, row.plannedSpending.money()),
      row.inaccessibleBalance?.let { Strings.monteCarloStoryLocked(it.money()) },
    )
  }
  if (row.income <= 0) return [Strings.monteCarloStoryFromPots(spent)]

  // What the pots put towards spending: the withdrawal net of tax
  val netFromPots = row.withdrawal - row.taxPaid
  if (netFromPots <= 0) return [Strings.monteCarloStoryIncomeAll(spent)]

  val fromIncome = row.spent - netFromPots
  // Net income that neither went to spending nor was left over was paid into pots by contributions
  // sourced from the income
  val netIncome = row.income - row.incomeTax
  val toContributions = netIncome - row.unspentIncome - fromIncome
  return [
    when {
      fromIncome <= 0 -> Strings.monteCarloStoryIncomeToContributions(netIncome.money(), spent)
      toContributions > 0 ->
        Strings.monteCarloStoryIncomePartialContributions(
          fromIncome.money(),
          spent,
          toContributions.money(),
        )
      else -> Strings.monteCarloStoryIncomePartial(fromIncome.money(), spent)
    }
  ]
}

// One sentence per rule outcome, phrased with the configured numbers so each year's working reads
// like the setup
@Composable
internal fun ruleWorking(explanation: McRuleExplanation, rule: McWithdrawalRule): String =
  when (explanation) {
    is Anchor -> {
      Strings.monteCarloWorkingAnchor(
        formatRuleRate(explanation.rate),
        formatRuleRate(rule.floorPct),
        formatRuleRate(rule.ceilingPct),
      )
    }

    is FloorCeiling -> {
      val rate = formatRuleRate(explanation.rate)
      val amount = explanation.unclamped.money()
      when (explanation.applied) {
        Floor ->
          Strings.monteCarloWorkingFloor(
            rate,
            amount,
            formatRuleRate(rule.floorPct),
            explanation.floor.money(),
          )
        Ceiling ->
          Strings.monteCarloWorkingCeiling(
            rate,
            amount,
            formatRuleRate(rule.ceilingPct),
            explanation.ceiling.money(),
          )
        Rate ->
          Strings.monteCarloWorkingWithin(
            rate,
            amount,
            formatRuleRate(rule.floorPct),
            explanation.floor.money(),
            formatRuleRate(rule.ceilingPct),
            explanation.ceiling.money(),
          )
      }
    }

    is Factor -> {
      when (explanation.rule) {
        Ratcheting -> ratchetingWorking(explanation, rule)
        Boundaries -> boundariesWorking(explanation, rule)
        Guardrails,
        None,
        FloorCeiling,
        Unknown -> guardrailsWorking(explanation, rule)
      }
    }
  }

@Composable
private fun guardrailsWorking(
  explanation: McRuleExplanation.Factor,
  rule: McWithdrawalRule,
): String {
  val current = formatRuleRate(explanation.currentRate ?: 0.0)
  val reference = formatRuleRate(explanation.referenceRate ?: 0.0)
  val planned = explanation.planned.money()
  val adjusted = explanation.adjusted.money()
  return when (explanation.action) {
    Cut ->
      Strings.monteCarloWorkingGuardrailsCut(
        current,
        formatRuleRate(rule.preservationTriggerPct),
        reference,
        formatRuleRate(rule.preservationCutPct),
        adjusted,
        planned,
      )
    Raise ->
      Strings.monteCarloWorkingGuardrailsRaise(
        current,
        formatRuleRate(rule.prosperityTriggerPct),
        reference,
        formatRuleRate(rule.prosperityIncreasePct),
        adjusted,
        planned,
      )
    None ->
      if (explanation.factor != 1.0) {
        Strings.monteCarloWorkingGuardrailsHoldEarlier(current, reference, adjusted, planned)
      } else {
        Strings.monteCarloWorkingGuardrailsHold(current, reference, planned)
      }
  }
}

@Composable
private fun ratchetingWorking(
  explanation: McRuleExplanation.Factor,
  rule: McWithdrawalRule,
): String {
  val multiple = formatMultiple(rule.balanceThresholdMultiple)
  val years = rule.consecutiveYears
  val planned = explanation.planned.money()
  val adjusted = explanation.adjusted.money()
  val hasEarlierAdjustments = explanation.factor != 1.0
  val streak = explanation.ratchetStreak ?: 0
  return when {
    explanation.action == Raise ->
      Strings.monteCarloWorkingRatchetRaise(
        multiple,
        years,
        formatRuleRate(rule.ratchetIncreasePct),
        adjusted,
        planned,
      )
    streak > 0 && hasEarlierAdjustments ->
      Strings.monteCarloWorkingRatchetStreakEarlier(multiple, streak, years, adjusted, planned)
    streak > 0 -> Strings.monteCarloWorkingRatchetStreak(multiple, streak, years, planned)
    hasEarlierAdjustments ->
      Strings.monteCarloWorkingRatchetResetEarlier(multiple, adjusted, planned)
    else -> Strings.monteCarloWorkingRatchetReset(multiple, planned)
  }
}

@Composable
private fun boundariesWorking(
  explanation: McRuleExplanation.Factor,
  rule: McWithdrawalRule,
): String {
  val current = formatRuleRate(explanation.currentRate ?: 0.0)
  val upper = formatRuleRate(rule.upperRateThreshold)
  val lower = formatRuleRate(rule.lowerRateThreshold)
  val planned = explanation.planned.money()
  val adjusted = explanation.adjusted.money()
  return when (explanation.action) {
    Cut ->
      Strings.monteCarloWorkingBoundariesCut(
        current,
        upper,
        formatRuleRate(rule.upperCutPct),
        adjusted,
        planned,
      )
    Raise ->
      Strings.monteCarloWorkingBoundariesRaise(
        current,
        lower,
        formatRuleRate(rule.lowerIncreasePct),
        adjusted,
        planned,
      )
    None ->
      if (explanation.factor != 1.0) {
        Strings.monteCarloWorkingBoundariesHoldEarlier(current, lower, upper, adjusted, planned)
      } else {
        Strings.monteCarloWorkingBoundariesHold(current, lower, upper, planned)
      }
  }
}
