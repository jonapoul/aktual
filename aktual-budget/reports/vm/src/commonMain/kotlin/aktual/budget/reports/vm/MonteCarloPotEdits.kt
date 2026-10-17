package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import kotlin.math.min
import kotlinx.collections.immutable.toImmutableList

// The pot edits behind upstream's MonteCarloConfiguration.tsx and MonteCarloPotConfiguration.tsx

val ReturnModel.isHistorical: Boolean
  get() = this == HistoricalBootstrap || this == HistoricalSequence

// Whether unspent money is kept is simply whether a surplus pot exists
val McConfig.keepsSurplus: Boolean
  get() = pots.any { it.isSurplus }

// The surplus pot is managed by the surplus toggle, and the plan always keeps an ordinary pot
fun McConfig.canRemove(pot: McPot): Boolean = !pot.isSurplus && pots.count { !it.isSurplus } > 1

// onKeepSurplusChange(): the surplus pot goes first, matching the order it's drawn on. Removing it
// takes its contributions with it
fun McConfig.withSurplusKept(keep: Boolean, newId: () -> String): McConfig =
  when {
    keep == keepsSurplus -> {
      this
    }

    keep -> {
      copy(pots = (listOf(surplusPot(newId())) + pots).toImmutableList())
    }

    else -> {
      val surplusIds = pots.filter { it.isSurplus }.map { it.id }.toSet()
      val remaining = pots.filterNot { it.isSurplus }.ifEmpty { listOf(McPot(id = newId())) }
      copy(
        pots = remaining.toImmutableList(),
        contributions = contributions.filterNot { it.potId in surplusIds }.toImmutableList(),
      )
    }
  }

fun McConfig.addPot(id: String): McConfig = copy(pots = (pots + McPot(id = id)).toImmutableList())

fun McConfig.updatePot(id: String, transform: (McPot) -> McPot): McConfig =
  copy(pots = pots.map { if (it.id == id) transform(it) else it }.toImmutableList())

// A removed pot takes its contributions with it
fun McConfig.removePot(id: String): McConfig =
  copy(
    pots = pots.filterNot { it.id == id }.toImmutableList(),
    contributions = contributions.filterNot { it.potId == id }.toImmutableList(),
  )

// Moves a pot up (negative) or down the list, which is the order pots are drained in
fun McConfig.movePot(id: String, offset: Int): McConfig {
  val from = pots.indexOfFirst { it.id == id }
  if (from < 0) return this
  val to = (from + offset).coerceIn(0, pots.lastIndex)
  if (to == from) return this
  val moved = pots.toMutableList()
  moved.add(to, moved.removeAt(from))
  return copy(pots = moved.toImmutableList())
}

// Typing a balance takes manual control: the pot unlinks from its account
fun McPot.withStartingBalance(balance: Double): McPot =
  copy(startingBalance = clampAmount(balance), accountId = null)

// Defaults the pot's name to the account's, but only when the current name isn't the user's own:
// empty, or still the name of the previously linked account
fun McPot.withLinkedAccount(accountId: AccountId?, accountNames: Map<AccountId, String>): McPot {
  val newName = accountId?.let(accountNames::get)
  val previousName = this.accountId?.let(accountNames::get)
  val fillsName = newName != null && (name.isEmpty() || name == previousName)
  return copy(accountId = accountId, name = if (fillsName) newName else name)
}

// getAllocationChange(), for a pot or one of its investment phases
fun <T : McInvestment<T>> T.withAllocationPreset(preset: AllocationPreset): T {
  if (preset == CustomMix) {
    // Seed the mix from the preset being left, so tweaking a preset starts from its shares
    val weights = if (allocationPreset == CustomMix) null else assetWeights()
    return withInvestment(
      allocationPreset = preset,
      allocationStocks = weights?.stocks ?: allocationStocks,
      allocationBonds = weights?.bonds ?: allocationBonds,
      allocationCash = weights?.cash ?: allocationCash,
    )
  }
  val stats = preset.presetStats ?: return withInvestment(allocationPreset = preset)
  return withInvestment(
    allocationPreset = preset,
    expectedReturnMean = stats.mean,
    returnStdDev = stats.stdDev,
  )
}

fun <T : McInvestment<T>> T.withExpectedReturn(mean: Double): T =
  withInvestment(expectedReturnMean = mean, allocationPreset = detachedPreset())

fun <T : McInvestment<T>> T.withVolatility(stdDev: Double): T =
  withInvestment(returnStdDev = stdDev, allocationPreset = detachedPreset())

// Typing a value only detaches a real preset. A custom mix keeps its mix, since these are its
// random-model inputs
private fun McInvestment<*>.detachedPreset(): AllocationPreset =
  if (allocationPreset == CustomMix) CustomMix else Custom

// Once a phase starts at the plan's end there is nowhere later to put another one
fun McPot.canAddInvestmentPhase(config: McConfig): Boolean =
  upcomingPhases(config.currentAge).lastOrNull().let { it == null || it.fromAge < config.targetAge }

// Starts from the latest phase so only the differences need changing, ten years on (or at the
// plan's end if that's sooner)
fun McPot.withInvestmentPhaseAdded(config: McConfig, id: String): McPot {
  val latest = upcomingPhases(config.currentAge).lastOrNull()
  val fromAge = min(config.targetAge, (latest?.fromAge ?: config.currentAge) + PHASE_GAP_YEARS)
  return copy(
    investmentPhases =
      (investmentPhases + investmentPhase(id, latest ?: this, fromAge)).toImmutableList(),
  )
}

fun McPot.updateInvestmentPhase(
  id: String,
  transform: (McInvestmentPhase) -> McInvestmentPhase,
): McPot =
  copy(
    investmentPhases =
      investmentPhases.map { if (it.id == id) transform(it) else it }.toImmutableList(),
  )

fun McPot.removeInvestmentPhase(id: String): McPot =
  copy(investmentPhases = investmentPhases.filterNot { it.id == id }.toImmutableList())

private const val PHASE_GAP_YEARS = 10

// Historical models take real blended returns for pots with an asset mix, so those show what the
// mix actually measured rather than an ignored assumption
fun McInvestment<*>.historicalStats(returnModel: ReturnModel): ReturnStats? =
  if (returnModel.isHistorical) assetWeights()?.let(::historicalMixStats) else null
