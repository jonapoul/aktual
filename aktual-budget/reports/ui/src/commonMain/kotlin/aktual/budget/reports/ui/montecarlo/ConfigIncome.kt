package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.MC_MAX_WITHDRAWAL_TAX_RATE
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McContribution
import aktual.budget.reports.vm.McIncomeStream
import aktual.budget.reports.vm.exceededSource
import aktual.budget.reports.vm.removeContribution
import aktual.budget.reports.vm.removeIncomeStream
import aktual.budget.reports.vm.updateContribution
import aktual.budget.reports.vm.updateIncomeStream
import aktual.budget.reports.vm.withSource
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.roundToLong
import kotlinx.collections.immutable.toImmutableList

// MonteCarloIncomeStreams.tsx
@Composable
internal fun IncomeTab(config: McConfig, onAction: MonteCarloActionHandler) {
  config.incomeStreams.fastForEachIndexed { index, stream ->
    key(stream.id) { IncomeCard(config, stream, index, onAction) }
  }

  NormalTextButton(text = Strings.monteCarloIncomeAdd, onClick = { onAction(AddIncomeStream) })
}

@Composable
private fun IncomeCard(
  config: McConfig,
  stream: McIncomeStream,
  index: Int,
  onAction: MonteCarloActionHandler,
) {
  val label = incomeLabel(config.incomeStreams, index)
  val edit: ((McIncomeStream) -> McIncomeStream) -> Unit = { transform ->
    onAction.edit { it.updateIncomeStream(stream.id, transform) }
  }

  ItemCard(
    title = label,
    actions = {
      BareIconButton(
        imageVector = MaterialIcons.Delete,
        contentDescription = Strings.monteCarloIncomeRemove,
        onClick = { onAction.edit { it.removeIncomeStream(stream.id) } },
      )
    },
  ) {
    LabeledField(Strings.monteCarloFieldIncomeName) {
      NameField(
        name = stream.name,
        placeholder = label,
        onCommit = { name -> edit { it.copy(name = name) } },
      )
    }

    FieldRow {
      AgeWindowFields(
        config = config,
        fromAge = stream.fromAge,
        toAge = stream.toAge,
        onFromAge = { age -> edit { it.copy(fromAge = age) } },
        onToAge = { age -> edit { it.copy(toAge = age) } },
      )
      LabeledField(Strings.monteCarloFieldAmountYear) {
        AmountField(
          amount = stream.annualAmount,
          onCommit = { amount -> edit { it.copy(annualAmount = amount) } },
        )
      }
      if (config.taxModel == Bands) {
        LabeledField(Strings.monteCarloFieldTaxablePortion) {
          PercentField(
            value = stream.taxableFraction,
            onCommit = { fraction -> edit { it.copy(taxableFraction = fraction) } },
          )
        }
      } else {
        LabeledField(Strings.monteCarloFieldTax) {
          PercentField(
            value = stream.taxRate,
            max = MC_MAX_WITHDRAWAL_TAX_RATE * PERCENT_SCALE,
            onCommit = { rate -> edit { it.copy(taxRate = rate) } },
          )
        }
      }
    }

    LabeledCheckbox(
      label = Strings.monteCarloAdjustByInflation,
      checked = stream.adjustsWithInflation,
      onCheckedChange = { adjusts -> edit { it.copy(adjustsWithInflation = adjusts) } },
    )
  }
}

// MonteCarloContributions.tsx
@Composable
internal fun ContributionsTab(config: McConfig, onAction: MonteCarloActionHandler) {
  config.contributions.fastForEachIndexed { index, contribution ->
    key(contribution.id) { ContributionCard(config, contribution, index, onAction) }
  }

  NormalTextButton(
    text = Strings.monteCarloContributionAdd,
    onClick = { onAction(AddContribution) },
  )
}

@Composable
private fun ContributionCard(
  config: McConfig,
  contribution: McContribution,
  index: Int,
  onAction: MonteCarloActionHandler,
) {
  val label = contribution.name.ifEmpty { Strings.monteCarloContributionNumbered(index + 1) }
  val edit: ((McContribution) -> McContribution) -> Unit = { transform ->
    onAction.edit { it.updateContribution(contribution.id, transform) }
  }

  ItemCard(
    title = label,
    actions = {
      BareIconButton(
        imageVector = MaterialIcons.Delete,
        contentDescription = Strings.monteCarloContributionRemove,
        onClick = { onAction.edit { it.removeContribution(contribution.id) } },
      )
    },
  ) {
    LabeledField(Strings.monteCarloFieldContributionName) {
      NameField(
        name = contribution.name,
        placeholder = label,
        onCommit = { name -> edit { it.copy(name = name) } },
      )
    }

    LabeledField(Strings.monteCarloFieldIntoPot) {
      val potIds = remember(config.pots) { config.pots.map { it.id }.toImmutableList() }
      AktualExposedDropDownMenu(
        value = contribution.potId,
        onValueChange = { potId -> edit { it.copy(potId = potId) } },
        options = potIds,
        string = { id ->
          val potIndex = config.pots.indexOfFirst { it.id == id }
          if (potIndex >= 0) potLabel(config.pots, potIndex) else ""
        },
      )
    }

    LabeledField(Strings.monteCarloFieldPaidFrom) {
      val sources =
        remember(config.incomeStreams) {
          (listOf<String?>(null) + config.incomeStreams.map { it.id }).toImmutableList()
        }
      AktualExposedDropDownMenu(
        value = contribution.sourceIncomeStreamId,
        onValueChange = { streamId -> edit { it.withSource(streamId) } },
        options = sources,
        string = { id ->
          val streamIndex = config.incomeStreams.indexOfFirst { it.id == id }
          if (streamIndex >= 0) {
            incomeLabel(config.incomeStreams, streamIndex)
          } else {
            Strings.monteCarloSourceOutside
          }
        },
      )
    }

    FieldRow {
      AgeWindowFields(
        config = config,
        fromAge = contribution.fromAge,
        toAge = contribution.toAge,
        onFromAge = { age -> edit { it.copy(fromAge = age) } },
        onToAge = { age -> edit { it.copy(toAge = age) } },
      )
      LabeledField(Strings.monteCarloFieldAmountYear) {
        AmountField(
          amount = contribution.annualAmount,
          onCommit = { amount -> edit { it.copy(annualAmount = amount) } },
        )
      }
    }

    LabeledCheckbox(
      label = Strings.monteCarloAdjustByInflation,
      checked = contribution.adjustsWithInflation,
      onCheckedChange = { adjusts -> edit { it.copy(adjustsWithInflation = adjusts) } },
    )

    // Before tax only means something for a contribution paid from income
    if (contribution.sourceIncomeStreamId != null) {
      LabeledCheckbox(
        label = Strings.monteCarloBeforeTax,
        help = Strings.monteCarloHelpBeforeTax,
        checked = contribution.beforeTax,
        onCheckedChange = { beforeTax -> edit { it.copy(beforeTax = beforeTax) } },
      )
    }

    config.exceededSource(contribution)?.let { stream ->
      Text(
        text =
          Strings.monteCarloContributionExceeds(
            incomeLabel(config.incomeStreams, config.incomeStreams.indexOf(stream)),
            stream.annualAmount.roundToLong().money(),
          ),
        style = typography.bodySmall,
        color = colors.warningText,
      )
    }
  }
}

// Both ages are inclusive, and blank means now and the end of the plan
@Composable
private fun AgeWindowFields(
  config: McConfig,
  fromAge: Int?,
  toAge: Int?,
  onFromAge: (Int?) -> Unit,
  onToAge: (Int?) -> Unit,
) {
  LabeledField(Strings.monteCarloFieldFromAge) {
    OptionalIntField(
      value = fromAge,
      min = config.currentAge,
      max = toAge ?: config.targetAge,
      placeholder = Strings.monteCarloPlaceholderNow,
      onCommit = onFromAge,
    )
  }
  LabeledField(Strings.monteCarloFieldToAge) {
    OptionalIntField(
      value = toAge,
      min = fromAge ?: config.currentAge,
      max = config.targetAge,
      placeholder = Strings.monteCarloPlaceholderEnd,
      onCommit = onToAge,
    )
  }
}
