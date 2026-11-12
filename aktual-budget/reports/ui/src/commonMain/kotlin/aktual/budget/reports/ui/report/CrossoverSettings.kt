package aktual.budget.reports.ui.report

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.reports.ui.montecarlo.NumberField
import aktual.budget.reports.ui.montecarlo.PERCENT_SCALE
import aktual.budget.reports.vm.CrossoverReportMeta
import aktual.budget.reports.vm.ProjectionType
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.rules.components.Divider
import aktual.budget.rules.components.QuerySheet
import aktual.budget.rules.components.SectionLabel
import aktual.budget.rules.components.label
import aktual.budget.rules.domain.ConditionEntity
import aktual.core.l10n.Strings
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.checkbox
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet

// packages/desktop-client/src/components/reports/reports/Crossover.tsx. Unset categories mean every
// expense category and unset accounts mean every account, as upstream
@Composable
internal fun CrossoverSettings(
  meta: CrossoverReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((CrossoverReportMeta) -> CrossoverReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  var picker by rememberSaveable { mutableStateOf<CrossoverPicker?>(null) }

  if (picker == Projection) {
    OptionSheet(
      title = Strings.reportsCrossoverProjectionType,
      options = PROJECTION_TYPES,
      selected = meta.projectionType.orDefault(),
      isEnabled = { true },
      label = { it.label() },
      onSelect = { type -> onEdit { it.copy(projectionType = type) } },
      onDismiss = { picker = null },
    )
    return
  }

  if (editorData != null) {
    when (picker) {
      Dates -> {
        TimeFrameSheet(
          timeFrame = meta.timeFrame,
          data = editorData,
          onChange = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
          onDismiss = { picker = null },
        )
        return
      }
      Categories -> {
        CategorySheet(
          groups = editorData.categoryChoices,
          selected = meta.selectedCategories(editorData),
          showHidden = meta.showHiddenCategories == true,
          onChange = { selected -> onEdit { it.copy(expenseCategoryIds = selected.toList()) } },
          onDismiss = { picker = null },
        )
        return
      }
      Accounts -> {
        AccountSheet(
          title = Strings.reportsCrossoverIncomeAccounts,
          accounts = editorData.allAccounts,
          selected = meta.selectedAccounts(editorData),
          onChange = { selected -> onEdit { it.copy(incomeAccountIds = selected.toList()) } },
          onDismiss = { picker = null },
        )
        return
      }
      Projection,
      null -> {}
    }
  }

  QuerySheet(title = Strings.reportsChartSettings, onDismiss = onDismiss) {
    if (editorData != null) {
      Column {
        SectionLabel(Strings.reportsFormulaQueryTimeFrame)
        TimeFrameRow(timeFrame = meta.timeFrame, onClick = { picker = Dates })
      }

      InputsSection(meta, editorData, onEdit, onPick = { picker = it })
    }

    ProjectionSection(meta, onEdit, onPickProjection = { picker = Projection })
  }
}

@Composable
private fun InputsSection(
  meta: CrossoverReportMeta,
  editorData: QueryEditorData,
  onEdit: ((CrossoverReportMeta) -> CrossoverReportMeta) -> Unit,
  onPick: (CrossoverPicker) -> Unit,
) =
  Section(label = Strings.reportsCrossoverInputs) {
    val categories = editorData.visibleCategories(meta.showHiddenCategories == true)
    val selectedCategories = meta.selectedCategories(editorData).count { it in categories }
    SettingValueRow(
      Strings.reportsCrossoverExpenseCategories,
      countLabel(selectedCategories, categories.size),
    ) {
      onPick(Categories)
    }
    Divider()
    val selectedAccounts = meta.selectedAccounts(editorData).size
    SettingValueRow(
      Strings.reportsCrossoverIncomeAccounts,
      countLabel(selectedAccounts, editorData.allAccounts.size),
    ) {
      onPick(Accounts)
    }
    Divider()
    SwitchRow(Strings.reportsCustomEditorShowHidden, meta.showHiddenCategories == true) { show ->
      onEdit { it.copy(showHiddenCategories = show) }
    }
  }

@Composable
private fun ProjectionSection(
  meta: CrossoverReportMeta,
  onEdit: ((CrossoverReportMeta) -> CrossoverReportMeta) -> Unit,
  onPickProjection: () -> Unit,
) =
  Section(label = Strings.reportsCrossoverProjection) {
    NumberRow(Strings.reportsCrossoverWithdrawalRate) {
      NumberField(
        value = meta.safeWithdrawalRate,
        min = 0.0,
        max = MAX_PERCENT,
        scale = PERCENT_SCALE,
        onCommit = { rate -> onEdit { it.copy(safeWithdrawalRate = rate) } },
      )
    }
    Divider()
    SettingValueRow(
      Strings.reportsCrossoverProjectionType,
      meta.projectionType.orDefault().label(),
    ) {
      onPickProjection()
    }
    Divider()
    NumberRow(Strings.reportsCrossoverTargetIncome) {
      NumberField(
        value = meta.expenseAdjustmentFactor,
        min = 0.0,
        max = MAX_TARGET_PERCENT,
        scale = PERCENT_SCALE,
        roundToInteger = true,
        onCommit = { factor -> onEdit { it.copy(expenseAdjustmentFactor = factor) } },
      )
    }
    Divider()
    val customGrowth = meta.estimatedReturn != null || meta.expectedContribution != null
    SwitchRow(Strings.reportsCrossoverCustomGrowth, customGrowth) { enabled ->
      onEdit { it.withCustomGrowth(enabled) }
    }
    if (customGrowth) {
      Divider()
      NumberRow(Strings.reportsCrossoverExpectedReturn) {
        NumberField(
          value = meta.estimatedReturn ?: 0.0,
          min = 0.0,
          max = MAX_PERCENT,
          scale = PERCENT_SCALE,
          onCommit = { rate -> onEdit { it.copy(estimatedReturn = rate) } },
        )
      }
      Divider()
      NumberRow(Strings.reportsCrossoverExpectedContribution) {
        // Saved in cents, typed in whole units
        NumberField(
          value = (meta.expectedContribution ?: 0.0) / CENTS,
          min = 0.0,
          max = MAX_CONTRIBUTION,
          onCommit = { amount ->
            onEdit { it.copy(expectedContribution = (amount ?: 0.0) * CENTS) }
          },
        )
      }
    }
  }

@Composable
private fun NumberRow(label: String, field: @Composable () -> Unit) =
  ValueRow(label = label, onClick = null) { field() }

@Composable
internal fun AccountSheet(
  title: String,
  accounts: ImmutableList<ConditionEntity>,
  selected: ImmutableSet<AccountId>,
  onChange: (Set<AccountId>) -> Unit,
  onDismiss: () -> Unit,
) =
  QuerySheet(title = title, onDismiss = onDismiss) {
    Row(horizontalArrangement = spacedBy(8.dp)) {
      AktualButton(
        kind = Bare,
        text = Strings.reportsCustomEditorSelectAll,
        onClick = { onChange(accounts.map { AccountId(it.id) }.toSet()) },
      )
      AktualButton(
        kind = Bare,
        text = Strings.reportsCustomEditorSelectNone,
        onClick = { onChange(emptySet()) },
      )
    }

    Column {
      accounts.fastForEach { account ->
        val id = AccountId(account.id)
        val isChecked = id in selected
        CheckRow(
          modifier =
            Modifier.toggleable(
              value = isChecked,
              role = Role.Checkbox,
              onValueChange = { checked ->
                onChange(if (checked) selected + id else selected - id)
              },
            ),
          text = account.name,
          isBold = false,
        ) {
          Checkbox(checked = isChecked, onCheckedChange = null, colors = colors.checkbox())
        }
      }
    }
  }

@Composable
internal fun countLabel(count: Int, total: Int): String =
  if (count == total) {
    Strings.reportsCustomEditorCategoriesAll
  } else {
    Strings.reportsCustomEditorCategoriesSome(count, total)
  }

private fun CrossoverReportMeta.selectedCategories(
  data: QueryEditorData,
): ImmutableSet<CategoryId> =
  expenseCategoryIds?.toImmutableSet()
    ?: data.categoryChoices
      .flatMap { group -> group.categories.filterNot { it.isIncome }.map { it.id } }
      .toImmutableSet()

private fun CrossoverReportMeta.selectedAccounts(data: QueryEditorData): ImmutableSet<AccountId> =
  incomeAccountIds?.toImmutableSet() ?: data.allAccounts.map { AccountId(it.id) }.toImmutableSet()

private fun QueryEditorData.visibleCategories(showHidden: Boolean): Set<CategoryId> =
  categoryChoices
    .filter { showHidden || !it.isHidden }
    .flatMap { group -> group.categories.filter { showHidden || !it.isHidden }.map { it.id } }
    .toSet()

// Upstream starts the expected return at 6% when custom growth is first turned on
private fun CrossoverReportMeta.withCustomGrowth(enabled: Boolean): CrossoverReportMeta =
  if (enabled) {
    copy(
      estimatedReturn = estimatedReturn ?: DEFAULT_RETURN,
      expectedContribution = expectedContribution ?: 0.0,
    )
  } else {
    copy(estimatedReturn = null, expectedContribution = null)
  }

private fun ProjectionType?.orDefault(): ProjectionType = this?.takeIf { it != Unknown } ?: Hampel

@Composable
private fun ProjectionType.label(): String =
  when (this) {
    Hampel,
    Unknown -> Strings.reportsCrossoverProjectionHampel
    Median -> Strings.reportsCrossoverProjectionMedian
    Mean -> Strings.reportsCrossoverProjectionMean
  }

private enum class CrossoverPicker {
  Dates,
  Categories,
  Accounts,
  Projection,
}

private val PROJECTION_TYPES = persistentListOf<ProjectionType>(Hampel, Median, Mean)

private const val DEFAULT_RETURN = 0.06
private const val CENTS = 100.0
private const val MAX_PERCENT = 100.0
private const val MAX_TARGET_PERCENT = 1000.0
private const val MAX_CONTRIBUTION = 1_000_000_000.0
