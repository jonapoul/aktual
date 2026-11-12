package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.ALL_SANKEY_CATEGORIES
import aktual.budget.reports.vm.CategorySort
import aktual.budget.reports.vm.DEFAULT_TOP_N_CATEGORIES
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.SankeyLayer
import aktual.budget.reports.vm.SankeyLayerEnd
import aktual.budget.reports.vm.SankeyLayerRange
import aktual.budget.reports.vm.SankeyMode
import aktual.budget.reports.vm.SankeyReportMeta
import aktual.budget.reports.vm.normalizeSankeyLayers
import aktual.budget.reports.vm.sankeyLayerChoices
import aktual.budget.rules.components.Divider
import aktual.budget.rules.components.SectionLabel
import aktual.budget.rules.components.label
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// packages/desktop-client/src/components/reports/reports/Sankey.tsx
@Composable
internal fun SankeySettings(
  meta: SankeyReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((SankeyReportMeta) -> SankeyReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  var picker by rememberSaveable { mutableStateOf<SankeyPicker?>(null) }
  val layers = meta.layers()

  when (picker) {
    TopCategories -> {
      OptionSheet(
        title = Strings.reportsSankeyTopCategories,
        options = TOP_N_OPTIONS,
        selected = meta.topNCategories ?: DEFAULT_TOP_N_CATEGORIES,
        isEnabled = { true },
        label = { topNLabel(it) },
        onSelect = { n -> onEdit { it.copy(topNCategories = n) } },
        onDismiss = { picker = null },
      )
      return
    }
    Sort -> {
      OptionSheet(
        title = Strings.reportsSankeySort,
        options = SORTS,
        selected = meta.categorySort?.takeIf { it != Unknown } ?: PerGroup,
        isEnabled = { true },
        label = { it.label() },
        onSelect = { sort -> onEdit { it.copy(categorySort = sort) } },
        onDismiss = { picker = null },
      )
      return
    }
    LayerFrom,
    LayerTo -> {
      val end = if (picker == LayerFrom) SankeyLayerEnd.From else SankeyLayerEnd.To
      val other = if (end == From) layers.to else layers.from
      OptionSheet(
        title = end.label(),
        options = sankeyLayerChoices(meta.mode, end, other),
        selected = if (end == From) layers.from else layers.to,
        isEnabled = { true },
        label = { it.label() },
        onSelect = { layer -> onEdit { it.withLayer(end, layer) } },
        onDismiss = { picker = null },
      )
      return
    }
    null -> {}
  }

  FilteredReportSettings(
    timeFrame = meta.timeFrame,
    conditions = meta.conditions.orEmpty().toImmutableList(),
    conditionsOp = meta.conditionsOp ?: And,
    editorData = editorData,
    onTimeFrame = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
    onConditions = { conditions, op ->
      onEdit { it.copy(conditions = conditions, conditionsOp = op) }
    },
    onDismiss = onDismiss,
  ) {
    Column {
      SectionLabel(Strings.reportsSankeyMode)
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = if (meta.mode == Budgeted) Budgeted else Spent,
        options = MODES,
        onSelect = { mode -> onEdit { it.withMode(mode) } },
        string = {
          if (it == Budgeted) Strings.reportsSankeyBudgeted else Strings.reportsSankeyModeSpent
        },
      )
    }

    SankeyDisplay(meta, layers, onEdit, onPick = { picker = it })
  }
}

@Composable
private fun SankeyDisplay(
  meta: SankeyReportMeta,
  layers: SankeyLayerRange,
  onEdit: ((SankeyReportMeta) -> SankeyReportMeta) -> Unit,
  onPick: (SankeyPicker) -> Unit,
) =
  Section(label = Strings.reportsCustomEditorDisplay) {
    val isSpent = meta.mode != Budgeted
    SettingValueRow(
      Strings.reportsSankeyTopCategories,
      topNLabel(meta.topNCategories ?: DEFAULT_TOP_N_CATEGORIES),
    ) {
      onPick(TopCategories)
    }
    Divider()
    SettingValueRow(Strings.reportsSankeySort, (meta.categorySort ?: PerGroup).label()) {
      onPick(Sort)
    }
    Divider()
    SettingValueRow(Strings.reportsSankeyLayerFrom, layers.from.label()) { onPick(LayerFrom) }
    Divider()
    SettingValueRow(Strings.reportsSankeyLayerTo, layers.to.label()) { onPick(LayerTo) }
    Divider()
    SwitchRow(Strings.reportsSankeyPercentages, meta.showPercentages == true) { show ->
      onEdit { it.copy(showPercentages = show) }
    }
    Divider()
    // Upstream only uses these in spent mode
    SwitchRow(
      Strings.reportsSankeyGroupAccounts,
      meta.groupAccounts == true,
      isEnabled = isSpent,
    ) { group ->
      onEdit { it.copy(groupAccounts = group) }
    }
    Divider()
    SwitchRow(
      Strings.reportsSankeyTransfers,
      meta.showTransfers == true,
      isEnabled = isSpent && meta.groupAccounts != true,
    ) { show ->
      onEdit { it.copy(showTransfers = show) }
    }
  }

private fun SankeyReportMeta.layers(): SankeyLayerRange =
  normalizeSankeyLayers(mode, SankeyLayer.parse(layerFrom), SankeyLayer.parse(layerTo))

// The layers that don't fit the new mode go back to its defaults
private fun SankeyReportMeta.withMode(mode: SankeyMode): SankeyReportMeta {
  val layers = normalizeSankeyLayers(mode, SankeyLayer.parse(layerFrom), SankeyLayer.parse(layerTo))
  return copy(mode = mode, layerFrom = layers.from.key, layerTo = layers.to.key)
}

private fun SankeyReportMeta.withLayer(end: SankeyLayerEnd, layer: SankeyLayer): SankeyReportMeta {
  val current = layers()
  val layers =
    when (end) {
      From -> normalizeSankeyLayers(mode, layer, current.to, end)
      To -> normalizeSankeyLayers(mode, current.from, layer, end)
    }
  return copy(layerFrom = layers.from.key, layerTo = layers.to.key)
}

@Composable
private fun topNLabel(n: Int): String =
  if (n >= ALL_SANKEY_CATEGORIES) Strings.reportsSankeyTopAll else n.toString()

@Composable
private fun CategorySort.label(): String =
  when (this) {
    PerGroup,
    Unknown -> Strings.reportsSankeySortPerGroup
    Global -> Strings.reportsSankeySortGlobal
    BudgetOrder -> Strings.reportsSankeySortBudget
  }

@Composable
private fun SankeyLayerEnd.label(): String =
  when (this) {
    From -> Strings.reportsSankeyLayerFrom
    To -> Strings.reportsSankeyLayerTo
  }

@Composable
private fun SankeyLayer.label(): String =
  when (this) {
    IncomePayee -> Strings.reportsSankeyLayerPayee
    IncomeCategory -> Strings.reportsSankeyLayerIncomeCategory
    Account -> Strings.reportsSankeyLayerAccount
    Budget -> Strings.reportsSankeyLayerBudget
    CategoryGroup -> Strings.reportsSankeyLayerCategoryGroup
    Category -> Strings.reportsSankeyLayerCategory
  }

private enum class SankeyPicker {
  TopCategories,
  Sort,
  LayerFrom,
  LayerTo,
}

private val MODES = persistentListOf<SankeyMode>(Spent, Budgeted)

private val SORTS = persistentListOf<CategorySort>(PerGroup, Global, BudgetOrder)

@Suppress("MagicNumber")
private val TOP_N_OPTIONS = persistentListOf(ALL_SANKEY_CATEGORIES, 10, 15, 20, 25, 30)
