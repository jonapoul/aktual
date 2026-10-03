package aktual.budget.reports.vm

import aktual.budget.model.AccountId
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

fun McPot.withAllocationPreset(preset: AllocationPreset): McPot {
  if (preset == CustomMix) {
    // Seed the mix from the preset being left, so tweaking a preset starts from its shares
    val weights = if (allocationPreset == CustomMix) null else assetWeights()
    return copy(
      allocationPreset = preset,
      allocationStocks = weights?.stocks ?: allocationStocks,
      allocationBonds = weights?.bonds ?: allocationBonds,
      allocationCash = weights?.cash ?: allocationCash,
    )
  }
  val stats = preset.presetStats ?: return copy(allocationPreset = preset)
  return copy(
    allocationPreset = preset,
    expectedReturnMean = stats.mean,
    returnStdDev = stats.stdDev,
  )
}

fun McPot.withExpectedReturn(mean: Double): McPot =
  copy(expectedReturnMean = mean, allocationPreset = detachedPreset())

fun McPot.withVolatility(stdDev: Double): McPot =
  copy(returnStdDev = stdDev, allocationPreset = detachedPreset())

// Typing a value only detaches a real preset. A custom mix keeps its mix, since these are its
// random-model inputs
private fun McPot.detachedPreset(): AllocationPreset =
  if (allocationPreset == CustomMix) CustomMix else Custom

// Historical models take real blended returns for pots with an asset mix, so those show what the
// mix actually measured rather than an ignored assumption
fun McPot.historicalStats(returnModel: ReturnModel): ReturnStats? =
  if (returnModel.isHistorical) assetWeights()?.let(::historicalMixStats) else null
