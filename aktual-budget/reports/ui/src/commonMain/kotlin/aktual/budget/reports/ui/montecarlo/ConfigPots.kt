package aktual.budget.reports.ui.montecarlo

import aktual.budget.model.AccountId
import aktual.budget.reports.vm.AllocationPreset
import aktual.budget.reports.vm.MC_MAX_AGE
import aktual.budget.reports.vm.MC_MAX_ANNUAL_FEE_RATE
import aktual.budget.reports.vm.MC_MAX_WITHDRAWAL_TAX_RATE
import aktual.budget.reports.vm.MC_MIN_AGE
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McPot
import aktual.budget.reports.vm.ReturnStats
import aktual.budget.reports.vm.canRemove
import aktual.budget.reports.vm.historicalStats
import aktual.budget.reports.vm.isMixIncomplete
import aktual.budget.reports.vm.mixShareTotal
import aktual.budget.reports.vm.montecarlo.MonteCarloAccount
import aktual.budget.reports.vm.movePot
import aktual.budget.reports.vm.removePot
import aktual.budget.reports.vm.updatePot
import aktual.budget.reports.vm.withAllocationPreset
import aktual.budget.reports.vm.withExpectedReturn
import aktual.budget.reports.vm.withLinkedAccount
import aktual.budget.reports.vm.withStartingBalance
import aktual.budget.reports.vm.withVolatility
import aktual.core.icons.material.ArrowDropDown
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.NormalTextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// The pots list from MonteCarloConfiguration.tsx
@Composable
internal fun PotsTab(
  config: McConfig,
  accounts: ImmutableList<MonteCarloAccount>,
  onAction: MonteCarloActionHandler,
) {
  config.pots.fastForEachIndexed { index, pot ->
    key(pot.id) {
      if (pot.isSurplus) {
        SurplusPotCard(config, pot, index, onAction)
      } else {
        PotCard(config, pot, index, accounts, onAction)
      }
    }
  }

  NormalTextButton(text = Strings.monteCarloPotAdd, onClick = { onAction(AddPot) })
}

// The surplus pot holds whatever the plan didn't spend, as cash, immediately accessible, untaxed
// and fee-free, so it's read-only
@Composable
private fun SurplusPotCard(
  config: McConfig,
  pot: McPot,
  index: Int,
  onAction: MonteCarloActionHandler,
) =
  ItemCard(
    title = potLabel(config.pots, index),
    titleHelp = Strings.monteCarloHelpSurplusPot,
    actions = { MovePotButtons(config, pot, index, onAction) },
  ) {
    FieldRow {
      LabeledField(Strings.monteCarloFieldStartingBalance) {
        ReadOnlyValue(Strings.monteCarloSurplusStartsEmpty)
      }
      LabeledField(Strings.monteCarloFieldLinkedAccount) {
        ReadOnlyValue(Strings.monteCarloPlaceholderNone)
      }
      LabeledField(Strings.monteCarloFieldAllocation) {
        ReadOnlyValue(Strings.monteCarloPresetCash)
      }
      val stats = pot.historicalStats(config.returnModel)
      LabeledField(Strings.monteCarloFieldExpectedReturn) {
        ReadOnlyValue(returnText(stats?.mean, pot.expectedReturnMean))
      }
      LabeledField(Strings.monteCarloFieldVolatility) {
        ReadOnlyValue(returnText(stats?.stdDev, pot.returnStdDev))
      }
    }
  }

// MonteCarloPotConfiguration.tsx
@Composable
private fun PotCard(
  config: McConfig,
  pot: McPot,
  index: Int,
  accounts: ImmutableList<MonteCarloAccount>,
  onAction: MonteCarloActionHandler,
) {
  var isExpanded by rememberSaveable { mutableStateOf(false) }
  val label = potLabel(config.pots, index)
  val edit: ((McPot) -> McPot) -> Unit = { transform ->
    onAction.edit { it.updatePot(pot.id, transform) }
  }

  ItemCard(
    title = label,
    leading = {
      BareIconButton(
        imageVector = if (isExpanded) MaterialIcons.ArrowDropDown else MaterialIcons.ArrowRight,
        contentDescription =
          if (isExpanded) Strings.monteCarloPotHideMore else Strings.monteCarloPotShowMore,
        onClick = { isExpanded = !isExpanded },
      )
    },
    actions = {
      MovePotButtons(config, pot, index, onAction)
      if (config.canRemove(pot)) {
        BareIconButton(
          imageVector = MaterialIcons.Delete,
          contentDescription = Strings.monteCarloPotRemove,
          onClick = { onAction.edit { it.removePot(pot.id) } },
        )
      }
    },
  ) {
    LabeledField(Strings.monteCarloFieldPotName) {
      NameField(
        name = pot.name,
        placeholder = label,
        onCommit = { name -> edit { it.copy(name = name) } },
      )
    }

    FieldRow {
      LabeledField(Strings.monteCarloFieldStartingBalance) {
        AmountField(
          amount = pot.startingBalance,
          onCommit = { balance -> edit { it.withStartingBalance(balance) } },
        )
      }
      LabeledField(
        label = Strings.monteCarloFieldLinkedAccount,
        help = Strings.monteCarloHelpLinkedAccount,
      ) {
        LinkedAccountPicker(
          pot,
          accounts,
          onSelect = { id, names -> edit { it.withLinkedAccount(id, names) } },
        )
      }
    }

    LabeledField(
      label = Strings.monteCarloFieldAllocation,
      help = Strings.monteCarloHelpAllocation,
    ) {
      AktualExposedDropDownMenu(
        value = pot.allocationPreset,
        onValueChange = { preset ->
          edit { it.withAllocationPreset(preset) }
          // The mix's share fields live in the expanded settings
          if (preset == CustomMix) isExpanded = true
        },
        options = ALLOCATION_PRESETS,
        string = { it.string() },
      )
    }

    ReturnFields(pot, pot.historicalStats(config.returnModel), edit)

    if (isExpanded) {
      PotSettings(pot, usesTaxBands = config.taxModel == Bands, edit = edit)
    }
  }
}

// Historical models derive the pot's returns from its allocation mix, so pots with one show the
// mix's measured history instead of editable assumptions
@Composable
private fun ReturnFields(pot: McPot, stats: ReturnStats?, edit: ((McPot) -> McPot) -> Unit) =
  FieldRow {
    LabeledField(
      label = Strings.monteCarloFieldExpectedReturn,
      help = Strings.monteCarloHelpExpectedReturn,
    ) {
      if (stats != null) {
        ReadOnlyValue(returnText(stats.mean, pot.expectedReturnMean))
      } else {
        PercentField(
          value = pot.expectedReturnMean,
          min = MIN_RETURN,
          onCommit = { mean -> edit { it.withExpectedReturn(mean) } },
        )
      }
    }
    LabeledField(
      label = Strings.monteCarloFieldVolatility,
      help = Strings.monteCarloHelpVolatility,
    ) {
      if (stats != null) {
        ReadOnlyValue(returnText(stats.stdDev, pot.returnStdDev))
      } else {
        PercentField(
          value = pot.returnStdDev,
          onCommit = { stdDev -> edit { it.withVolatility(stdDev) } },
        )
      }
    }
  }

// The access, tax and fee settings upstream keeps in the pot's expandable panel
@Composable
private fun PotSettings(pot: McPot, usesTaxBands: Boolean, edit: ((McPot) -> McPot) -> Unit) {
  if (pot.allocationPreset == CustomMix) {
    GroupHeadingWithHelp(Strings.monteCarloGroupAllocation, Strings.monteCarloHelpMix)
    FieldRow {
      LabeledField(Strings.monteCarloFieldStocks) {
        PercentField(
          value = pot.allocationStocks,
          onCommit = { share -> edit { it.copy(allocationStocks = share) } },
        )
      }
      LabeledField(Strings.monteCarloFieldBonds) {
        PercentField(
          value = pot.allocationBonds,
          onCommit = { share -> edit { it.copy(allocationBonds = share) } },
        )
      }
      LabeledField(Strings.monteCarloFieldCash) {
        PercentField(
          value = pot.allocationCash,
          onCommit = { share -> edit { it.copy(allocationCash = share) } },
        )
      }
    }
    if (pot.isMixIncomplete()) {
      Text(
        text =
          Strings.monteCarloMixIncomplete(
            formatTrimmedPercent(1.0, places = 0),
            formatTrimmedPercent(pot.mixShareTotal(), places = MIX_TOTAL_PLACES),
          ),
        style = typography.bodyMedium,
        color = colors.errorText,
      )
    }
  }

  GroupHeading(Strings.monteCarloGroupAccess)
  LabeledField(label = Strings.monteCarloFieldAccessAge, help = Strings.monteCarloHelpAccessAge) {
    OptionalIntField(
      value = pot.accessAge,
      min = MC_MIN_AGE,
      max = MC_MAX_AGE,
      placeholder = Strings.monteCarloPlaceholderImmediately,
      onCommit = { age -> edit { it.copy(accessAge = age) } },
    )
  }

  GroupHeading(Strings.monteCarloTabTax)
  if (usesTaxBands) {
    LabeledField(
      label = Strings.monteCarloFieldTaxablePortion,
      help = Strings.monteCarloHelpPotTaxablePortion,
    ) {
      PercentField(
        value = pot.taxableFraction,
        onCommit = { fraction -> edit { it.copy(taxableFraction = fraction) } },
      )
    }
  } else {
    LabeledField(label = Strings.monteCarloFieldTax, help = Strings.monteCarloHelpPotTax) {
      PercentField(
        value = pot.withdrawalTaxRate,
        max = MC_MAX_WITHDRAWAL_TAX_RATE * PERCENT_SCALE,
        onCommit = { rate -> edit { it.copy(withdrawalTaxRate = rate) } },
      )
    }
  }

  GroupHeading(Strings.monteCarloGroupFees)
  FieldRow {
    LabeledField(label = Strings.monteCarloFieldFeeFixed, help = Strings.monteCarloHelpFeeFixed) {
      AmountField(
        amount = pot.annualFeeFixed,
        onCommit = { fee -> edit { it.copy(annualFeeFixed = fee) } },
      )
    }
    LabeledField(label = Strings.monteCarloFieldFeeRate, help = Strings.monteCarloHelpFeeRate) {
      PercentField(
        value = pot.annualFeeRate,
        max = MC_MAX_ANNUAL_FEE_RATE * PERCENT_SCALE,
        onCommit = { rate -> edit { it.copy(annualFeeRate = rate) } },
      )
    }
  }
  LabeledCheckbox(
    label = Strings.monteCarloAdjustByInflation,
    checked = pot.feeAdjustsWithInflation,
    onCheckedChange = { adjusts -> edit { it.copy(feeAdjustsWithInflation = adjusts) } },
  )
}

@Composable
private fun LinkedAccountPicker(
  pot: McPot,
  accounts: ImmutableList<MonteCarloAccount>,
  onSelect: (AccountId?, Map<AccountId, String>) -> Unit,
) {
  val names = remember(accounts) { accounts.associate { it.id to it.name } }
  // Budgeted accounts first, then off-budget, each keeping the order they're listed in. A linked
  // account that has since been closed or deleted stays in the list, so the stored link doesn't
  // show as a blank selection
  val options =
    remember(accounts, pot.accountId) {
      val open =
        accounts
          .asSequence()
          .filterNot { it.isClosed }
          .sortedBy { it.isOffBudget }
          .map { it.id }
          .toList()
      val missing = listOfNotNull(pot.accountId?.takeIf { it !in open })
      ([null] + missing + open).toImmutableList()
    }

  AktualExposedDropDownMenu(
    value = pot.accountId,
    onValueChange = { id -> onSelect(id, names) },
    options = options,
    string = { id ->
      val account = accounts.firstOrNull { it.id == id }
      when {
        id == null -> Strings.monteCarloPlaceholderNone
        account == null -> Strings.monteCarloAccountUnavailable
        account.isClosed -> Strings.monteCarloAccountClosed(account.name)
        else -> account.name
      }
    },
  )
}

// Pots are drained in the order they're listed, so they can be moved up and down
@Composable
private fun MovePotButtons(
  config: McConfig,
  pot: McPot,
  index: Int,
  onAction: MonteCarloActionHandler,
) {
  BareIconButton(
    modifier = Modifier.rotate(HALF_TURN),
    imageVector = MaterialIcons.ArrowDropDown,
    contentDescription = Strings.monteCarloPotMoveUp,
    enabled = index > 0,
    onClick = { onAction.edit { it.movePot(pot.id, offset = -1) } },
  )
  BareIconButton(
    imageVector = MaterialIcons.ArrowDropDown,
    contentDescription = Strings.monteCarloPotMoveDown,
    enabled = index < config.pots.lastIndex,
    onClick = { onAction.edit { it.movePot(pot.id, offset = 1) } },
  )
}

@Composable
private fun returnText(historical: Double?, assumed: Double): String =
  if (historical != null) {
    Strings.monteCarloHistoricalValue(formatPercent(historical, places = 1))
  } else {
    formatPercent(assumed, places = 1)
  }

private val ALLOCATION_PRESETS =
  persistentListOf<AllocationPreset>(
    Equity100,
    Equity80,
    Equity60,
    Equity40,
    Cash,
    CustomMix,
    Custom,
  )

private const val MIN_RETURN = -100.0
private const val MIX_TOTAL_PLACES = 2
private const val HALF_TURN = 180f
