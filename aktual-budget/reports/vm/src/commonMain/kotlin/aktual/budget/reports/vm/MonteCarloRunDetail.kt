package aktual.budget.reports.vm

import kotlinx.collections.immutable.ImmutableList

// MonteCarloRuleExplanation: how the active withdrawal rule arrived at a captured year's amount
sealed interface McRuleExplanation {
  // Floor & ceiling's first spending year: the planned amount is taken as-is, and the rule's rate
  // (planned / accessible wealth) is set here
  data class Anchor(val rate: Double) : McRuleExplanation

  data class FloorCeiling(
    val rate: Double,
    // rate x accessible balance before clamping
    val unclamped: Long,
    val floor: Long,
    val ceiling: Long,
    val applied: FloorCeilingBound,
  ) : McRuleExplanation

  data class Factor(
    val rule: WithdrawalRuleType,
    // The running adjustment factor after this year's decision
    val factor: Double,
    val planned: Long,
    // planned x factor, before any minimum floor
    val adjusted: Long,
    val action: RuleAction,
    // Guardrails and boundaries only
    val currentRate: Double?,
    // Guardrails only
    val referenceRate: Double?,
    // Ratcheting only, when no raise happened: consecutive above-threshold years so far
    val ratchetStreak: Int?,
  ) : McRuleExplanation
}

enum class FloorCeilingBound {
  Floor,
  Ceiling,
  Rate,
}

enum class RuleAction {
  Cut,
  Raise,
  None,
}

// MonteCarloRunDetailRow: one simulated year of a single captured run. Per-pot lists follow the
// configured pot order, per-income and per-contribution lists their configured order
data class McRunDetailRow(
  val year: Int,
  // Total balance at the start of the year, before contributions and the withdrawal
  val startBalance: Long,
  // Gross amount withdrawn (the accessible remainder on failure)
  val withdrawal: Long,
  // Planned net spending after the rule's adjustment and the minimum floor, before affordability
  val plannedSpending: Long,
  // Net income put towards spending plus the withdrawal net of tax
  val spent: Long,
  val growth: Long,
  val endBalance: Long,
  val potBalances: ImmutableList<Long>,
  val potStartBalances: ImmutableList<Long>,
  // The year's realised inflation rate; null when inflation is disabled
  val inflation: Double?,
  val income: Long,
  val incomeAmounts: ImmutableList<Long>,
  val incomeTax: Long,
  // Net income left after sourced contributions and the year's spending
  val unspentIncome: Long,
  // Unspent income saved into the surplus pot
  val surplusSaved: Long,
  val contributions: Long,
  val potContributions: ImmutableList<Long>,
  val contributionAmounts: ImmutableList<Long>,
  val potWithdrawals: ImmutableList<Long>,
  val potTaxes: ImmutableList<Long>,
  val potTaxables: ImmutableList<Long>,
  val taxPaid: Long,
  val feesPaid: Long,
  val potFees: ImmutableList<Long>,
  // null when the pot had no balance or the plan failed
  val potReturns: ImmutableList<Double?>,
  val ruleExplanation: McRuleExplanation? = null,
  val minimumApplied: Boolean = false,
  // Money left in locked pots on a failure year
  val inaccessibleBalance: Long? = null,
  // A synthetic year after the plan ran out, charted by the cashflow view but not the table
  val afterDepletion: Boolean = false,
)
