package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf

class MonteCarloEditsTest {
  @Test
  fun `Dropping the surplus pot takes its contributions with it`() {
    val config =
      McConfig(
        pots = persistentListOf(surplusPot("surplus"), McPot(id = "a")),
        contributions =
          persistentListOf(
            McContribution(id = "c1", potId = "surplus"),
            McContribution(id = "c2", potId = "a"),
          ),
      )

    val edited = config.withSurplusKept(keep = false) { "new" }

    assertThat(edited).all {
      prop(McConfig::keepsSurplus).isFalse()
      transform { it.pots.map(McPot::id) }.containsExactly("a")
      transform { it.contributions.map(McContribution::id) }.containsExactly("c2")
    }
  }

  @Test
  fun `Dropping the surplus pot always leaves a pot`() {
    val config = McConfig(pots = persistentListOf(surplusPot("surplus")))
    val edited = config.withSurplusKept(keep = false) { "new" }
    assertThat(edited.pots).containsExactly(McPot(id = "new"))
  }

  @Test
  fun `Keeping the surplus puts its pot first`() {
    val config = McConfig(pots = persistentListOf(McPot(id = "a")))
    val edited = config.withSurplusKept(keep = true) { "new" }
    assertThat(edited.pots).containsExactly(surplusPot("new"), McPot(id = "a"))
  }

  @Test
  fun `Removing a pot takes its contributions with it`() {
    val config =
      McConfig(
        pots = persistentListOf(McPot(id = "a"), McPot(id = "b")),
        contributions =
          persistentListOf(
            McContribution(id = "c1", potId = "a"),
            McContribution(id = "c2", potId = "b"),
          ),
      )

    val edited = config.removePot("a")

    assertThat(edited).all {
      transform { it.pots.map(McPot::id) }.containsExactly("b")
      transform { it.contributions.map(McContribution::id) }.containsExactly("c2")
    }
  }

  @Test
  fun `Only ordinary pots can be removed, and never the last`() {
    val surplus = surplusPot("surplus")
    val a = McPot(id = "a")
    val b = McPot(id = "b")
    val two = McConfig(pots = persistentListOf(surplus, a, b))
    val one = McConfig(pots = persistentListOf(surplus, a))

    assertThat(two.canRemove(a)).isTrue()
    assertThat(two.canRemove(surplus)).isFalse()
    assertThat(one.canRemove(a)).isFalse()
  }

  @Test
  fun `Moving a pot stays inside the list`() {
    val config = McConfig(pots = persistentListOf(McPot(id = "a"), McPot(id = "b"), McPot("c")))

    assertThat(config.movePot("a", offset = 1).pots.map(McPot::id)).containsExactly("b", "a", "c")
    assertThat(config.movePot("c", offset = -1).pots.map(McPot::id)).containsExactly("a", "c", "b")
    assertThat(config.movePot("a", offset = -1)).isEqualTo(config)
    assertThat(config.movePot("c", offset = 1)).isEqualTo(config)
  }

  @Test
  fun `Typing a balance unlinks the account`() {
    val pot = McPot(id = "a", startingBalance = 100.0, accountId = ACCOUNT_1)

    assertThat(pot.withStartingBalance(200.0)).all {
      prop(McPot::startingBalance).isEqualTo(200.0)
      prop(McPot::accountId).isNull()
    }
    assertThat(pot.withStartingBalance(-5.0).startingBalance).isEqualTo(0.0)
  }

  @Test
  fun `Linking an account fills in a name that isn't the user's own`() {
    val unnamed = McPot(id = "a")
    val autoNamed = McPot(id = "a", name = "Savings", accountId = ACCOUNT_1)
    val named = McPot(id = "a", name = "My pot", accountId = ACCOUNT_1)

    assertThat(unnamed.withLinkedAccount(ACCOUNT_1, NAMES).name).isEqualTo("Savings")
    assertThat(autoNamed.withLinkedAccount(ACCOUNT_2, NAMES).name).isEqualTo("Pension")
    assertThat(named.withLinkedAccount(ACCOUNT_2, NAMES).name).isEqualTo("My pot")
    assertThat(autoNamed.withLinkedAccount(null, NAMES)).all {
      prop(McPot::name).isEqualTo("Savings")
      prop(McPot::accountId).isNull()
    }
  }

  @Test
  fun `A preset fills in its return and volatility`() {
    val pot = McPot(id = "a").withAllocationPreset(Equity100)
    assertThat(pot).all {
      prop(McPot::allocationPreset).isEqualTo(Equity100)
      prop(McPot::expectedReturnMean).isEqualTo(0.07)
      prop(McPot::returnStdDev).isEqualTo(0.15)
    }
  }

  @Test
  fun `A custom mix is seeded from the preset being left`() {
    val pot = McPot(id = "a").withAllocationPreset(Equity80).withAllocationPreset(CustomMix)
    assertThat(pot).all {
      prop(McPot::allocationPreset).isEqualTo(CustomMix)
      prop(McPot::allocationStocks).isEqualTo(0.8)
      prop(McPot::allocationBonds).isEqualTo(0.2)
      prop(McPot::allocationCash).isEqualTo(0.0)
      // The random-model inputs are kept
      prop(McPot::expectedReturnMean).isEqualTo(0.065)
    }
  }

  @Test
  fun `Typing a return detaches a preset but not a custom mix`() {
    val preset = McPot(id = "a", allocationPreset = Equity60)
    val mix = McPot(id = "a", allocationPreset = CustomMix)

    assertThat(preset.withExpectedReturn(0.05).allocationPreset).isEqualTo(Custom)
    assertThat(preset.withVolatility(0.05).allocationPreset).isEqualTo(Custom)
    assertThat(mix.withExpectedReturn(0.05).allocationPreset).isEqualTo(CustomMix)
  }

  @Test
  fun `Historical stats only show for pots with a mix under a historical model`() {
    val preset = McPot(id = "a", allocationPreset = Equity60)
    val custom = McPot(id = "a", allocationPreset = Custom)

    assertThat(preset.historicalStats(Normal)).isNull()
    assertThat(preset.historicalStats(HistoricalBootstrap)).isNotNull()
    assertThat(custom.historicalStats(HistoricalSequence)).isNull()
  }

  @Test
  fun `A new phase starts ten years after the last, inside the plan`() {
    val config = McConfig(currentAge = 60, targetAge = 90)

    val one = config.addSpendingPhase("p2")
    val two = one.addSpendingPhase("p3")
    val three = two.addSpendingPhase("p4")

    assertThat(one.spendingPhases.map { it.fromAge }).containsExactly(null, 70)
    assertThat(two.spendingPhases.map { it.fromAge }).containsExactly(null, 70, 80)
    assertThat(three.spendingPhases.map { it.fromAge }).containsExactly(null, 70, 80, 89)
  }

  @Test
  fun `Phases stay sorted by age when edited`() {
    val config =
      McConfig(
        spendingPhases =
          persistentListOf(
            McSpendingPhase(id = "p1"),
            McSpendingPhase(id = "p2", fromAge = 70),
            McSpendingPhase(id = "p3", fromAge = 80),
          )
      )

    val edited = config.updateSpendingPhase("p3") { it.copy(fromAge = 65) }

    assertThat(edited.spendingPhases.map { it.id }).containsExactly("p1", "p3", "p2")
  }

  @Test
  fun `The first phase always starts now`() {
    val config =
      McConfig(
        spendingPhases =
          persistentListOf(McSpendingPhase(id = "p1"), McSpendingPhase(id = "p2", fromAge = 70))
      )

    val edited = config.removeSpendingPhase("p1")

    assertThat(edited.spendingPhases).containsExactly(McSpendingPhase(id = "p2", fromAge = null))
  }

  @Test
  fun `Tax bands stay sorted, with the first starting at zero`() {
    val config = McConfig().addTaxBand("b2").addTaxBand("b3")
    assertThat(config.taxBands.map { it.from }).containsExactly(0.0, 2_500_000.0, 5_000_000.0)

    val reordered = config.updateTaxBand("b3") { it.copy(from = 1_000_000.0) }
    assertThat(reordered.taxBands.map { it.id }).containsExactly("band-1", "b3", "b2")

    val removed = reordered.removeTaxBand("band-1")
    assertThat(removed.taxBands.map { it.from }).containsExactly(0.0, 2_500_000.0)
  }

  @Test
  fun `A new contribution pays into the first ordinary pot`() {
    val config = McConfig(pots = persistentListOf(surplusPot("surplus"), McPot(id = "a")))
    val edited = config.addContribution("c1")
    assertThat(edited.contributions).containsExactly(McContribution(id = "c1", potId = "a"))
  }

  @Test
  fun `Paying from outside the plan clears before tax`() {
    val contribution =
      McContribution(id = "c1", potId = "a", sourceIncomeStreamId = "i1", beforeTax = true)

    assertThat(contribution.withSource("i2").beforeTax).isTrue()
    assertThat(contribution.withSource(null)).all {
      prop(McContribution::sourceIncomeStreamId).isNull()
      prop(McContribution::beforeTax).isFalse()
    }
  }

  @Test
  fun `Removing a stream moves its contributions outside the plan`() {
    val config =
      McConfig(
        incomeStreams = persistentListOf(McIncomeStream(id = "i1"), McIncomeStream(id = "i2")),
        contributions =
          persistentListOf(
            McContribution(id = "c1", potId = "a", sourceIncomeStreamId = "i1", beforeTax = true),
            McContribution(id = "c2", potId = "a", sourceIncomeStreamId = "i2", beforeTax = true),
          ),
      )

    val edited = config.removeIncomeStream("i1")

    assertThat(edited).all {
      transform { it.incomeStreams.map(McIncomeStream::id) }.containsExactly("i2")
      prop(McConfig::contributions)
        .containsExactly(
          McContribution(id = "c1", potId = "a"),
          McContribution(id = "c2", potId = "a", sourceIncomeStreamId = "i2", beforeTax = true),
        )
    }
  }

  @Test
  fun `A contribution can't take more than its stream pays`() {
    val stream = McIncomeStream(id = "i1", annualAmount = 500.0)
    val config = McConfig(incomeStreams = persistentListOf(stream))
    val over = McContribution(id = "c1", potId = "a", sourceIncomeStreamId = "i1")
    val under = over.copy(annualAmount = 500.0)
    val outside = over.copy(sourceIncomeStreamId = null)

    assertThat(config.exceededSource(over)).isEqualTo(stream)
    assertThat(config.exceededSource(under)).isNull()
    assertThat(config.exceededSource(outside)).isNull()
  }

  @Test
  fun `Removing the last contribution leaves none`() {
    val config = McConfig().addContribution("c1").removeContribution("c1")
    assertThat(config.contributions).isEmpty()
  }

  private companion object {
    val ACCOUNT_1 = AccountId("account-1")
    val ACCOUNT_2 = AccountId("account-2")
    val NAMES = mapOf(ACCOUNT_1 to "Savings", ACCOUNT_2 to "Pension")
  }
}
