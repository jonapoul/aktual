package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

// Ported from
// packages/desktop-client/src/components/reports/reports/monte-carlo/monteCarloSimulation.ts.
// Amounts are in minor units, rates are decimal fractions (0.06 = 6%). The per-run drill-in capture
// isn't ported, since only upstream's runs table uses it.

private const val MAX_AMOUNT = 100_000_000_000_000.0
private const val MAX_EMITTED = 1_125_899_906_842_624.0 // 2^50
private const val MIN_SIMULATION_COUNT = 1000
private const val MAX_SIMULATION_COUNT = 10000
private const val MIN_HORIZON_YEARS = 1
private const val MAX_HORIZON_YEARS = 100
private const val MAX_WITHDRAWAL_TAX_RATE = 0.75
private const val MAX_TAX_BAND_RATE = 0.99
private const val MAX_ANNUAL_FEE_RATE = 0.1
private const val MIN_INFLATION = -0.9
private const val MIX_TOLERANCE = 1e-9
private const val TAX_ITERATIONS = 40
private const val TAX_CONVERGENCE = 1e-7
internal const val DEFAULT_SIMULATION_SEED = 1234
internal const val DEFAULT_INFLATION_MEAN = 0.025

internal data class AssetWeights(val stocks: Double, val bonds: Double, val cash: Double)

private val PRESET_WEIGHTS =
  mapOf(
    AllocationPreset.Equity100 to AssetWeights(stocks = 1.0, bonds = 0.0, cash = 0.0),
    AllocationPreset.Equity80 to AssetWeights(stocks = 0.8, bonds = 0.2, cash = 0.0),
    AllocationPreset.Equity60 to AssetWeights(stocks = 0.6, bonds = 0.4, cash = 0.0),
    AllocationPreset.Equity40 to AssetWeights(stocks = 0.4, bonds = 0.6, cash = 0.0),
    AllocationPreset.Cash to AssetWeights(stocks = 0.0, bonds = 0.0, cash = 1.0),
  )

internal data class McPot(
  val id: String,
  val startingBalance: Double = 50_000_000.0,
  val allocationPreset: AllocationPreset = Equity60,
  val allocationStocks: Double = 0.6,
  val allocationBonds: Double = 0.4,
  val allocationCash: Double = 0.0,
  val expectedReturnMean: Double = 0.06,
  val returnStdDev: Double = 0.1,
  val accessAge: Int? = null,
  val accountId: AccountId? = null,
  val withdrawalTaxRate: Double = 0.0,
  val taxableFraction: Double = 1.0,
  val annualFeeFixed: Double = 0.0,
  val feeAdjustsWithInflation: Boolean = false,
  val annualFeeRate: Double = 0.0,
  val isSurplus: Boolean = false,
)

// The pot that keeps a plan's unspent money: empty cash, immediately accessible, untaxed, fee-free
private fun surplusPot(id: String) =
  McPot(
    id = id,
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

internal data class McSpendingPhase(
  val fromAge: Int? = null,
  val annualWithdrawal: Double = 2_000_000.0,
)

internal data class McContribution(
  val potId: String,
  val fromAge: Int? = null,
  val toAge: Int? = null,
  val annualAmount: Double = 1_000_000.0,
  val adjustsWithInflation: Boolean = true,
  val sourceIncomeStreamId: String? = null,
  val beforeTax: Boolean = false,
)

internal data class McIncomeStream(
  val id: String,
  val fromAge: Int? = null,
  val toAge: Int? = null,
  val annualAmount: Double = 1_000_000.0,
  val adjustsWithInflation: Boolean = true,
  val taxRate: Double = 0.0,
  val taxableFraction: Double = 1.0,
)

internal data class McTaxBand(val from: Double = 0.0, val rate: Double = 0.0)

internal data class McWithdrawalRule(
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

internal data class McConfig(
  val pots: List<McPot> = listOf(surplusPot("surplus-pot"), McPot(id = "pot-1")),
  val withdrawalStrategy: WithdrawalStrategy = Proportional,
  val returnModel: ReturnModel = Normal,
  val withdrawalRule: McWithdrawalRule = McWithdrawalRule(),
  val minimumSpending: Double = 0.0,
  val spendingPhases: List<McSpendingPhase> = listOf(McSpendingPhase()),
  val contributions: List<McContribution> = emptyList(),
  val incomeStreams: List<McIncomeStream> = emptyList(),
  val inflationMean: Double? = DEFAULT_INFLATION_MEAN,
  val inflationStdDev: Double = 0.02,
  val taxModel: TaxModel = Flat,
  val taxBands: List<McTaxBand> = listOf(McTaxBand()),
  val currentAge: Int = 60,
  val targetAge: Int = 90,
  val simulationCount: Int = 5000,
) {
  val horizonYears: Int
    get() = (targetAge - currentAge).coerceIn(MIN_HORIZON_YEARS, MAX_HORIZON_YEARS)
}

internal data class McPercentileBand(
  // 0 = starting point, 1..horizonYears = end of that year
  val year: Int,
  val p10: Long,
  val p25: Long,
  val p50: Long,
  val p75: Long,
  val p90: Long,
)

internal data class McResult(
  val successRate: Double,
  val percentileBands: List<McPercentileBand>,
  val medianEndingBalance: Long,
  val medianDepletionYear: Int?,
  val simulationCount: Int,
  val horizonYears: Int,
)

// monteCarloConfigFromMeta()
internal fun MonteCarloReportMeta.toConfig(): McConfig {
  val defaults = McConfig()
  return McConfig(
    pots = pots?.takeIf { it.isNotEmpty() }?.mapIndexed { i, pot -> pot.toPot(i) } ?: defaults.pots,
    withdrawalStrategy =
      withdrawalStrategy?.takeIf { it != Unknown } ?: defaults.withdrawalStrategy,
    returnModel = returnModel?.takeIf { it != Unknown } ?: defaults.returnModel,
    withdrawalRule = withdrawalRule?.toRule() ?: defaults.withdrawalRule,
    minimumSpending =
      (minimumSpending ?: minimumWithdrawal)?.toDouble() ?: defaults.minimumSpending,
    spendingPhases =
      spendingPhases
        ?.takeIf { it.isNotEmpty() }
        ?.map { phase ->
          McSpendingPhase(
            fromAge = phase.fromAge,
            annualWithdrawal =
              phase.annualWithdrawal?.toDouble() ?: McSpendingPhase().annualWithdrawal,
          )
        } ?: defaults.spendingPhases,
    contributions = contributions.orEmpty().map { it.toContribution() },
    incomeStreams = incomeStreams.orEmpty().mapIndexed { i, stream -> stream.toIncomeStream(i) },
    inflationMean = inflationMean,
    inflationStdDev = inflationStdDev ?: defaults.inflationStdDev,
    taxModel = taxModel?.takeIf { it != Unknown } ?: defaults.taxModel,
    taxBands =
      taxBands
        ?.takeIf { it.isNotEmpty() }
        ?.map { McTaxBand(from = it.from?.toDouble() ?: 0.0, rate = it.rate ?: 0.0) }
        ?: defaults.taxBands,
    currentAge = currentAge ?: defaults.currentAge,
    targetAge = targetAge ?: defaults.targetAge,
    simulationCount = simulationCount ?: defaults.simulationCount,
  )
}

private fun MonteCarloPot.toPot(index: Int): McPot {
  val defaults = McPot(id = id.ifEmpty { "pot-${index + 1}" })
  return defaults.copy(
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

private fun Contribution.toContribution(): McContribution {
  val defaults = McContribution(potId = potId.orEmpty())
  return defaults.copy(
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
    fromAge = fromAge,
    toAge = toAge,
    annualAmount = annualAmount?.toDouble() ?: defaults.annualAmount,
    adjustsWithInflation = adjustsWithInflation ?: defaults.adjustsWithInflation,
    taxRate = taxRate ?: defaults.taxRate,
    taxableFraction = taxableFraction ?: defaults.taxableFraction,
  )
}

// getPotAssetWeights(): null means the pot draws normally from its own mean and volatility
internal fun McPot.assetWeights(): AssetWeights? =
  when (allocationPreset) {
    Custom,
    Unknown -> {
      null
    }
    CustomMix -> {
      fun sanitize(share: Double) = if (share.isFinite() && share > 0) share else 0.0
      val stocks = sanitize(allocationStocks)
      val bonds = sanitize(allocationBonds)
      val cash = sanitize(allocationCash)
      val total = stocks + bonds + cash
      if (abs(total - 1) >= MIX_TOLERANCE) {
        null
      } else {
        AssetWeights(stocks / total, bonds / total, cash / total)
      }
    }
    Equity100,
    Equity80,
    Equity60,
    Equity40,
    Cash -> {
      PRESET_WEIGHTS.getValue(allocationPreset)
    }
  }

// mulberry32, so results match upstream's for the same seed
@Suppress("MagicNumber")
internal class Mulberry32(seed: Int) {
  private var state = seed

  fun next(): Double {
    state += 0x6d2b79f5
    var mixed = (state xor (state ushr 15)) * (1 or state)
    mixed = mixed + (mixed xor (mixed ushr 7)) * (61 or mixed) xor mixed
    return ((mixed xor (mixed ushr 14)).toLong() and 0xffffffffL) / 4_294_967_296.0
  }

  // Standard normal draw via the Box-Muller transform, shifting the first draw into (0, 1]
  fun nextNormal(): Double {
    val uniform1 = 1 - next()
    val uniform2 = next()
    return sqrt(-2 * ln(uniform1)) * cos(2 * PI * uniform2)
  }
}

private fun percentileOfSorted(sorted: DoubleArray, percentile: Double): Double {
  val position = (sorted.size - 1) * percentile
  val lower = floor(position).toInt()
  val upper = ceil(position).toInt()
  if (lower == upper) return sorted[lower]
  val weight = position - lower
  return sorted[lower] * (1 - weight) + sorted[upper] * weight
}

private fun roundJs(value: Double): Long = floor(value + HALF).toLong()

@Suppress("UseDataClass")
private class Schedule(val flatByYear: Array<DoubleArray>, val adjustedByYear: Array<DoubleArray>)

private data class YearFlows(
  val incomeTax: Double,
  val taxableIncomeBase: Double,
  val spendableIncome: Double,
)

// runMonteCarloSimulation(), always deflating to today's money like upstream's card
@Suppress(
  "CyclomaticComplexMethod",
  "LongMethod",
  "NestedBlockDepth",
  "LoopWithTooManyJumpStatements",
)
internal fun runMonteCarlo(
  config: McConfig,
  history: List<HistoricalReturn> = HISTORICAL_RETURNS,
  seed: Int = DEFAULT_SIMULATION_SEED,
): McResult {
  // The surplus pot's semantics are fixed, whatever the stored meta says
  val pots =
    config.pots.ifEmpty { McConfig().pots }.map { if (it.isSurplus) surplusPot(it.id) else it }
  val potCount = pots.size
  val potStartBalances =
    DoubleArray(potCount) { pots[it].startingBalance.coerceIn(0.0, MAX_AMOUNT) }
  val potMeans = DoubleArray(potCount) { pots[it].expectedReturnMean }
  val potStdDevs = DoubleArray(potCount) { max(0.0, pots[it].returnStdDev) }
  val strategy = config.withdrawalStrategy
  val previousReturns = DoubleArray(potCount)
  val drainOrder = MutableList(potCount) { it }
  val potIdealBalances = DoubleArray(potCount)
  val potTakes = DoubleArray(potCount)
  val potBalances = DoubleArray(potCount)

  // First simulation year (1-based) in which each pot can fund withdrawals
  val potAccessFromYear =
    IntArray(potCount) { i ->
      pots[i].accessAge?.let { max(1, it - config.currentAge + 1) } ?: 1
    }

  // Tax
  val taxModel = config.taxModel
  val potTaxRates =
    DoubleArray(potCount) { pots[it].withdrawalTaxRate.coerceIn(0.0, MAX_WITHDRAWAL_TAX_RATE) }
  val potTaxableFractions = DoubleArray(potCount) { pots[it].taxableFraction.coerceIn(0.0, 1.0) }
  val taxBands =
    config.taxBands
      .map {
        McTaxBand(it.from.coerceIn(0.0, MAX_AMOUNT), it.rate.coerceIn(0.0, MAX_TAX_BAND_RATE))
      }
      .sortedBy { it.from }
      .toMutableList()
  if (taxBands.isEmpty() || taxBands[0].from > 0) taxBands.add(0, McTaxBand(0.0, 0.0))
  val isBands = taxModel == Bands
  val hasTax =
    if (isBands) {
      taxBands.any { it.rate > 0 } && potTaxableFractions.any { it > 0 }
    } else {
      potTaxRates.any { it > 0 }
    }

  val incomeStreams = config.incomeStreams
  val incomeCount = incomeStreams.size
  val incomeTaxRates =
    DoubleArray(incomeCount) { incomeStreams[it].taxRate.coerceIn(0.0, MAX_WITHDRAWAL_TAX_RATE) }
  val incomeTaxableFractions =
    DoubleArray(incomeCount) { incomeStreams[it].taxableFraction.coerceIn(0.0, 1.0) }
  val hasIncomeTax =
    if (isBands) {
      taxBands.any { it.rate > 0 } && incomeTaxableFractions.any { it > 0 }
    } else {
      incomeTaxRates.any { it > 0 }
    }

  // Fees
  val potFeeFixed = DoubleArray(potCount) { pots[it].annualFeeFixed.coerceIn(0.0, MAX_AMOUNT) }
  val potFeeRates =
    DoubleArray(potCount) { pots[it].annualFeeRate.coerceIn(0.0, MAX_ANNUAL_FEE_RATE) }
  val hasFees = potFeeFixed.any { it > 0 } || potFeeRates.any { it > 0 }

  // Worst-case share of a gross withdrawal lost to tax, to skip the shortfall precheck
  val maxTaxRate =
    if (isBands) max(0.0, taxBands.maxOf { it.rate }) else max(0.0, potTaxRates.maxOrNull() ?: 0.0)
  val minNetFactor = 1 - maxTaxRate

  fun bandTax(taxableIncome: Double): Double {
    var tax = 0.0
    for (i in taxBands.indices) {
      if (taxableIncome <= taxBands[i].from) break
      val upper = if (i + 1 < taxBands.size) taxBands[i + 1].from else POSITIVE_INFINITY
      tax += taxBands[i].rate * (min(taxableIncome, upper) - taxBands[i].from)
    }
    return tax
  }

  // Band thresholds are in today's money, so taxable income is deflated before banding. The year's
  // income has already filled the lower bands, so the takes are taxed as the slice above it
  fun taxForTakes(cumulativeInflation: Double, taxableIncomeBase: Double): Double {
    if (!hasTax) return 0.0
    if (!isBands) {
      var tax = 0.0
      for (i in 0 until potCount) tax += potTakes[i] * potTaxRates[i]
      return tax
    }
    var taxable = 0.0
    for (i in 0 until potCount) taxable += potTakes[i] * potTaxableFractions[i]
    val baseToday = taxableIncomeBase / cumulativeInflation
    return (bandTax(baseToday + taxable / cumulativeInflation) - bandTax(baseToday)) *
      cumulativeInflation
  }

  val surplusPotIndex = pots.indexOfFirst { it.isSurplus }

  fun canDrain(potIndex: Int, year: Int) =
    year >= potAccessFromYear[potIndex] && potIndex != surplusPotIndex

  fun splitAcrossPots(grossTotal: Double, year: Int, accessibleTotal: Double) {
    when (strategy) {
      Sequential,
      BestPerformer -> {
        val isBestPerformer = strategy == BestPerformer
        if (isBestPerformer) {
          for (i in 0 until potCount) drainOrder[i] = i
          // Stable, so ties fall back to the listed order
          drainOrder.sortByDescending { previousReturns[it] }
        }
        var remaining = grossTotal
        var orderIndex = 0
        while (orderIndex < potCount && remaining > 0) {
          val potIndex = if (isBestPerformer) drainOrder[orderIndex] else orderIndex
          orderIndex++
          if (!canDrain(potIndex, year)) continue
          val take = min(potBalances[potIndex], remaining)
          potTakes[potIndex] = take
          remaining -= take
        }
      }

      TargetMix -> {
        // Move the accessible pots back toward their share of the starting balances, taking from
        // the most overweight pots first
        var targetAccessible = 0.0
        for (i in 0 until potCount) if (canDrain(i, year)) targetAccessible += potStartBalances[i]
        val remainingTotal = accessibleTotal - grossTotal
        for (i in 0 until potCount) {
          drainOrder[i] = i
          potIdealBalances[i] =
            if (canDrain(i, year) && targetAccessible > 0) {
              potStartBalances[i] / targetAccessible * remainingTotal
            } else {
              0.0
            }
        }
        drainOrder.sortByDescending { potBalances[it] - potIdealBalances[it] }
        var remaining = grossTotal
        var orderIndex = 0
        while (orderIndex < potCount && remaining > 0) {
          val potIndex = drainOrder[orderIndex]
          orderIndex++
          if (!canDrain(potIndex, year)) continue
          val excess = potBalances[potIndex] - potIdealBalances[potIndex]
          if (excess <= 0) continue
          val take = minOf(remaining, potBalances[potIndex], excess)
          potTakes[potIndex] = take
          remaining -= take
        }
        // Float-drift safety net
        var potIndex = 0
        while (potIndex < potCount && remaining > 0) {
          if (canDrain(potIndex, year)) {
            val take = min(potBalances[potIndex] - potTakes[potIndex], remaining)
            potTakes[potIndex] += take
            remaining -= take
          }
          potIndex++
        }
      }

      Proportional,
      Unknown -> {
        // The last drainable pot takes the remainder, so there's no float drift
        var lastDrainableIndex = -1
        for (i in 0 until potCount) if (canDrain(i, year)) lastDrainableIndex = i
        var remaining = grossTotal
        for (i in 0 until max(0, lastDrainableIndex)) {
          if (!canDrain(i, year)) continue
          val take = grossTotal * (potBalances[i] / accessibleTotal)
          potTakes[i] = take
          remaining -= take
        }
        if (lastDrainableIndex >= 0) potTakes[lastDrainableIndex] = remaining
      }
    }
  }

  // The surplus pot goes first, then the strategy splits the rest
  fun computeTakes(grossTotal: Double, year: Int, accessibleTotal: Double) {
    potTakes.fill(0.0)
    if (grossTotal <= 0 || accessibleTotal <= 0) return
    var remainingGross = grossTotal
    var remainingAccessible = accessibleTotal
    if (surplusPotIndex != -1 && year >= potAccessFromYear[surplusPotIndex]) {
      val take = min(potBalances[surplusPotIndex], remainingGross)
      potTakes[surplusPotIndex] = take
      remainingGross -= take
      remainingAccessible -= potBalances[surplusPotIndex]
    }
    if (remainingGross > 0 && remainingAccessible > 0) {
      splitAcrossPots(remainingGross, year, remainingAccessible)
    }
  }

  val returnModel = config.returnModel
  val historyCount = history.size

  // Pots with an asset mix take that mix's blended return in each historical year
  val potHistoricalReturns: List<DoubleArray?> = pots.map { pot ->
    if (returnModel == Normal || returnModel == Unknown) {
      return@map null
    }
    val weights = pot.assetWeights() ?: return@map null
    DoubleArray(historyCount) { i ->
      weights.stocks * history[i].stocks +
        weights.bonds * history[i].bonds +
        weights.cash * history[i].cash
    }
  }
  val hasNormalDrawPot = potHistoricalReturns.any { it == null }

  val inflationMean = config.inflationMean
  val inflationStdDev = if (inflationMean != null) max(0.0, config.inflationStdDev) else 0.0
  val horizonYears = config.horizonYears

  // The planned spending path in today's money
  val phases =
    config.spendingPhases
      .ifEmpty { listOf(McSpendingPhase()) }
      .sortedBy {
        it.fromAge ?: Int.MIN_VALUE
      }
  val plannedTodayByYear = DoubleArray(horizonYears + 1)
  for (year in 1..horizonYears) {
    val age = config.currentAge + year - 1
    var active = phases[0]
    for (phase in phases) {
      if (phase.fromAge == null || phase.fromAge <= age) active = phase else break
    }
    plannedTodayByYear[year] = active.annualWithdrawal.coerceIn(0.0, MAX_AMOUNT)
  }

  // Scheduled amounts per year in today's money, split so a year's amount is
  // flat + adjusted * cumulativeInflation
  fun scheduleByYear(
    count: Int,
    item: (Int) -> Triple<Int?, Int?, Pair<Double, Boolean>>,
  ): Schedule {
    val flat = Array(horizonYears + 1) { DoubleArray(count) }
    val adjusted = Array(horizonYears + 1) { DoubleArray(count) }
    for (i in 0 until count) {
      val (fromAge, toAge, amountAndAdjusts) = item(i)
      val (rawAmount, adjusts) = amountAndAdjusts
      val amount = rawAmount.coerceIn(0.0, MAX_AMOUNT)
      for (year in 1..horizonYears) {
        val age = config.currentAge + year - 1
        if (fromAge != null && age < fromAge) continue
        if (toAge != null && age > toAge) continue
        (if (adjusts) adjusted else flat)[year][i] = amount
      }
    }
    return Schedule(flat, adjusted)
  }

  // Contributions into unknown pots or out of unknown income streams are ignored
  val contributions = config.contributions
  val contributionCount = contributions.size
  val contributionPotIndex =
    IntArray(contributionCount) { i ->
      pots.indexOfFirst { it.id == contributions[i].potId }
    }
  val contributionSourceIndex =
    IntArray(contributionCount) { i ->
      contributions[i].sourceIncomeStreamId?.let { id ->
        incomeStreams.indexOfFirst { it.id == id }
      } ?: -1
    }
  val contributionSchedule =
    scheduleByYear(contributionCount) { i ->
      val c = contributions[i]
      val ignored =
        contributionPotIndex[i] == -1 ||
          c.sourceIncomeStreamId != null && contributionSourceIndex[i] == -1
      Triple(c.fromAge, c.toAge, (if (ignored) 0.0 else c.annualAmount) to c.adjustsWithInflation)
    }
  val hasContributions =
    (0..horizonYears).any { year ->
      contributionSchedule.flatByYear[year].any { it > 0 } ||
        contributionSchedule.adjustedByYear[year].any { it > 0 }
    }
  val incomeSchedule =
    scheduleByYear(incomeCount) { i ->
      val s = incomeStreams[i]
      Triple(s.fromAge, s.toAge, s.annualAmount to s.adjustsWithInflation)
    }

  val yearContributionAmounts = DoubleArray(contributionCount)
  val yearIncomeRemaining = DoubleArray(incomeCount)

  // Settles a year's income and contributions: before-tax contributions come out of their stream's
  // gross, each stream is taxed, after-tax contributions come out of the net, and the rest is
  // spendable. Contributions from outside the plan are paid in full
  fun settleYearFlows(year: Int, cumulativeInflation: Double): YearFlows {
    val flatIncome = incomeSchedule.flatByYear[year]
    val adjustedIncome = incomeSchedule.adjustedByYear[year]
    for (i in 0 until incomeCount) {
      yearIncomeRemaining[i] = flatIncome[i] + adjustedIncome[i] * cumulativeInflation
    }

    val flatContributions = contributionSchedule.flatByYear[year]
    val adjustedContributions = contributionSchedule.adjustedByYear[year]
    yearContributionAmounts.fill(0.0)
    for (i in 0 until contributionCount) {
      val scheduled = flatContributions[i] + adjustedContributions[i] * cumulativeInflation
      val source = contributionSourceIndex[i]
      if (source == -1) {
        yearContributionAmounts[i] = scheduled
      } else if (contributions[i].beforeTax) {
        val amount = min(scheduled, yearIncomeRemaining[source])
        yearIncomeRemaining[source] -= amount
        yearContributionAmounts[i] = amount
      }
    }

    var incomeTax = 0.0
    var taxableIncomeBase = 0.0
    if (hasIncomeTax && !isBands) {
      for (i in 0 until incomeCount) {
        val tax = yearIncomeRemaining[i] * incomeTaxRates[i]
        yearIncomeRemaining[i] -= tax
        incomeTax += tax
      }
    } else if (isBands) {
      for (i in 0 until incomeCount) {
        taxableIncomeBase += yearIncomeRemaining[i] * incomeTaxableFractions[i]
      }
      if (hasIncomeTax && taxableIncomeBase > 0) {
        incomeTax = bandTax(taxableIncomeBase / cumulativeInflation) * cumulativeInflation
        for (i in 0 until incomeCount) {
          yearIncomeRemaining[i] -=
            incomeTax * (yearIncomeRemaining[i] * incomeTaxableFractions[i] / taxableIncomeBase)
        }
      }
    }

    for (i in 0 until contributionCount) {
      val source = contributionSourceIndex[i]
      if (source == -1 || contributions[i].beforeTax) continue
      val scheduled = flatContributions[i] + adjustedContributions[i] * cumulativeInflation
      val amount = min(scheduled, yearIncomeRemaining[source])
      yearIncomeRemaining[source] -= amount
      yearContributionAmounts[i] = amount
    }

    return YearFlows(incomeTax, taxableIncomeBase, yearIncomeRemaining.sum())
  }

  // The part of each year's planned spending the pots have to fund, in today's money. Withdrawal
  // rules anchor and measure against this path
  val potFundedPlannedTodayByYear = DoubleArray(horizonYears + 1)
  for (year in 1..horizonYears) {
    val spendable = settleYearFlows(year, 1.0).spendableIncome
    potFundedPlannedTodayByYear[year] = max(0.0, plannedTodayByYear[year] - spendable)
  }
  val firstSpendingYear =
    (1..horizonYears).firstOrNull { potFundedPlannedTodayByYear[it] > 0 } ?: Int.MAX_VALUE

  val deflate = inflationMean != null
  // Sequence replay runs one scenario per historical start year
  val simulationCount =
    if (returnModel == HistoricalSequence) {
      historyCount
    } else {
      config.simulationCount.coerceIn(MIN_SIMULATION_COUNT, MAX_SIMULATION_COUNT)
    }

  val random = Mulberry32(seed)
  val balancesByYear = Array(horizonYears + 1) { DoubleArray(simulationCount) }
  val depletionCounts = IntArray(horizonYears + 1)
  var survivedCount = 0
  val startingTotal = potStartBalances.sum()
  val rule = config.withdrawalRule
  val minimumSpending = config.minimumSpending.coerceIn(0.0, MAX_AMOUNT)

  fun toSafeAmount(value: Double) = value.coerceIn(-MAX_EMITTED, MAX_EMITTED)

  // Starting wealth that's accessible in each year, so locked pots don't drive rule decisions
  val accessibleStartByYear = DoubleArray(horizonYears + 1)
  for (year in 1..horizonYears) {
    var accessibleStart = 0.0
    for (i in 0 until potCount) {
      if (year >= potAccessFromYear[i]) accessibleStart += potStartBalances[i]
    }
    accessibleStartByYear[year] = accessibleStart
  }

  for (simulationIndex in 0 until simulationCount) {
    potStartBalances.copyInto(potBalances)
    previousReturns.fill(0.0)
    var total = startingTotal
    var adjustmentFactor = 1.0
    var floorCeilingAnchorRate = 0.0
    var cumulativeInflation = 1.0
    var ratchetStreak = 0
    var depleted = false

    balancesByYear[0][simulationIndex] = toSafeAmount(total)

    for (year in 1..horizonYears) {
      // Post-depletion years stay at zero
      if (depleted) continue

      // Income arrives and contributions land at the start of the year, before the withdrawal
      val flows = settleYearFlows(year, cumulativeInflation)
      if (hasContributions) {
        for (i in 0 until contributionCount) {
          val deposit = yearContributionAmounts[i]
          if (deposit > 0) {
            potBalances[contributionPotIndex[i]] += deposit
            total += deposit
          }
        }
      }

      // Income pays for the year's spending first, and the pots fund the rest
      val totalPlanned = plannedTodayByYear[year] * cumulativeInflation
      val incomeTowardsSpending = min(flows.spendableIncome, totalPlanned)
      val planned = totalPlanned - incomeTowardsSpending
      val unspentIncome = flows.spendableIncome - incomeTowardsSpending

      // Every pot experiences the same market year
      val historyIndex =
        when (returnModel) {
          HistoricalBootstrap -> floor(random.next() * historyCount).toInt()
          HistoricalSequence -> (simulationIndex + year - 1) % historyCount
          Normal,
          Unknown -> -1
        }

      var accessibleTotal = 0.0
      for (i in 0 until potCount) {
        if (year >= potAccessFromYear[i]) accessibleTotal += potBalances[i]
      }

      var withdrawal: Double
      if (rule.type == FloorCeiling) {
        if (year == firstSpendingYear) {
          floorCeilingAnchorRate = if (accessibleTotal > 0) planned / accessibleTotal else 0.0
        }
        withdrawal =
          if (planned > 0 && year > firstSpendingYear) {
            val unclamped = floorCeilingAnchorRate * accessibleTotal
            unclamped.coerceIn(planned * (1 - rule.floorPct), planned * (1 + rule.ceilingPct))
          } else {
            planned
          }
      } else {
        val isSpending = planned > 0 && year > firstSpendingYear
        val hasAccessible = accessibleTotal > 0 && accessibleStartByYear[year] > 0
        if (isSpending && hasAccessible && rule.type != None) {
          val currentRate = planned * adjustmentFactor / accessibleTotal
          when (rule.type) {
            Guardrails -> {
              // Measured against the planned path, so a phase change doesn't read as drift
              val referenceRate = potFundedPlannedTodayByYear[year] / accessibleStartByYear[year]
              if (currentRate > referenceRate * (1 + rule.preservationTriggerPct)) {
                adjustmentFactor *= 1 - rule.preservationCutPct
              } else if (currentRate < referenceRate * (1 - rule.prosperityTriggerPct)) {
                adjustmentFactor *= 1 + rule.prosperityIncreasePct
              }
            }

            Ratcheting -> {
              if (accessibleTotal > accessibleStartByYear[year] * rule.balanceThresholdMultiple) {
                ratchetStreak++
                if (ratchetStreak >= rule.consecutiveYears) {
                  adjustmentFactor *= 1 + rule.ratchetIncreasePct
                  ratchetStreak = 0
                }
              } else {
                ratchetStreak = 0
              }
            }

            Boundaries -> {
              if (currentRate > rule.upperRateThreshold) {
                adjustmentFactor *= 1 - rule.upperCutPct
              } else if (currentRate < rule.lowerRateThreshold) {
                adjustmentFactor *= 1 + rule.lowerIncreasePct
              }
            }

            Unknown -> {
              // No-op
            }
          }
        }
        withdrawal = planned * adjustmentFactor
      }

      // The minimum spending floor only applies alongside a rule, in years with planned spending.
      // Income counts towards it
      val minimumFromPots = max(0.0, minimumSpending * cumulativeInflation - incomeTowardsSpending)
      val hasMinimum = rule.type != None && minimumSpending > 0
      if (hasMinimum && planned > 0 && withdrawal < minimumFromPots) {
        withdrawal = minimumFromPots
      }

      // The requirement is net of tax
      val netRequired = withdrawal
      var fundingShortfall = false

      var accessibleNetCapacity = accessibleTotal
      if (hasTax && accessibleTotal * minNetFactor <= netRequired) {
        computeTakes(accessibleTotal, year, accessibleTotal)
        accessibleNetCapacity =
          accessibleTotal - taxForTakes(cumulativeInflation, flows.taxableIncomeBase)
      }

      if (netRequired > 0 && accessibleNetCapacity < netRequired) {
        // The accessible pots can't cover this year's spending, even if locked pots hold money
        fundingShortfall = true
        potBalances.fill(0.0)
        total = 0.0
        depleted = true
        depletionCounts[year]++
      } else {
        // Solve g = net + tax(takes(g)); tax is piecewise linear with marginal rates < 1, so this
        // converges
        var grossTotal = netRequired
        if (hasTax) {
          var iterations = 0
          var converged = false
          while (!converged && iterations < TAX_ITERATIONS) {
            computeTakes(grossTotal, year, accessibleTotal)
            val next = netRequired + taxForTakes(cumulativeInflation, flows.taxableIncomeBase)
            converged = abs(next - grossTotal) <= TAX_CONVERGENCE * max(1.0, next)
            grossTotal = next
            iterations++
          }
          grossTotal = min(grossTotal, accessibleTotal)
        }

        computeTakes(grossTotal, year, accessibleTotal)
        for (i in 0 until potCount) potBalances[i] -= potTakes[i]

        // Unspent income is saved into the surplus pot before growth
        if (surplusPotIndex != -1) potBalances[surplusPotIndex] += unspentIncome

        val marketShock = if (hasNormalDrawPot) random.nextNormal() else 0.0

        total = 0.0
        for (i in 0 until potCount) {
          if (potBalances[i] > 0) {
            val blended = potHistoricalReturns[i]
            val yearReturn =
              if (blended != null && historyIndex >= 0) {
                blended[historyIndex]
              } else {
                potMeans[i] + potStdDevs[i] * marketShock
              }
            previousReturns[i] = yearReturn
            potBalances[i] *= 1 + yearReturn
            if (potBalances[i] <= 0) potBalances[i] = 0.0
          }
          total += potBalances[i]
        }
      }

      // Historical models take the sampled year's own inflation, the normal model draws from
      // the configured mean and volatility
      if (inflationMean != null) {
        val rate =
          when {
            historyIndex >= 0 -> history[historyIndex].inflation
            inflationStdDev > 0 ->
              max(MIN_INFLATION, inflationMean + inflationStdDev * random.nextNormal())
            else -> inflationMean
          }
        cumulativeInflation *= 1 + rate
      }

      // Fees come out at the end of the year, after growth and inflation
      if (hasFees && !fundingShortfall && total > 0) {
        for (i in 0 until potCount) {
          if (potBalances[i] <= 0) continue
          val feeInflation = if (pots[i].feeAdjustsWithInflation) cumulativeInflation else 1.0
          val fixed = potFeeFixed[i] * feeInflation
          val fee = potBalances[i] * potFeeRates[i] + fixed
          potBalances[i] -= min(potBalances[i], fee)
        }
        total = potBalances.sum()
      }

      val endDeflator = if (deflate) 1 / cumulativeInflation else 1.0
      balancesByYear[year][simulationIndex] = toSafeAmount(total * endDeflator)
    }

    if (!depleted) survivedCount++
  }

  var finalSorted = DoubleArray(0)
  val percentileBands =
    (0..horizonYears).map { year ->
      val sorted = balancesByYear[year].sortedArray()
      if (year == horizonYears) finalSorted = sorted
      McPercentileBand(
        year = year,
        p10 = roundJs(percentileOfSorted(sorted, P10)),
        p25 = roundJs(percentileOfSorted(sorted, P25)),
        p50 = roundJs(percentileOfSorted(sorted, P50)),
        p75 = roundJs(percentileOfSorted(sorted, P75)),
        p90 = roundJs(percentileOfSorted(sorted, P90)),
      )
    }

  val totalDepleted = simulationCount - survivedCount
  val medianTargetIndex = floor((totalDepleted - 1) / 2.0).toInt()
  var medianDepletionYear: Int? = null
  var seenDepleted = 0
  for (year in 1..horizonYears) {
    if (depletionCounts[year] == 0) continue
    seenDepleted += depletionCounts[year]
    if (seenDepleted > medianTargetIndex) {
      medianDepletionYear = year
      break
    }
  }

  return McResult(
    successRate = survivedCount.toDouble() / simulationCount,
    percentileBands = percentileBands,
    medianEndingBalance = roundJs(percentileOfSorted(finalSorted, P50)),
    medianDepletionYear = medianDepletionYear,
    simulationCount = simulationCount,
    horizonYears = horizonYears,
  )
}

private const val HALF = 0.5
private const val P10 = 0.1
private const val P25 = 0.25
private const val P50 = 0.5
private const val P75 = 0.75
private const val P90 = 0.9
