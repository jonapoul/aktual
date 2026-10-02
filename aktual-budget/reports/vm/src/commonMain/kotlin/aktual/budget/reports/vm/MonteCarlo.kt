package aktual.budget.reports.vm

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// Ported from
// packages/desktop-client/src/components/reports/reports/monte-carlo/monteCarloSimulation.ts.
// Amounts are in minor units, rates are decimal fractions (0.06 = 6%)

private const val MAX_EMITTED = 1_125_899_906_842_624.0 // 2^50
private const val MAX_EMITTED_LONG = 1_125_899_906_842_624L
private const val MIN_INFLATION = -0.9
private const val TAX_ITERATIONS = 40
private const val TAX_CONVERGENCE = 1e-7
internal const val DEFAULT_SIMULATION_SEED = 1234

internal data class McPercentileBand(
  // 0 = starting point, 1..horizonYears = end of that year
  val year: Int,
  val p5: Long,
  val p10: Long,
  val p25: Long,
  val p30: Long,
  val p50: Long,
  val p70: Long,
  val p75: Long,
  val p90: Long,
)

@Suppress("LongParameterList", "UseDataClass")
internal class McResult(
  // Share (0..1) of simulations that survived the full horizon
  val successRate: Double,
  val percentileBands: List<McPercentileBand>,
  // Index i is the count of simulations depleted in year i + 1
  val depletionCounts: List<Int>,
  val medianEndingBalance: Long,
  // Median across simulations of the total withdrawn over the horizon
  val medianTotalWithdrawn: Long,
  val medianDepletionYear: Int?,
  val earliestDepletionYear: Int?,
  val latestDepletionYear: Int?,
  // Balance path (year 0..horizonYears) of the run that ran out earliest, or ended lowest
  val worstRunPath: List<Long>,
  val endingBalances: DoubleArray,
  // Depletion year per simulation; -1 means it survived
  val depletionYearBySimulation: IntArray,
  val totalWithdrawnBySimulation: DoubleArray,
  // Year-by-year rows for the simulation requested with captureRunDetail
  val runDetail: List<McRunDetailRow>?,
  val simulationCount: Int,
  val horizonYears: Int,
)

// getPotAssetWeights() for pots, plus getHistoricalMixStats(): the measured mean and sample
// standard deviation of a mix's blended historical series
fun historicalMixStats(weights: AssetWeights): ReturnStats =
  historicalMixStats(weights, HISTORICAL_RETURNS)

internal fun historicalMixStats(
  weights: AssetWeights,
  history: List<HistoricalReturn>,
): ReturnStats {
  val blended = history.map {
    weights.stocks * it.stocks + weights.bonds * it.bonds + weights.cash * it.cash
  }
  val mean = blended.sum() / blended.size
  val variance =
    if (blended.size > 1) {
      blended.sumOf { (it - mean) * (it - mean) } / (blended.size - 1)
    } else {
      0.0
    }
  return ReturnStats(mean, sqrt(variance))
}

val HISTORICAL_FIRST_YEAR: Int
  get() = HISTORICAL_RETURNS.first().year

val HISTORICAL_LAST_YEAR: Int
  get() = HISTORICAL_RETURNS.last().year

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

internal fun roundJs(value: Double): Long = floor(value + HALF).toLong()

// rankSimulationsWorstFirst(): by ending balance, using the depletion year to order the failed runs
// (which all end at zero) among themselves. Survivors rank after any depleted run
internal fun rankSimulationsWorstFirst(
  endingBalances: DoubleArray,
  depletionYearBySimulation: IntArray,
): IntArray =
  endingBalances.indices
    .sortedWith { a, b ->
      val balanceDiff = endingBalances[a].compareTo(endingBalances[b])
      if (balanceDiff != 0) return@sortedWith balanceDiff
      val yearA = depletionYearBySimulation[a]
      val yearB = depletionYearBySimulation[b]
      when {
        yearA == -1 && yearB == -1 -> 0
        yearA == -1 -> 1
        yearB == -1 -> -1
        else -> yearA.compareTo(yearB)
      }
    }
    .toIntArray()

@Suppress("UseDataClass")
private class Schedule(val flatByYear: Array<DoubleArray>, val adjustedByYear: Array<DoubleArray>)

private data class YearFlows(
  val grossIncome: Double,
  val incomeTax: Double,
  val taxableIncomeBase: Double,
  val spendableIncome: Double,
)

private fun toSafeAmount(value: Double) = value.coerceIn(-MAX_EMITTED, MAX_EMITTED)

// Every emitted amount is deflated, rounded, and kept within the formatter-safe range
private fun emit(value: Double, deflator: Double): Long =
  roundJs(value * deflator).coerceIn(-MAX_EMITTED_LONG, MAX_EMITTED_LONG)

// Per-pot amounts are rounded so they sum exactly to their row's already-rounded total: floor each
// part, then hand the leftover cents to the parts that lost the most in flooring (largest
// remainder, ties to the lower index)
private fun emitParts(values: DoubleArray?, deflator: Double, target: Long): List<Long> {
  if (values == null) return emptyList()
  val scaled = DoubleArray(values.size) { values[it] * deflator }
  val parts = DoubleArray(values.size) { floor(scaled[it]) }
  val shortfall = target - parts.sum()
  if (shortfall < 0 || shortfall > parts.size) {
    // The parts don't reconcile with this target, so round them independently
    return values.map { emit(it, deflator) }
  }
  val byLargestRemainder =
    parts.indices.sortedWith(compareByDescending<Int> { scaled[it] - parts[it] }.thenBy { it })
  for (i in 0 until shortfall.toInt()) parts[byLargestRemainder[i]] += 1
  return parts.map { toSafeAmount(it).toLong() }
}

// runMonteCarloSimulation(). deflate converts outputs to today's money; captureRunDetail is the
// simulation index to capture year-by-year rows for. Runs are seeded, so re-running with the same
// config reproduces any run exactly
@Suppress(
  "CyclomaticComplexMethod",
  "LongMethod",
  "NestedBlockDepth",
  "LoopWithTooManyJumpStatements",
  "ComplexCondition",
)
internal fun runMonteCarlo(
  config: McConfig,
  history: List<HistoricalReturn> = HISTORICAL_RETURNS,
  seed: Int = DEFAULT_SIMULATION_SEED,
  deflate: Boolean = true,
  captureRunDetail: Int? = null,
): McResult {
  // The surplus pot's semantics are fixed, whatever the stored meta says
  val pots =
    config.pots
      .ifEmpty { McConfig().pots }
      .map { if (it.isSurplus) surplusPot(it.id, it.name) else it }
  val potCount = pots.size
  val potStartBalances =
    DoubleArray(potCount) { pots[it].startingBalance.coerceIn(0.0, MC_MAX_AMOUNT) }
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
    DoubleArray(potCount) { pots[it].withdrawalTaxRate.coerceIn(0.0, MC_MAX_WITHDRAWAL_TAX_RATE) }
  val potTaxableFractions = DoubleArray(potCount) { pots[it].taxableFraction.coerceIn(0.0, 1.0) }
  val taxBands =
    config.taxBands
      .map { band ->
        McTaxBand(
          from = band.from.coerceIn(0.0, MC_MAX_AMOUNT),
          rate = band.rate.coerceIn(0.0, MC_MAX_TAX_BAND_RATE),
        )
      }
      .sortedBy { it.from }
      .toMutableList()
  if (taxBands.isEmpty() || taxBands[0].from > 0) taxBands.add(0, McTaxBand(from = 0.0, rate = 0.0))
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
    DoubleArray(incomeCount) {
      incomeStreams[it].taxRate.coerceIn(0.0, MC_MAX_WITHDRAWAL_TAX_RATE)
    }
  val incomeTaxableFractions =
    DoubleArray(incomeCount) { incomeStreams[it].taxableFraction.coerceIn(0.0, 1.0) }
  val hasIncomeTax =
    if (isBands) {
      taxBands.any { it.rate > 0 } && incomeTaxableFractions.any { it > 0 }
    } else {
      incomeTaxRates.any { it > 0 }
    }

  // Fees
  val potFeeFixed = DoubleArray(potCount) { pots[it].annualFeeFixed.coerceIn(0.0, MC_MAX_AMOUNT) }
  val potFeeRates =
    DoubleArray(potCount) { pots[it].annualFeeRate.coerceIn(0.0, MC_MAX_ANNUAL_FEE_RATE) }
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

  // The slice of each pot's take that was assessed for tax, for the captured run
  fun captureTaxables(into: DoubleArray) {
    for (i in 0 until potCount) {
      into[i] =
        when {
          isBands -> potTakes[i] * potTaxableFractions[i]
          potTaxRates[i] > 0 -> potTakes[i]
          else -> 0.0
        }
    }
  }

  // Attributes a year's tax to pots for the captured run: exact under the flat model, prorated by
  // taxable income share under the bands model
  fun attributeTaxToPots(totalTax: Double, into: DoubleArray) {
    if (totalTax <= 0) return
    if (!isBands) {
      for (i in 0 until potCount) into[i] = potTakes[i] * potTaxRates[i]
      return
    }
    var taxableTotal = 0.0
    for (i in 0 until potCount) taxableTotal += potTakes[i] * potTaxableFractions[i]
    if (taxableTotal <= 0) return
    for (i in 0 until potCount) {
      into[i] = totalTax * (potTakes[i] * potTaxableFractions[i] / taxableTotal)
    }
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
  val phases = config.spendingPhases.resolve()
  val plannedTodayByYear = DoubleArray(horizonYears + 1)
  for (year in 1..horizonYears) {
    val age = config.currentAge + year - 1
    plannedTodayByYear[year] = phases.activeAt(age).annualWithdrawal.coerceIn(0.0, MC_MAX_AMOUNT)
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
      val amount = rawAmount.coerceIn(0.0, MC_MAX_AMOUNT)
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
  val yearIncomeGross = DoubleArray(incomeCount)
  val yearIncomeRemaining = DoubleArray(incomeCount)

  // Settles a year's income and contributions: before-tax contributions come out of their stream's
  // gross, each stream is taxed, after-tax contributions come out of the net, and the rest is
  // spendable. Contributions from outside the plan are paid in full
  fun settleYearFlows(
    year: Int,
    cumulativeInflation: Double,
    withContributions: Boolean,
  ): YearFlows {
    val flatIncome = incomeSchedule.flatByYear[year]
    val adjustedIncome = incomeSchedule.adjustedByYear[year]
    var grossIncome = 0.0
    for (i in 0 until incomeCount) {
      val gross = flatIncome[i] + adjustedIncome[i] * cumulativeInflation
      yearIncomeGross[i] = gross
      yearIncomeRemaining[i] = gross
      grossIncome += gross
    }

    val flatContributions = contributionSchedule.flatByYear[year]
    val adjustedContributions = contributionSchedule.adjustedByYear[year]
    yearContributionAmounts.fill(0.0)
    if (withContributions) {
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

    if (withContributions) {
      for (i in 0 until contributionCount) {
        val source = contributionSourceIndex[i]
        if (source == -1 || contributions[i].beforeTax) continue
        val scheduled = flatContributions[i] + adjustedContributions[i] * cumulativeInflation
        val amount = min(scheduled, yearIncomeRemaining[source])
        yearIncomeRemaining[source] -= amount
        yearContributionAmounts[i] = amount
      }
    }

    return YearFlows(grossIncome, incomeTax, taxableIncomeBase, yearIncomeRemaining.sum())
  }

  // The part of each year's planned spending the pots have to fund, in today's money. Withdrawal
  // rules anchor and measure against this path
  val potFundedPlannedTodayByYear = DoubleArray(horizonYears + 1)
  for (year in 1..horizonYears) {
    val spendable = settleYearFlows(year, 1.0, withContributions = true).spendableIncome
    potFundedPlannedTodayByYear[year] = max(0.0, plannedTodayByYear[year] - spendable)
  }
  val firstSpendingYear =
    (1..horizonYears).firstOrNull { potFundedPlannedTodayByYear[it] > 0 } ?: Int.MAX_VALUE

  val shouldDeflate = deflate && inflationMean != null
  // Sequence replay runs one scenario per historical start year
  val simulationCount =
    if (returnModel == HistoricalSequence) {
      historyCount
    } else {
      config.simulationCount.coerceIn(MC_MIN_SIMULATION_COUNT, MC_MAX_SIMULATION_COUNT)
    }

  val random = Mulberry32(seed)
  val balancesByYear = Array(horizonYears + 1) { DoubleArray(simulationCount) }
  val depletionCounts = IntArray(horizonYears + 1)
  var survivedCount = 0

  // The unluckiest simulation: earliest depletion, or lowest ending balance
  var worstSimIndex = 0
  var worstDepletionYear = Int.MAX_VALUE
  var worstFinalBalance = Double.POSITIVE_INFINITY

  val startingTotal = potStartBalances.sum()
  val rule = config.withdrawalRule
  val minimumSpending = config.minimumSpending.coerceIn(0.0, MC_MAX_AMOUNT)

  // Starting wealth that's accessible in each year, so locked pots don't drive rule decisions
  val accessibleStartByYear = DoubleArray(horizonYears + 1)
  for (year in 1..horizonYears) {
    var accessibleStart = 0.0
    for (i in 0 until potCount) {
      if (year >= potAccessFromYear[i]) accessibleStart += potStartBalances[i]
    }
    accessibleStartByYear[year] = accessibleStart
  }

  val withdrawnTotals = DoubleArray(simulationCount)
  val depletionYearBySimulation = IntArray(simulationCount) { -1 }

  val captureIndex = captureRunDetail ?: -1
  val runDetail: MutableList<McRunDetailRow>? =
    if (captureIndex in 0 until simulationCount) mutableListOf() else null

  for (simulationIndex in 0 until simulationCount) {
    potStartBalances.copyInto(potBalances)
    previousReturns.fill(0.0)
    var total = startingTotal
    var adjustmentFactor = 1.0
    var floorCeilingAnchorRate = 0.0
    var cumulativeInflation = 1.0
    var ratchetStreak = 0
    var withdrawnSum = 0.0
    var depleted = false
    var simulationDepletionYear = Int.MAX_VALUE
    val isCapturedRun = runDetail != null && simulationIndex == captureIndex

    balancesByYear[0][simulationIndex] = toSafeAmount(total)

    for (year in 1..horizonYears) {
      if (!depleted) {
        val startDeflator = if (shouldDeflate) 1 / cumulativeInflation else 1.0

        // Income arrives and contributions land at the start of the year, before the withdrawal
        val flows = settleYearFlows(year, cumulativeInflation, withContributions = true)
        var contributionsThisYear = 0.0
        var preContributionPotBalances: DoubleArray? = null
        val capturedPotContributions = if (isCapturedRun) DoubleArray(potCount) else null
        if (hasContributions) {
          if (capturedPotContributions != null) preContributionPotBalances = potBalances.copyOf()
          for (i in 0 until contributionCount) {
            val deposit = yearContributionAmounts[i]
            if (deposit > 0) {
              val potIndex = contributionPotIndex[i]
              potBalances[potIndex] += deposit
              total += deposit
              contributionsThisYear += deposit
              if (capturedPotContributions != null) capturedPotContributions[potIndex] += deposit
            }
          }
        }
        val capturedContributionAmounts = capturedPotContributions?.let {
          yearContributionAmounts.copyOf()
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

        var capturedRuleExplanation: McRuleExplanation? = null
        var capturedMinimumApplied = false
        var withdrawal: Double
        if (rule.type == FloorCeiling) {
          if (year == firstSpendingYear) {
            floorCeilingAnchorRate = if (accessibleTotal > 0) planned / accessibleTotal else 0.0
            if (isCapturedRun) {
              capturedRuleExplanation = McRuleExplanation.Anchor(floorCeilingAnchorRate)
            }
          }
          if (planned > 0 && year > firstSpendingYear) {
            val unclamped = floorCeilingAnchorRate * accessibleTotal
            val floorAmount = planned * (1 - rule.floorPct)
            val ceilingAmount = planned * (1 + rule.ceilingPct)
            withdrawal = unclamped.coerceAtLeast(floorAmount).coerceAtMost(ceilingAmount)
            if (isCapturedRun) {
              capturedRuleExplanation =
                McRuleExplanation.FloorCeiling(
                  rate = floorCeilingAnchorRate,
                  unclamped = emit(unclamped, startDeflator),
                  floor = emit(floorAmount, startDeflator),
                  ceiling = emit(ceilingAmount, startDeflator),
                  applied =
                    when {
                      unclamped < floorAmount -> FloorCeilingBound.Floor
                      unclamped > ceilingAmount -> FloorCeilingBound.Ceiling
                      else -> FloorCeilingBound.Rate
                    },
                )
            }
          } else {
            withdrawal = planned
          }
        } else {
          val isSpending = planned > 0 && year > firstSpendingYear
          val hasAccessible = accessibleTotal > 0 && accessibleStartByYear[year] > 0
          if (isSpending && hasAccessible && rule.type != None) {
            val currentRate = planned * adjustmentFactor / accessibleTotal
            var action = RuleAction.None
            when (rule.type) {
              Guardrails -> {
                // Measured against the planned path, so a phase change doesn't read as drift
                val referenceRate = potFundedPlannedTodayByYear[year] / accessibleStartByYear[year]
                if (currentRate > referenceRate * (1 + rule.preservationTriggerPct)) {
                  adjustmentFactor *= 1 - rule.preservationCutPct
                  action = RuleAction.Cut
                } else if (currentRate < referenceRate * (1 - rule.prosperityTriggerPct)) {
                  adjustmentFactor *= 1 + rule.prosperityIncreasePct
                  action = RuleAction.Raise
                }
              }

              Ratcheting -> {
                if (accessibleTotal > accessibleStartByYear[year] * rule.balanceThresholdMultiple) {
                  ratchetStreak++
                  if (ratchetStreak >= rule.consecutiveYears) {
                    adjustmentFactor *= 1 + rule.ratchetIncreasePct
                    ratchetStreak = 0
                    action = RuleAction.Raise
                  }
                } else {
                  ratchetStreak = 0
                }
              }

              Boundaries -> {
                if (currentRate > rule.upperRateThreshold) {
                  adjustmentFactor *= 1 - rule.upperCutPct
                  action = RuleAction.Cut
                } else if (currentRate < rule.lowerRateThreshold) {
                  adjustmentFactor *= 1 + rule.lowerIncreasePct
                  action = RuleAction.Raise
                }
              }

              None,
              FloorCeiling,
              Unknown -> {
                // No-op
              }
            }
            val isFactorRule =
              rule.type == Guardrails || rule.type == Ratcheting || rule.type == Boundaries
            if (isCapturedRun && isFactorRule) {
              capturedRuleExplanation =
                McRuleExplanation.Factor(
                  rule = rule.type,
                  factor = adjustmentFactor,
                  planned = emit(planned, startDeflator),
                  adjusted = emit(planned * adjustmentFactor, startDeflator),
                  action = action,
                  currentRate = currentRate.takeIf { rule.type != Ratcheting },
                  referenceRate =
                    (potFundedPlannedTodayByYear[year] / accessibleStartByYear[year]).takeIf {
                      rule.type == Guardrails
                    },
                  ratchetStreak =
                    ratchetStreak.takeIf { rule.type == Ratcheting && action == RuleAction.None },
                )
            }
          }
          withdrawal = planned * adjustmentFactor
        }

        // The minimum spending floor only applies alongside a rule, in years with planned spending.
        // Income counts towards it
        val minimumFromPots =
          max(0.0, minimumSpending * cumulativeInflation - incomeTowardsSpending)
        val hasMinimum = rule.type != None && minimumSpending > 0
        if (hasMinimum && planned > 0 && withdrawal < minimumFromPots) {
          withdrawal = minimumFromPots
          if (isCapturedRun) capturedMinimumApplied = true
        }

        // Before affordability capping, so the cashflow chart can show shortfalls
        val plannedSpendingThisYear = incomeTowardsSpending + withdrawal

        val yearStartTotal = total
        // The requirement is net of tax; withdrawalTaken is the gross that leaves the pots
        val netRequired = withdrawal
        val withdrawalTaken: Double
        val netDelivered: Double
        var fundingShortfall = false
        var surplusSavedThisYear = 0.0

        var failurePotSnapshot: DoubleArray? = null
        val captured = if (isCapturedRun) CapturedPots(potCount) else null
        val capturedPotStartBalances =
          if (isCapturedRun) preContributionPotBalances ?: potBalances.copyOf() else null

        var accessibleNetCapacity = accessibleTotal
        if (hasTax && accessibleTotal * minNetFactor <= netRequired) {
          computeTakes(accessibleTotal, year, accessibleTotal)
          accessibleNetCapacity =
            accessibleTotal - taxForTakes(cumulativeInflation, flows.taxableIncomeBase)
        }

        if (netRequired > 0 && accessibleNetCapacity < netRequired) {
          // The accessible pots can't cover this year's spending, even if locked pots hold money,
          // so they're emptied for whatever net they can deliver
          fundingShortfall = true
          withdrawnSum = toSafeAmount(withdrawnSum + accessibleTotal * startDeflator)
          withdrawalTaken = accessibleTotal
          netDelivered = max(0.0, accessibleNetCapacity)
          if (captured != null) {
            failurePotSnapshot =
              DoubleArray(potCount) { i ->
                if (year >= potAccessFromYear[i]) 0.0 else roundJs(potBalances[i]).toDouble()
              }
            for (i in 0 until potCount) {
              captured.withdrawals[i] = if (year >= potAccessFromYear[i]) potBalances[i] else 0.0
            }
            // potTakes still holds the everything-accessible split from the capacity check
            attributeTaxToPots(accessibleTotal - netDelivered, captured.taxes)
            if (hasTax) captureTaxables(captured.taxables)
          }
          potBalances.fill(0.0)
          total = 0.0
          depleted = true
          simulationDepletionYear = year
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
          withdrawnSum = toSafeAmount(withdrawnSum + grossTotal * startDeflator)
          withdrawalTaken = grossTotal
          netDelivered = netRequired

          if (captured != null) {
            potTakes.copyInto(captured.withdrawals)
            attributeTaxToPots(grossTotal - netRequired, captured.taxes)
            if (hasTax) captureTaxables(captured.taxables)
          }

          // Unspent income is saved into the surplus pot before growth
          if (surplusPotIndex != -1) {
            surplusSavedThisYear = unspentIncome
            potBalances[surplusPotIndex] += surplusSavedThisYear
          }

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
              captured?.returns?.set(i, yearReturn)
              previousReturns[i] = yearReturn
              potBalances[i] *= 1 + yearReturn
              if (potBalances[i] <= 0) potBalances[i] = 0.0
            }
            total += potBalances[i]
          }
        }

        // Historical models take the sampled year's own inflation, the normal model draws from
        // the configured mean and volatility
        var yearInflationRate: Double? = null
        if (inflationMean != null) {
          val rate =
            when {
              historyIndex >= 0 -> history[historyIndex].inflation
              inflationStdDev > 0 ->
                max(MIN_INFLATION, inflationMean + inflationStdDev * random.nextNormal())
              else -> inflationMean
            }
          yearInflationRate = rate
          cumulativeInflation *= 1 + rate
        }

        // Fees come out at the end of the year, after growth and inflation
        var feesThisYear = 0.0
        if (hasFees && !fundingShortfall && total > 0) {
          for (i in 0 until potCount) {
            if (potBalances[i] <= 0) continue
            val feeInflation = if (pots[i].feeAdjustsWithInflation) cumulativeInflation else 1.0
            val fee = potBalances[i] * potFeeRates[i] + potFeeFixed[i] * feeInflation
            val charged = min(potBalances[i], fee)
            potBalances[i] -= charged
            feesThisYear += charged
            captured?.fees?.set(i, charged)
          }
          total = potBalances.sum()
        }
        val endDeflator = if (shouldDeflate) 1 / cumulativeInflation else 1.0

        if (runDetail != null && isCapturedRun) {
          // The displayed start excludes contributions: start + contributions - withdrawal +
          // growth - fees = end
          val startBalance = emit(yearStartTotal - contributionsThisYear, startDeflator)
          val contributionsEmitted = emit(contributionsThisYear, startDeflator)
          val withdrawalEmitted = emit(withdrawalTaken, startDeflator)
          val taxPaid = emit(withdrawalTaken - netDelivered, startDeflator)
          val income = emit(flows.grossIncome, startDeflator)
          val base =
            McRunDetailRow(
              year = year,
              startBalance = startBalance,
              withdrawal = withdrawalEmitted,
              plannedSpending = emit(plannedSpendingThisYear, startDeflator),
              spent = emit(incomeTowardsSpending + netDelivered, startDeflator),
              growth = 0,
              endBalance = 0,
              potBalances = persistentListOf(),
              potStartBalances =
                emitParts(capturedPotStartBalances, startDeflator, startBalance).toImmutableList(),
              // A rate, not an amount, so it's passed through undeflated
              inflation = yearInflationRate,
              income = income,
              incomeAmounts = emitParts(yearIncomeGross, startDeflator, income).toImmutableList(),
              incomeTax = emit(flows.incomeTax, startDeflator),
              unspentIncome = emit(unspentIncome, startDeflator),
              surplusSaved = emit(surplusSavedThisYear, startDeflator),
              contributions = contributionsEmitted,
              potContributions =
                emitParts(capturedPotContributions, startDeflator, contributionsEmitted)
                  .toImmutableList(),
              contributionAmounts =
                emitParts(capturedContributionAmounts, startDeflator, contributionsEmitted)
                  .toImmutableList(),
              potWithdrawals =
                emitParts(captured?.withdrawals, startDeflator, withdrawalEmitted)
                  .toImmutableList(),
              potTaxes = emitParts(captured?.taxes, startDeflator, taxPaid).toImmutableList(),
              // No displayed total to reconcile against
              potTaxables =
                captured?.taxables.orEmpty().map { emit(it, startDeflator) }.toImmutableList(),
              taxPaid = taxPaid,
              feesPaid = 0,
              potFees = persistentListOf(),
              potReturns = persistentListOf(),
              ruleExplanation = capturedRuleExplanation,
              minimumApplied = capturedMinimumApplied,
            )
          runDetail +=
            if (fundingShortfall) {
              // Any remaining balance was locked in pots not yet accessible, not lost to markets
              val locked = emit(yearStartTotal - withdrawalTaken, startDeflator)
              base.copy(
                potFees = List(potCount) { 0L }.toImmutableList(),
                potBalances =
                  emitParts(failurePotSnapshot, startDeflator, locked).toImmutableList(),
                potReturns = captured?.returnsOrNull().orEmpty().toImmutableList(),
                inaccessibleBalance = locked.takeIf { it > 0 },
              )
            } else {
              val feesPaid = emit(feesThisYear, endDeflator)
              val endBalance = emit(total, endDeflator)
              base.copy(
                // In today's money, growth is the real gain. Fees are reported separately, so
                // growth stays pure market performance
                growth =
                  roundJs(
                      (total + feesThisYear) * endDeflator -
                        (yearStartTotal - withdrawalTaken + surplusSavedThisYear) * startDeflator
                    )
                    .coerceIn(-MAX_EMITTED_LONG, MAX_EMITTED_LONG),
                feesPaid = feesPaid,
                potFees = emitParts(captured?.fees, endDeflator, feesPaid).toImmutableList(),
                endBalance = endBalance,
                potBalances = emitParts(potBalances, endDeflator, endBalance).toImmutableList(),
                potReturns =
                  captured
                    ?.returnsOrNull()
                    .orEmpty()
                    .map { potReturn ->
                      potReturn?.let { (1 + it) * (endDeflator / startDeflator) - 1 }
                    }
                    .toImmutableList(),
              )
            }
        }

        balancesByYear[year][simulationIndex] = toSafeAmount(total * endDeflator)
      } else if (runDetail != null && isCapturedRun) {
        runDetail +=
          unfundedYear(
            year = year,
            frozenDeflator = if (shouldDeflate) 1 / cumulativeInflation else 1.0,
            flows = settleYearFlows(year, cumulativeInflation, withContributions = false),
            totalPlanned = plannedTodayByYear[year] * cumulativeInflation,
            adjustmentFactor = adjustmentFactor,
            minimumThisYear =
              if (rule.type != None && minimumSpending > 0) {
                minimumSpending * cumulativeInflation
              } else {
                null
              },
            incomeAmounts = yearIncomeGross,
            potCount = potCount,
            contributionCount = contributionCount,
          )
      }
      // Post-depletion years otherwise stay at zero
    }

    withdrawnTotals[simulationIndex] = withdrawnSum
    if (simulationDepletionYear != Int.MAX_VALUE) {
      depletionYearBySimulation[simulationIndex] = simulationDepletionYear
    }

    if (!depleted) survivedCount++

    if (
      simulationDepletionYear < worstDepletionYear ||
        simulationDepletionYear == worstDepletionYear && total < worstFinalBalance
    ) {
      worstSimIndex = simulationIndex
      worstDepletionYear = simulationDepletionYear
      worstFinalBalance = total
    }
  }

  var finalSorted = DoubleArray(0)
  val percentileBands =
    (0..horizonYears).map { year ->
      val sorted = balancesByYear[year].sortedArray()
      if (year == horizonYears) finalSorted = sorted
      McPercentileBand(
        year = year,
        p5 = roundJs(percentileOfSorted(sorted, P5)),
        p10 = roundJs(percentileOfSorted(sorted, P10)),
        p25 = roundJs(percentileOfSorted(sorted, P25)),
        p30 = roundJs(percentileOfSorted(sorted, P30)),
        p50 = roundJs(percentileOfSorted(sorted, P50)),
        p70 = roundJs(percentileOfSorted(sorted, P70)),
        p75 = roundJs(percentileOfSorted(sorted, P75)),
        p90 = roundJs(percentileOfSorted(sorted, P90)),
      )
    }

  // Earliest, median and latest failure years straight from the counts
  val totalDepleted = simulationCount - survivedCount
  val medianTargetIndex = floor((totalDepleted - 1) / 2.0).toInt()
  var earliestDepletionYear: Int? = null
  var latestDepletionYear: Int? = null
  var medianDepletionYear: Int? = null
  var seenDepleted = 0
  for (year in 1..horizonYears) {
    if (depletionCounts[year] == 0) continue
    if (earliestDepletionYear == null) earliestDepletionYear = year
    latestDepletionYear = year
    if (medianDepletionYear == null) {
      seenDepleted += depletionCounts[year]
      if (seenDepleted > medianTargetIndex) medianDepletionYear = year
    }
  }

  return McResult(
    successRate = survivedCount.toDouble() / simulationCount,
    percentileBands = percentileBands,
    depletionCounts = depletionCounts.drop(1),
    medianEndingBalance = roundJs(percentileOfSorted(finalSorted, P50)),
    medianTotalWithdrawn = roundJs(percentileOfSorted(withdrawnTotals.sortedArray(), P50)),
    medianDepletionYear = medianDepletionYear,
    earliestDepletionYear = earliestDepletionYear,
    latestDepletionYear = latestDepletionYear,
    worstRunPath = (0..horizonYears).map { roundJs(balancesByYear[it][worstSimIndex]) },
    endingBalances = balancesByYear[horizonYears].copyOf(),
    depletionYearBySimulation = depletionYearBySimulation,
    totalWithdrawnBySimulation = withdrawnTotals,
    runDetail = runDetail,
    simulationCount = simulationCount,
    horizonYears = horizonYears,
  )
}

// A captured year after the plan ran out, for the cashflow chart. Nothing moves and no RNG is
// drawn, so inflation stays frozen at the failure year's level. Income keeps covering what it can,
// and the rule's running adjustment persists on the pot-funded part. Contributions stop
@Suppress("LongParameterList")
private fun unfundedYear(
  year: Int,
  frozenDeflator: Double,
  flows: YearFlows,
  totalPlanned: Double,
  adjustmentFactor: Double,
  minimumThisYear: Double?,
  incomeAmounts: DoubleArray,
  potCount: Int,
  contributionCount: Int,
): McRunDetailRow {
  val incomeTowardsSpending = min(flows.spendableIncome, totalPlanned)
  val potFundedPlan = totalPlanned - incomeTowardsSpending
  var adjustedPlan = potFundedPlan * adjustmentFactor
  if (minimumThisYear != null && potFundedPlan > 0) {
    adjustedPlan = max(adjustedPlan, minimumThisYear - incomeTowardsSpending)
  }
  val potZeros = List(potCount) { 0L }.toImmutableList()
  return McRunDetailRow(
    year = year,
    afterDepletion = true,
    startBalance = 0,
    plannedSpending = emit(incomeTowardsSpending + max(0.0, adjustedPlan), frozenDeflator),
    // Only the income still reaches spending once the pots are gone
    spent = emit(incomeTowardsSpending, frozenDeflator),
    withdrawal = 0,
    growth = 0,
    endBalance = 0,
    potBalances = potZeros,
    potStartBalances = potZeros,
    inflation = null,
    income = emit(flows.grossIncome, frozenDeflator),
    incomeAmounts = incomeAmounts.map { emit(it, frozenDeflator) }.toImmutableList(),
    incomeTax = emit(flows.incomeTax, frozenDeflator),
    unspentIncome = emit(flows.spendableIncome - incomeTowardsSpending, frozenDeflator),
    surplusSaved = 0,
    contributions = 0,
    potContributions = potZeros,
    contributionAmounts = List(contributionCount) { 0L }.toImmutableList(),
    potWithdrawals = potZeros,
    potTaxes = potZeros,
    potTaxables = potZeros,
    taxPaid = 0,
    feesPaid = 0,
    potFees = potZeros,
    potReturns = List<Double?>(potCount) { null }.toImmutableList(),
  )
}

private fun DoubleArray?.orEmpty(): DoubleArray = this ?: DoubleArray(0)

// Per-pot scratch space for the captured run's year
private class CapturedPots(potCount: Int) {
  val withdrawals = DoubleArray(potCount)
  val taxes = DoubleArray(potCount)
  val taxables = DoubleArray(potCount)
  val fees = DoubleArray(potCount)
  // NaN until the pot experiences a return
  val returns = DoubleArray(potCount) { Double.NaN }

  fun returnsOrNull(): List<Double?> = returns.map { it.takeUnless(Double::isNaN) }
}

private const val HALF = 0.5
private const val P5 = 0.05
private const val P10 = 0.1
private const val P25 = 0.25
private const val P30 = 0.3
private const val P50 = 0.5
private const val P70 = 0.7
private const val P75 = 0.75
private const val P90 = 0.9
