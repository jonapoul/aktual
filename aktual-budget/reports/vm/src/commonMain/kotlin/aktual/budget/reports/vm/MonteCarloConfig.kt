package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import androidx.compose.runtime.Immutable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// The configuration types from
// packages/desktop-client/src/components/reports/reports/monte-carlo/monteCarloSimulation.ts.
// Amounts are in minor units, rates are decimal fractions (0.06 = 6%)

const val MC_MAX_AMOUNT = 100_000_000_000_000.0
const val MC_MIN_SIMULATION_COUNT = 1000
const val MC_MAX_SIMULATION_COUNT = 10000
const val MC_MAX_WITHDRAWAL_TAX_RATE = 0.75
const val MC_MAX_TAX_BAND_RATE = 0.99
const val MC_MAX_ANNUAL_FEE_RATE = 0.1
private const val MIN_HORIZON_YEARS = 1
private const val MAX_HORIZON_YEARS = 100
const val DEFAULT_INFLATION_MEAN = 0.025

data class AssetWeights(val stocks: Double, val bonds: Double, val cash: Double)

@Immutable data class ReturnStats(val mean: Double, val stdDev: Double)

// ALLOCATION_PRESETS: the mean and volatility each preset fills in
val AllocationPreset.presetStats: ReturnStats?
  get() =
    when (this) {
      Equity100 -> ReturnStats(mean = 0.07, stdDev = 0.15)
      Equity80 -> ReturnStats(mean = 0.065, stdDev = 0.12)
      Equity60 -> ReturnStats(mean = 0.06, stdDev = 0.1)
      Equity40 -> ReturnStats(mean = 0.05, stdDev = 0.075)
      Cash -> ReturnStats(mean = 0.03, stdDev = 0.015)
      CustomMix,
      Custom,
      Unknown -> null
    }

// PRESET_ASSET_WEIGHTS
val AllocationPreset.presetWeights: AssetWeights?
  get() =
    when (this) {
      Equity100 -> AssetWeights(stocks = 1.0, bonds = 0.0, cash = 0.0)
      Equity80 -> AssetWeights(stocks = 0.8, bonds = 0.2, cash = 0.0)
      Equity60 -> AssetWeights(stocks = 0.6, bonds = 0.4, cash = 0.0)
      Equity40 -> AssetWeights(stocks = 0.4, bonds = 0.6, cash = 0.0)
      Cash -> AssetWeights(stocks = 0.0, bonds = 0.0, cash = 1.0)
      CustomMix,
      Custom,
      Unknown -> null
    }

@Immutable
data class McPot(
  val id: String,
  val name: String = "",
  val startingBalance: Double = 50_000_000.0,
  val allocationPreset: AllocationPreset = Equity60,
  val allocationStocks: Double = 0.6,
  val allocationBonds: Double = 0.4,
  val allocationCash: Double = 0.0,
  val expectedReturnMean: Double = 0.06,
  val returnStdDev: Double = 0.1,
  // Age from which the pot can fund withdrawals; null means immediately
  val accessAge: Int? = null,
  // Account whose live balance supplies the starting balance
  val accountId: AccountId? = null,
  val withdrawalTaxRate: Double = 0.0,
  val taxableFraction: Double = 1.0,
  val annualFeeFixed: Double = 0.0,
  val feeAdjustsWithInflation: Boolean = false,
  val annualFeeRate: Double = 0.0,
  val isSurplus: Boolean = false,
)

// createMonteCarloSurplusPot(): empty cash, immediately accessible, untaxed, fee-free
fun surplusPot(id: String, name: String = "") =
  McPot(
    id = id,
    name = name,
    startingBalance = 0.0,
    allocationPreset = Cash,
    allocationStocks = 0.0,
    allocationBonds = 0.0,
    allocationCash = 1.0,
    expectedReturnMean = 0.03,
    returnStdDev = 0.015,
    taxableFraction = 0.0,
    isSurplus = true,
  )

@Immutable
data class McSpendingPhase(
  val id: String = "phase-1",
  val name: String = "",
  // Age the phase begins, inclusive; null means it starts immediately
  val fromAge: Int? = null,
  val annualWithdrawal: Double = 2_000_000.0,
)

@Immutable
data class McContribution(
  val id: String = "",
  val name: String = "",
  val potId: String,
  val fromAge: Int? = null,
  val toAge: Int? = null,
  val annualAmount: Double = 1_000_000.0,
  val adjustsWithInflation: Boolean = true,
  // Income stream the contribution is paid out of; null means from outside the plan
  val sourceIncomeStreamId: String? = null,
  // Whether an income-sourced contribution comes out of the stream's gross (salary sacrifice)
  val beforeTax: Boolean = false,
)

@Immutable
data class McIncomeStream(
  val id: String,
  val name: String = "",
  val fromAge: Int? = null,
  val toAge: Int? = null,
  val annualAmount: Double = 1_000_000.0,
  val adjustsWithInflation: Boolean = true,
  val taxRate: Double = 0.0,
  val taxableFraction: Double = 1.0,
)

@Immutable
data class McTaxBand(val id: String = "band-1", val from: Double = 0.0, val rate: Double = 0.0)

@Immutable
data class McWithdrawalRule(
  val type: WithdrawalRuleType = None,
  val prosperityTriggerPct: Double = 0.2,
  val prosperityIncreasePct: Double = 0.1,
  val preservationTriggerPct: Double = 0.2,
  val preservationCutPct: Double = 0.1,
  val balanceThresholdMultiple: Double = 1.5,
  val consecutiveYears: Int = 3,
  val ratchetIncreasePct: Double = 0.05,
  val floorPct: Double = 0.15,
  val ceilingPct: Double = 0.2,
  val upperRateThreshold: Double = 0.06,
  val upperCutPct: Double = 0.1,
  val lowerRateThreshold: Double = 0.04,
  val lowerIncreasePct: Double = 0.05,
)

@Immutable
data class McConfig(
  val pots: ImmutableList<McPot> = persistentListOf(surplusPot("surplus-pot"), McPot(id = "pot-1")),
  val withdrawalStrategy: WithdrawalStrategy = Proportional,
  val returnModel: ReturnModel = Normal,
  val withdrawalRule: McWithdrawalRule = McWithdrawalRule(),
  val minimumSpending: Double = 0.0,
  val spendingPhases: ImmutableList<McSpendingPhase> = persistentListOf(McSpendingPhase()),
  val contributions: ImmutableList<McContribution> = persistentListOf(),
  val incomeStreams: ImmutableList<McIncomeStream> = persistentListOf(),
  // null means flat withdrawals
  val inflationMean: Double? = DEFAULT_INFLATION_MEAN,
  val inflationStdDev: Double = 0.02,
  val taxModel: TaxModel = Flat,
  val taxBands: ImmutableList<McTaxBand> = persistentListOf(McTaxBand()),
  val currentAge: Int = 60,
  val targetAge: Int = 90,
  val simulationCount: Int = 5000,
) {
  val horizonYears: Int
    get() = (targetAge - currentAge).coerceIn(MIN_HORIZON_YEARS, MAX_HORIZON_YEARS)
}

// getPotAssetWeights(): null means the pot draws normally from its own mean and volatility
fun McPot.assetWeights(): AssetWeights? =
  if (allocationPreset == CustomMix) {
    val stocks = allocationStocks.sanitizeShare()
    val bonds = allocationBonds.sanitizeShare()
    val cash = allocationCash.sanitizeShare()
    val total = stocks + bonds + cash
    if (abs(total - 1) >= MIX_TOLERANCE) {
      null
    } else {
      AssetWeights(stocks / total, bonds / total, cash / total)
    }
  } else {
    allocationPreset.presetWeights
  }

// The sum of a custom mix's shares, which must be 100% for the mix to count
fun McPot.mixShareTotal(): Double =
  allocationStocks.sanitizeShare() +
    allocationBonds.sanitizeShare() +
    allocationCash.sanitizeShare()

fun McPot.isMixIncomplete(): Boolean =
  allocationPreset == CustomMix && abs(mixShareTotal() - 1) >= MIX_TOLERANCE

private fun Double.sanitizeShare() = if (isFinite() && this > 0) this else 0.0

private const val MIX_TOLERANCE = 1e-9

// resolveSpendingPhases(): sorted by starting age, with a default phase when none are set
fun List<McSpendingPhase>.resolve(): List<McSpendingPhase> = ifEmpty {
  [McSpendingPhase()]
}
  .sortedBy { it.fromAge ?: Int.MIN_VALUE }

// getActiveSpendingPhase(): the last phase that has started, or the earliest before any has
fun List<McSpendingPhase>.activeAt(age: Int): McSpendingPhase {
  var active = first()
  for (phase in this) {
    if (phase.fromAge == null || phase.fromAge <= age) active = phase else break
  }
  return active
}

// The ordinal used for "Pot N" labels, counting only the ordinary pots so the surplus pot doesn't
// shift the numbering
fun List<McPot>.ordinaryOrdinal(index: Int): Int = take(index + 1).count { !it.isSurplus }

// useResolvedMonteCarloConfig(): a linked pot takes its account's live balance, falling back to
// the stored balance
fun McConfig.withLiveBalances(balances: Map<AccountId, Long>): McConfig =
  copy(
    pots =
      pots
        .map { pot ->
          val balance = pot.accountId?.let(balances::get)
          if (balance == null) pot else pot.copy(startingBalance = max(0L, balance).toDouble())
        }
        .toImmutableList()
  )

// monteCarloConfigFromMeta()
fun MonteCarloReportMeta.toConfig(): McConfig {
  val defaults = McConfig()
  return McConfig(
    pots =
      pots?.takeIf { it.isNotEmpty() }?.mapIndexed { i, pot -> pot.toPot(i) }?.toImmutableList()
        ?: defaults.pots,
    withdrawalStrategy =
      withdrawalStrategy?.takeIf { it != Unknown } ?: defaults.withdrawalStrategy,
    returnModel = returnModel?.takeIf { it != Unknown } ?: defaults.returnModel,
    withdrawalRule = withdrawalRule?.toRule() ?: defaults.withdrawalRule,
    minimumSpending =
      (minimumSpending ?: minimumWithdrawal)?.toDouble() ?: defaults.minimumSpending,
    spendingPhases =
      spendingPhases
        ?.takeIf { it.isNotEmpty() }
        ?.mapIndexed { i, phase ->
          McSpendingPhase(
            id = phase.id.ifEmpty { "phase-${i + 1}" },
            name = phase.name.orEmpty(),
            fromAge = phase.fromAge,
            annualWithdrawal =
              phase.annualWithdrawal?.toDouble() ?: McSpendingPhase().annualWithdrawal,
          )
        }
        ?.toImmutableList() ?: defaults.spendingPhases,
    contributions =
      contributions.orEmpty().mapIndexed { i, c -> c.toContribution(i) }.toImmutableList(),
    incomeStreams =
      incomeStreams
        .orEmpty()
        .mapIndexed { i, stream -> stream.toIncomeStream(i) }
        .toImmutableList(),
    inflationMean = inflationMean,
    inflationStdDev = inflationStdDev ?: defaults.inflationStdDev,
    taxModel = taxModel?.takeIf { it != Unknown } ?: defaults.taxModel,
    taxBands =
      taxBands
        ?.takeIf { it.isNotEmpty() }
        ?.mapIndexed { i, band ->
          McTaxBand(
            id = band.id.ifEmpty { "band-${i + 1}" },
            from = band.from?.toDouble() ?: 0.0,
            rate = band.rate ?: 0.0,
          )
        }
        ?.toImmutableList() ?: defaults.taxBands,
    currentAge = currentAge ?: defaults.currentAge,
    targetAge = targetAge ?: defaults.targetAge,
    simulationCount = simulationCount ?: defaults.simulationCount,
  )
}

private fun MonteCarloPot.toPot(index: Int): McPot {
  val defaults = McPot(id = id.ifEmpty { "pot-${index + 1}" })
  return defaults.copy(
    name = name.orEmpty(),
    startingBalance = startingBalance?.toDouble() ?: defaults.startingBalance,
    allocationPreset = allocationPreset?.takeIf { it != Unknown } ?: defaults.allocationPreset,
    allocationStocks = allocationStocks ?: defaults.allocationStocks,
    allocationBonds = allocationBonds ?: defaults.allocationBonds,
    allocationCash = allocationCash ?: defaults.allocationCash,
    expectedReturnMean = expectedReturnMean ?: defaults.expectedReturnMean,
    returnStdDev = returnStdDev ?: defaults.returnStdDev,
    accessAge = accessAge,
    accountId = accountId,
    withdrawalTaxRate = withdrawalTaxRate ?: defaults.withdrawalTaxRate,
    taxableFraction = taxableFraction ?: defaults.taxableFraction,
    annualFeeFixed = annualFeeFixed?.toDouble() ?: defaults.annualFeeFixed,
    feeAdjustsWithInflation = feeAdjustsWithInflation ?: defaults.feeAdjustsWithInflation,
    annualFeeRate = annualFeeRate ?: defaults.annualFeeRate,
    isSurplus = isSurplus ?: defaults.isSurplus,
  )
}

private fun WithdrawalRule.toRule(): McWithdrawalRule {
  val defaults = McWithdrawalRule()
  return McWithdrawalRule(
    type = type.takeIf { it != Unknown } ?: defaults.type,
    prosperityTriggerPct = prosperityTriggerPct ?: defaults.prosperityTriggerPct,
    prosperityIncreasePct = prosperityIncreasePct ?: defaults.prosperityIncreasePct,
    preservationTriggerPct = preservationTriggerPct ?: defaults.preservationTriggerPct,
    preservationCutPct = preservationCutPct ?: defaults.preservationCutPct,
    balanceThresholdMultiple = balanceThresholdMultiple ?: defaults.balanceThresholdMultiple,
    consecutiveYears = consecutiveYears ?: defaults.consecutiveYears,
    ratchetIncreasePct = ratchetIncreasePct ?: defaults.ratchetIncreasePct,
    floorPct = floorPct ?: defaults.floorPct,
    ceilingPct = ceilingPct ?: defaults.ceilingPct,
    upperRateThreshold = upperRateThreshold ?: defaults.upperRateThreshold,
    upperCutPct = upperCutPct ?: defaults.upperCutPct,
    lowerRateThreshold = lowerRateThreshold ?: defaults.lowerRateThreshold,
    lowerIncreasePct = lowerIncreasePct ?: defaults.lowerIncreasePct,
  )
}

private fun Contribution.toContribution(index: Int): McContribution {
  val defaults =
    McContribution(id = id.ifEmpty { "contribution-${index + 1}" }, potId = potId.orEmpty())
  return defaults.copy(
    name = name.orEmpty(),
    fromAge = fromAge,
    toAge = toAge,
    annualAmount = annualAmount?.toDouble() ?: defaults.annualAmount,
    adjustsWithInflation = adjustsWithInflation ?: defaults.adjustsWithInflation,
    sourceIncomeStreamId = sourceIncomeStreamId,
    beforeTax = beforeTax ?: defaults.beforeTax,
  )
}

private fun IncomeStream.toIncomeStream(index: Int): McIncomeStream {
  val defaults = McIncomeStream(id = id.ifEmpty { "income-${index + 1}" })
  return defaults.copy(
    name = name.orEmpty(),
    fromAge = fromAge,
    toAge = toAge,
    annualAmount = annualAmount?.toDouble() ?: defaults.annualAmount,
    adjustsWithInflation = adjustsWithInflation ?: defaults.adjustsWithInflation,
    taxRate = taxRate ?: defaults.taxRate,
    taxableFraction = taxableFraction ?: defaults.taxableFraction,
  )
}

// Writes the whole config over the stored meta, like upstream's save, keeping fields the config
// doesn't own (the name)
fun McConfig.toMeta(base: MonteCarloReportMeta): MonteCarloReportMeta =
  base.copy(
    pots =
      pots.map { pot ->
        MonteCarloPot(
          id = pot.id,
          name = pot.name,
          startingBalance = pot.startingBalance.toMinor(),
          allocationPreset = pot.allocationPreset,
          allocationStocks = pot.allocationStocks,
          allocationBonds = pot.allocationBonds,
          allocationCash = pot.allocationCash,
          expectedReturnMean = pot.expectedReturnMean,
          returnStdDev = pot.returnStdDev,
          accessAge = pot.accessAge,
          accountId = pot.accountId,
          withdrawalTaxRate = pot.withdrawalTaxRate,
          taxableFraction = pot.taxableFraction,
          annualFeeFixed = pot.annualFeeFixed.toMinor(),
          feeAdjustsWithInflation = pot.feeAdjustsWithInflation,
          annualFeeRate = pot.annualFeeRate,
          isSurplus = pot.isSurplus,
        )
      },
    withdrawalStrategy = withdrawalStrategy,
    returnModel = returnModel,
    withdrawalRule =
      with(withdrawalRule) {
        WithdrawalRule(
          type = type,
          prosperityTriggerPct = prosperityTriggerPct,
          prosperityIncreasePct = prosperityIncreasePct,
          preservationTriggerPct = preservationTriggerPct,
          preservationCutPct = preservationCutPct,
          balanceThresholdMultiple = balanceThresholdMultiple,
          consecutiveYears = consecutiveYears,
          ratchetIncreasePct = ratchetIncreasePct,
          floorPct = floorPct,
          ceilingPct = ceilingPct,
          upperRateThreshold = upperRateThreshold,
          upperCutPct = upperCutPct,
          lowerRateThreshold = lowerRateThreshold,
          lowerIncreasePct = lowerIncreasePct,
        )
      },
    minimumSpending = minimumSpending.toMinor(),
    minimumWithdrawal = null,
    spendingPhases =
      spendingPhases.map { phase ->
        SpendingPhase(
          id = phase.id,
          name = phase.name,
          fromAge = phase.fromAge,
          annualWithdrawal = phase.annualWithdrawal.toMinor(),
        )
      },
    contributions =
      contributions.map { contribution ->
        Contribution(
          id = contribution.id,
          name = contribution.name,
          potId = contribution.potId,
          fromAge = contribution.fromAge,
          toAge = contribution.toAge,
          annualAmount = contribution.annualAmount.toMinor(),
          adjustsWithInflation = contribution.adjustsWithInflation,
          sourceIncomeStreamId = contribution.sourceIncomeStreamId,
          beforeTax = contribution.beforeTax,
        )
      },
    incomeStreams =
      incomeStreams.map { stream ->
        IncomeStream(
          id = stream.id,
          name = stream.name,
          fromAge = stream.fromAge,
          toAge = stream.toAge,
          annualAmount = stream.annualAmount.toMinor(),
          adjustsWithInflation = stream.adjustsWithInflation,
          taxRate = stream.taxRate,
          taxableFraction = stream.taxableFraction,
        )
      },
    inflationMean = inflationMean,
    inflationStdDev = inflationStdDev,
    taxModel = taxModel,
    taxBands = taxBands.map { TaxBand(id = it.id, from = it.from.toMinor(), rate = it.rate) },
    currentAge = currentAge,
    targetAge = targetAge,
    simulationCount = simulationCount,
  )

private fun Double.toMinor(): Long = max(0.0, this).roundToLong()
