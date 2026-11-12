package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.MC_MAX_WITHDRAWAL_TAX_RATE
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McLumpSum
import aktual.budget.reports.vm.McPot
import aktual.budget.reports.vm.removeLumpSum
import aktual.budget.reports.vm.surplusPot
import aktual.budget.reports.vm.updateLumpSum
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Trash
import aktual.core.l10n.Strings
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// MonteCarloLumpSums.tsx
@Composable
internal fun LumpSumList(config: McConfig, onAction: OnAction<MonteCarloAction>) {
  GroupHeadingWithHelp(Strings.monteCarloGroupLumpSums, Strings.monteCarloHelpLumpSums)
  config.lumpSums.fastForEachIndexed { index, lumpSum ->
    key(lumpSum.id) { LumpSumCard(config, lumpSum, index, onAction) }
  }
  AktualButton(
    kind = Tonal,
    icon = AktualIcons.Add,
    text = Strings.monteCarloLumpSumAdd,
    onClick = { onAction(AddLumpSum) },
  )
}

@Composable
private fun LumpSumCard(
  config: McConfig,
  lumpSum: McLumpSum,
  index: Int,
  onAction: OnAction<MonteCarloAction>,
) {
  val label = lumpSum.name.ifEmpty { Strings.monteCarloLumpSumNumbered(index + 1) }
  val edit: ((McLumpSum) -> McLumpSum) -> Unit = { transform ->
    onAction.edit { it.updateLumpSum(lumpSum.id, transform) }
  }
  val lastAge = config.lastPlanAge

  ItemCard(
    title = label,
    actions = {
      AktualIconButton(
        imageVector = AktualIcons.Trash,
        contentDescription = Strings.monteCarloLumpSumRemove,
        onClick = { onAction.edit { it.removeLumpSum(lumpSum.id) } },
      )
    },
  ) {
    LabeledField(Strings.monteCarloFieldLumpSumName) {
      NameField(
        name = lumpSum.name,
        placeholder = label,
        onCommit = { name -> edit { it.copy(name = name) } },
      )
    }

    FieldRow {
      LabeledField(Strings.monteCarloFieldAge) {
        IntField(
          value = lumpSum.age,
          min = config.currentAge,
          max = lastAge,
          onCommit = { age -> edit { it.copy(age = age) } },
        )
      }
      LabeledField(Strings.monteCarloFieldAmountAfterTax) {
        AmountField(
          amount = lumpSum.amount,
          onCommit = { amount -> edit { it.copy(amount = amount) } },
        )
      }
      if (config.taxModel == Bands) {
        LabeledField(Strings.monteCarloFieldTaxablePortion) {
          PercentField(
            value = lumpSum.taxableFraction,
            onCommit = { fraction -> edit { it.copy(taxableFraction = fraction) } },
          )
        }
      } else {
        LabeledField(Strings.monteCarloFieldTax) {
          PercentField(
            value = lumpSum.taxRate,
            max = MC_MAX_WITHDRAWAL_TAX_RATE * PERCENT_SCALE,
            onCommit = { rate -> edit { it.copy(taxRate = rate) } },
          )
        }
      }
    }

    // The surplus pot is drawn first by the order anyway, so it isn't offered as a named source
    val sourcePot = config.pots.firstOrNull { it.id == lumpSum.sourcePotId && !it.isSurplus }
    LabeledField(Strings.monteCarloFieldTakenFrom, help = Strings.monteCarloHelpTakenFrom) {
      val sources =
        remember(config.pots) {
          (listOf<String?>(null) + config.pots.filterNot { it.isSurplus }.map { it.id })
            .toImmutableList()
        }
      AktualExposedDropDownMenu(
        value = sourcePot?.id,
        onValueChange = { potId -> edit { it.copy(sourcePotId = potId) } },
        options = sources,
        string = { id ->
          val potIndex = config.pots.indexOfFirst { it.id == id }
          if (potIndex >= 0) potLabel(config.pots, potIndex) else Strings.monteCarloSourceOrder
        },
      )
    }

    LabeledCheckbox(
      label = Strings.monteCarloAdjustByInflation,
      checked = lumpSum.adjustsWithInflation,
      onCheckedChange = { adjusts -> edit { it.copy(adjustsWithInflation = adjusts) } },
    )

    // Ages can drift outside the plan when the current or target age changes
    val accessAge = sourcePot?.accessAge
    val warning =
      when {
        lumpSum.age < config.currentAge || lumpSum.age > lastAge ->
          Strings.monteCarloLumpSumOutsidePlan(lumpSum.age, config.currentAge, lastAge)
        accessAge != null && accessAge > lumpSum.age ->
          Strings.monteCarloLumpSumLocked(
            sourcePot.name.ifEmpty { Strings.monteCarloLumpSumThatPot },
            accessAge,
            lumpSum.age,
          )
        else -> null
      }
    warning?.let {
      Text(text = it, style = typography.bodySmall, color = colors.warningText)
    }
  }
}

@Preview
@Composable
private fun PreviewLumpSumList(
  @PreviewParameter(LumpSumsProvider::class) params: ColorParams<McConfig>,
) =
  PreviewWithColorParams(params) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      LumpSumList(config = this@PreviewWithColorParams, onAction = {})
    }
  }

private val PREVIEW_LUMP_SUMS =
  persistentListOf(
    McLumpSum(id = "lump-1", age = 62, name = "House deposit", sourcePotId = "pot-1"),
    McLumpSum(id = "lump-2", age = 95, amount = 500_000.0, taxRate = 0.2, taxableFraction = 0.75),
  )

private class LumpSumsProvider :
  ColorParamsProvider<McConfig>(
    McConfig(
      pots =
        persistentListOf(
          surplusPot("surplus"),
          McPot(id = "pot-1", name = "Pension", accessAge = 67),
          McPot(id = "pot-2"),
        ),
      lumpSums = PREVIEW_LUMP_SUMS,
    ),
    McConfig(taxModel = Bands, lumpSums = PREVIEW_LUMP_SUMS),
  )
