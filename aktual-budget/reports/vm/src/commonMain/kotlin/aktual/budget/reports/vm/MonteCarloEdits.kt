package aktual.budget.reports.vm

import kotlin.math.max
import kotlin.math.min
import kotlinx.collections.immutable.toImmutableList

// The edit operations behind upstream's configuration editor, from MonteCarloConfiguration.tsx and
// the components next to it in
// packages/desktop-client/src/components/reports/reports/monte-carlo/

const val MC_MIN_AGE = 16
const val MC_MAX_AGE = 120
private const val NEW_PHASE_GAP_YEARS = 10
private const val NEW_BAND_GAP = 2_500_000.0

fun clampAmount(amount: Double): Double = amount.coerceIn(0.0, MC_MAX_AMOUNT)

// sortMonteCarloSpendingPhases(): by starting age, with the no-age phase first
fun List<McSpendingPhase>.sortedByAge(): List<McSpendingPhase> = sortedBy {
  it.fromAge ?: Int.MIN_VALUE
}

fun McConfig.addSpendingPhase(id: String): McConfig {
  val lastFrom = spendingPhases.lastOrNull()?.fromAge ?: currentAge
  val fromAge = max(currentAge + 1, min(targetAge - 1, lastFrom + NEW_PHASE_GAP_YEARS))
  val added = spendingPhases + McSpendingPhase(id = id, fromAge = fromAge)
  return copy(spendingPhases = added.sortedByAge().toImmutableList())
}

fun McConfig.updateSpendingPhase(
  id: String,
  transform: (McSpendingPhase) -> McSpendingPhase,
): McConfig =
  copy(
    spendingPhases =
      spendingPhases.map { if (it.id == id) transform(it) else it }.sortedByAge().toImmutableList(),
  )

// The first phase always starts immediately
fun McConfig.removeSpendingPhase(id: String): McConfig =
  copy(
    spendingPhases =
      spendingPhases
        .filterNot { it.id == id }
        .mapIndexed { i, phase -> if (i == 0) phase.copy(fromAge = null) else phase }
        .toImmutableList(),
  )

fun McConfig.addTaxBand(id: String): McConfig {
  val lastFrom = taxBands.lastOrNull()?.from ?: 0.0
  val added = taxBands + McTaxBand(id = id, from = lastFrom + NEW_BAND_GAP)
  return copy(taxBands = added.sortedBy { it.from }.toImmutableList())
}

fun McConfig.updateTaxBand(id: String, transform: (McTaxBand) -> McTaxBand): McConfig =
  copy(
    taxBands =
      taxBands
        .map { if (it.id == id) transform(it) else it }
        .sortedBy { it.from }
        .toImmutableList(),
  )

// The first band always starts at zero income
fun McConfig.removeTaxBand(id: String): McConfig =
  copy(
    taxBands =
      taxBands
        .filterNot { it.id == id }
        .mapIndexed { i, band -> if (i == 0) band.copy(from = 0.0) else band }
        .toImmutableList(),
  )

// The surplus pot is managed by the plan, so a new contribution starts on an ordinary one
fun McConfig.addContribution(id: String): McConfig {
  val pot = pots.firstOrNull { !it.isSurplus } ?: pots.firstOrNull() ?: return this
  return copy(
    contributions = (contributions + McContribution(id = id, potId = pot.id)).toImmutableList(),
  )
}

fun McConfig.updateContribution(
  id: String,
  transform: (McContribution) -> McContribution,
): McConfig =
  copy(
    contributions = contributions.map { if (it.id == id) transform(it) else it }.toImmutableList(),
  )

fun McConfig.removeContribution(id: String): McConfig =
  copy(contributions = contributions.filterNot { it.id == id }.toImmutableList())

// Before tax only means something for a contribution paid from income
fun McContribution.withSource(incomeStreamId: String?): McContribution =
  copy(sourceIncomeStreamId = incomeStreamId, beforeTax = beforeTax && incomeStreamId != null)

// A contribution paid from an income stream can only ever take what the stream brings in. Returns
// that stream when the contribution asks for more
fun McConfig.exceededSource(contribution: McContribution): McIncomeStream? =
  incomeStreams
    .firstOrNull { it.id == contribution.sourceIncomeStreamId }
    ?.takeIf { contribution.annualAmount > it.annualAmount }

fun McConfig.addIncomeStream(id: String): McConfig =
  copy(incomeStreams = (incomeStreams + McIncomeStream(id = id)).toImmutableList())

fun McConfig.updateIncomeStream(
  id: String,
  transform: (McIncomeStream) -> McIncomeStream,
): McConfig =
  copy(
    incomeStreams = incomeStreams.map { if (it.id == id) transform(it) else it }.toImmutableList(),
  )

// Contributions paid out of a removed stream fall back to money from outside the plan
fun McConfig.removeIncomeStream(id: String): McConfig =
  copy(
    incomeStreams = incomeStreams.filterNot { it.id == id }.toImmutableList(),
    contributions =
      contributions
        .map { if (it.sourceIncomeStreamId == id) it.withSource(null) else it }
        .toImmutableList(),
  )
