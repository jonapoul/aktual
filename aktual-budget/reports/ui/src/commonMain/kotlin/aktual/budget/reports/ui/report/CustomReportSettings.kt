package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.string
import aktual.budget.reports.vm.CUSTOM_BALANCE_TYPES
import aktual.budget.reports.vm.CUSTOM_GROUP_BYS
import aktual.budget.reports.vm.CUSTOM_INTERVALS
import aktual.budget.reports.vm.CUSTOM_MODES
import aktual.budget.reports.vm.CUSTOM_SORT_BYS
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomReportConfig
import aktual.budget.reports.vm.CustomReportDates
import aktual.budget.reports.vm.canIncludeCurrent
import aktual.budget.reports.vm.canShowTrendLines
import aktual.budget.reports.vm.dateRangeOptions
import aktual.budget.reports.vm.graphOptions
import aktual.budget.reports.vm.hasComplexCategoryCondition
import aktual.budget.reports.vm.isBalanceTypeEnabled
import aktual.budget.reports.vm.isGraphEnabled
import aktual.budget.reports.vm.isGroupByEnabled
import aktual.budget.reports.vm.isIntervalEnabled
import aktual.budget.reports.vm.report.CustomReportEditorData
import aktual.budget.reports.vm.report.TagChoice
import aktual.budget.reports.vm.selectedCategories
import aktual.budget.reports.vm.showsSort
import aktual.budget.reports.vm.withBalanceType
import aktual.budget.reports.vm.withDateRange
import aktual.budget.reports.vm.withGraphType
import aktual.budget.reports.vm.withGroupBy
import aktual.budget.reports.vm.withIncludeCurrent
import aktual.budget.reports.vm.withInterval
import aktual.budget.reports.vm.withLiveDates
import aktual.budget.reports.vm.withMode
import aktual.budget.reports.vm.withSelectedCategories
import aktual.budget.reports.vm.withStaticDates
import aktual.budget.reports.vm.withStaticMonths
import aktual.budget.reports.vm.withTagScope
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.YearMonthPicker
import aktual.core.ui.switch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.yearMonth

// packages/desktop-client/src/components/reports/ReportSidebar.tsx and ReportTopbar.tsx. Each
// change goes through the options' rules, so the draft never holds a combination upstream disables
@Composable
internal fun CustomReportSettings(
  draft: CustomReportConfig,
  preview: ChartData?,
  editorData: CustomReportEditorData?,
  onAction: (CustomReportAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  var sheet by rememberSaveable { mutableStateOf<CustomSheet?>(null) }
  val edit = { transform: (CustomReportConfig) -> CustomReportConfig ->
    onAction(CustomReportAction.Edit(transform))
  }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
    DisplaySection(draft = draft, editorData = editorData, edit = edit, open = { sheet = it })
    DatesSection(draft = draft, preview = preview, editorData = editorData, edit = edit) {
      sheet = it
    }
    OptionsSection(draft = draft, edit = edit)
    CategoriesSection(draft = draft, editorData = editorData) { sheet = it }

    if (editorData != null) {
      Column {
        SectionLabel(Strings.reportsCustomEditorFilters)
        ConditionsEditor(
          conditions = draft.conditions,
          conditionsOp = draft.conditionsOp,
          data = editorData.query,
          onChange = { conditions, op ->
            edit { it.copy(conditions = conditions.toImmutableList(), conditionsOp = op) }
          },
        )
      }
    }
  }

  CustomSheets(
    sheet = sheet,
    draft = draft,
    editorData = editorData,
    edit = edit,
    onDismiss = { sheet = null },
  )
}

private enum class CustomSheet {
  Graph,
  Split,
  Type,
  Interval,
  Sort,
  Range,
  Categories,
  TagScope,
}

@Composable
private fun DisplaySection(
  draft: CustomReportConfig,
  editorData: CustomReportEditorData?,
  edit: ((CustomReportConfig) -> CustomReportConfig) -> Unit,
  open: (CustomSheet) -> Unit,
) =
  Section(label = Strings.reportsCustomEditorDisplay) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = draft.mode,
        options = CUSTOM_MODES,
        onSelect = { mode -> edit { it.withMode(mode) } },
        string = { it.label() },
      )
    }
    Divider()
    SettingValueRow(Strings.reportsCustomEditorGraph, draft.graphType.label()) {
      open(Graph)
    }
    Divider()
    SettingValueRow(Strings.reportsCustomEditorSplit, draft.groupBy.label()) {
      open(Split)
    }
    if (draft.groupBy == Tag && editorData != null) {
      Divider()
      SettingValueRow(Strings.reportsCustomEditorTags, tagScopeLabel(draft, editorData)) {
        open(TagScope)
      }
    }
    Divider()
    SettingValueRow(Strings.reportsCustomEditorType, draft.balanceType.label()) {
      open(Type)
    }
    Divider()
    SettingValueRow(Strings.reportsCustomEditorInterval, draft.interval.label()) {
      open(Interval)
    }
    if (draft.showsSort) {
      Divider()
      SettingValueRow(Strings.reportsCustomEditorSort, draft.sortBy.label()) {
        open(Sort)
      }
    }
  }

@Composable
private fun DatesSection(
  draft: CustomReportConfig,
  preview: ChartData?,
  editorData: CustomReportEditorData?,
  edit: ((CustomReportConfig) -> CustomReportConfig) -> Unit,
  open: (CustomSheet) -> Unit,
) =
  Section(label = Strings.reportsCustomEditorDates) {
    val dates = draft.dates
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = dates is Static,
        options = DATE_MODES,
        onSelect = { isStatic ->
          if (isStatic) {
            val range = currentRange(preview, editorData)
            if (range != null) edit { it.withStaticDates(range) }
          } else {
            edit { it.withLiveDates() }
          }
        },
        string = { isStatic ->
          if (isStatic) Strings.reportsDateConfigStatic else Strings.reportsDateConfigLive
        },
      )
    }
    Divider()
    when (dates) {
      is Live -> {
        SettingValueRow(Strings.reportsCustomEditorRange, dates.type.string()) {
          open(Range)
        }
        Divider()
        SwitchRow(
          label = Strings.reportsCustomEditorIncludeCurrent,
          isChecked = dates.includeCurrent && draft.canIncludeCurrent,
          isEnabled = draft.canIncludeCurrent,
          onChange = { include -> edit { it.withIncludeCurrent(include) } },
        )
      }
      is Static -> {
        if (editorData != null) StaticMonths(dates = dates, editorData = editorData, edit = edit)
      }
    }
  }

@Composable
private fun StaticMonths(
  dates: CustomReportDates.Static,
  editorData: CustomReportEditorData,
  edit: ((CustomReportConfig) -> CustomReportConfig) -> Unit,
) {
  val start = dates.start.yearMonth
  val end = dates.end.yearMonth
  val months = editorData.query.months
  val range = YearMonthRange(minOf(months.start, start), maxOf(months.endInclusive, end))
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      SectionLabel(Strings.reportsCustomEditorStart)
      YearMonthPicker(
        modifier = Modifier.fillMaxWidth(),
        value = start,
        range = range,
        onValueChange = { month -> edit { it.withStaticMonths(month, end) } },
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      SectionLabel(Strings.reportsCustomEditorEnd)
      YearMonthPicker(
        modifier = Modifier.fillMaxWidth(),
        value = end,
        range = range,
        onValueChange = { month -> edit { it.withStaticMonths(minOf(start, month), month) } },
      )
    }
  }
}

@Composable
private fun OptionsSection(
  draft: CustomReportConfig,
  edit: ((CustomReportConfig) -> CustomReportConfig) -> Unit,
) =
  Section(label = Strings.reportsCustomEditorOptions) {
    SwitchRow(Strings.reportsCustomEditorShowHidden, draft.showHidden) { show ->
      edit { it.copy(showHidden = show) }
    }
    Divider()
    SwitchRow(Strings.reportsCustomEditorShowEmpty, draft.showEmpty) { show ->
      edit { it.copy(showEmpty = show) }
    }
    Divider()
    SwitchRow(Strings.reportsCustomEditorShowOffBudget, draft.showOffBudget) { show ->
      edit { it.copy(showOffBudget = show) }
    }
    Divider()
    SwitchRow(Strings.reportsCustomEditorShowUncategorised, draft.showUncategorized) { show ->
      edit { it.copy(showUncategorized = show) }
    }
    Divider()
    SwitchRow(Strings.reportsCustomEditorTrimIntervals, draft.trimIntervals) { trim ->
      edit { it.copy(trimIntervals = trim) }
    }
    Divider()
    SwitchRow(
      label = Strings.reportsCustomEditorShowTrendLines,
      isChecked = draft.showTrendLines && draft.canShowTrendLines,
      isEnabled = draft.canShowTrendLines,
      onChange = { show -> edit { it.copy(showTrendLines = show) } },
    )
  }

@Composable
private fun CategoriesSection(
  draft: CustomReportConfig,
  editorData: CustomReportEditorData?,
  open: (CustomSheet) -> Unit,
) =
  Section(label = Strings.reportsCustomEditorCategories) {
    if (draft.conditions.hasComplexCategoryCondition()) {
      SettingRow(onClick = null) {
        Text(
          modifier = Modifier.weight(1f),
          text = Strings.reportsCustomEditorCategoriesComplex,
          style = typography.bodyMedium,
          color = colors.tableHeaderText,
        )
      }
    } else {
      val all = editorData?.allCategories().orEmpty()
      val count = draft.conditions.selectedCategories(all).size
      val value =
        when {
          editorData == null -> ""
          count == all.size -> Strings.reportsCustomEditorCategoriesAll
          else -> Strings.reportsCustomEditorCategoriesSome(count, all.size)
        }
      ValueRow(
        modifier = Modifier.testTag(Tags.CustomSetting),
        label = Strings.reportsCustomEditorCategories,
        onClick = { open(Categories) }.takeIf { editorData != null },
      ) {
        Value(text = value)
      }
    }
  }

@Composable
@Suppress("CyclomaticComplexMethod")
private fun CustomSheets(
  sheet: CustomSheet?,
  draft: CustomReportConfig,
  editorData: CustomReportEditorData?,
  edit: ((CustomReportConfig) -> CustomReportConfig) -> Unit,
  onDismiss: () -> Unit,
) {
  when (sheet) {
    Graph -> {
      OptionSheet(
        title = Strings.reportsCustomEditorGraph,
        options = draft.graphOptions,
        selected = draft.graphType,
        isEnabled = draft::isGraphEnabled,
        label = { it.label() },
        onSelect = { graph -> edit { it.withGraphType(graph) } },
        onDismiss = onDismiss,
      )
    }
    Split -> {
      OptionSheet(
        title = Strings.reportsCustomEditorSplit,
        options = CUSTOM_GROUP_BYS,
        selected = draft.groupBy,
        isEnabled = draft::isGroupByEnabled,
        label = { it.label() },
        onSelect = { groupBy -> edit { it.withGroupBy(groupBy) } },
        onDismiss = onDismiss,
      )
    }
    Type -> {
      OptionSheet(
        title = Strings.reportsCustomEditorType,
        options = CUSTOM_BALANCE_TYPES,
        selected = draft.balanceType,
        isEnabled = draft::isBalanceTypeEnabled,
        label = { it.label() },
        onSelect = { type -> edit { it.withBalanceType(type) } },
        onDismiss = onDismiss,
      )
    }
    Interval -> {
      OptionSheet(
        title = Strings.reportsCustomEditorInterval,
        options = CUSTOM_INTERVALS,
        selected = draft.interval,
        isEnabled = draft::isIntervalEnabled,
        label = { it.label() },
        onSelect = { interval -> edit { it.withInterval(interval) } },
        onDismiss = onDismiss,
      )
    }
    Sort -> {
      OptionSheet(
        title = Strings.reportsCustomEditorSort,
        options = CUSTOM_SORT_BYS,
        selected = draft.sortBy,
        isEnabled = { true },
        label = { it.label() },
        onSelect = { sort -> edit { it.copy(sortBy = sort) } },
        onDismiss = onDismiss,
      )
    }
    Range -> {
      val live = draft.dates as? Live
      if (live != null) {
        OptionSheet(
          title = Strings.reportsCustomEditorRange,
          options = dateRangeOptions(draft.interval),
          selected = live.type,
          isEnabled = { true },
          label = { it.string() },
          onSelect = { type -> edit { it.withDateRange(type) } },
          onDismiss = onDismiss,
        )
      }
    }
    Categories -> {
      if (editorData != null) {
        val all = editorData.allCategories()
        CategorySheet(
          groups = editorData.categories,
          selected = draft.conditions.selectedCategories(all),
          showHidden = draft.showHidden,
          onChange = { selected ->
            edit { it.copy(conditions = it.conditions.withSelectedCategories(selected, all)) }
          },
          onDismiss = onDismiss,
        )
      }
    }
    TagScope -> {
      if (editorData != null) {
        TagSheet(
          tags = editorData.tags,
          selected = draft.selectedTags(editorData),
          onChange = { selected ->
            edit { it.withTagScope(selected, editorData.tags.map(TagChoice::id)) }
          },
          onDismiss = onDismiss,
        )
      }
    }
    null -> {}
  }
}

// TagScopeSelector.tsx summary
@Composable
private fun tagScopeLabel(draft: CustomReportConfig, editorData: CustomReportEditorData): String {
  val selected = draft.selectedTags(editorData)
  return when {
    selected.isEmpty() -> Strings.reportsCustomEditorTagsAll
    selected.size == 1 -> "#${editorData.tags.first { it.id in selected }.name}"
    else -> Strings.reportsCustomEditorTagsSome(selected.size)
  }
}

// The scope's tags that still exist; none means all
private fun CustomReportConfig.selectedTags(editorData: CustomReportEditorData) =
  when (val scope = tagScope) {
    All -> persistentSetOf()
    is Selected ->
      scope.tagIds.filter { id -> editorData.tags.any { it.id == id } }.toImmutableSet()
  }

@Composable
private fun SettingValueRow(label: String, value: String, onClick: () -> Unit) =
  ValueRow(modifier = Modifier.testTag(Tags.CustomSetting), label = label, onClick = onClick) {
    Value(text = value)
  }

@Composable
private fun SwitchRow(
  label: String,
  isChecked: Boolean,
  isEnabled: Boolean = true,
  onChange: (Boolean) -> Unit,
) =
  SettingRow(
    modifier = Modifier.testTag(Tags.CustomSwitch),
    onClick = { onChange(!isChecked) }.takeIf { isEnabled },
    showChevron = false,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = label,
      style = typography.bodyLarge,
      color = if (isEnabled) colors.tableText else colors.tableHeaderText,
    )
    Switch(
      checked = isChecked,
      onCheckedChange = onChange,
      enabled = isEnabled,
      colors = colors.switch(),
    )
  }

private fun CustomReportEditorData.allCategories() = categories.flatMap { group ->
  group.categories.map { it.id }
}

// What the chart covers now, or the current month before it has loaded
private fun currentRange(
  preview: ChartData?,
  editorData: CustomReportEditorData?,
): LocalDateRange? {
  (preview as? CustomData)?.let {
    return it.range
  }
  val month = editorData?.query?.currentMonth ?: return null
  return month.firstDay..month.lastDay
}

private val DATE_MODES = persistentListOf(false, true)
