package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.McRunDetailRow
import aktual.budget.reports.vm.montecarlo.MonteCarloRunDetail
import aktual.core.icons.material.ArrowDropDown
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.unaryPlus
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareTextButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.redacted
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// MonteCarloRunDetailTable: one run's year-by-year path, each year expandable into a plain-language
// story, the working behind it, and a per-pot breakdown
@Composable
internal fun RunDetailView(
  detail: MonteCarloRunDetail,
  config: McConfig,
  simulationCount: Int,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    // After a failure the capture continues with unfunded years for the cashflow chart; the table
    // ends at the failure
    val rows = remember(detail) { detail.rows.filter { !it.afterDepletion }.toImmutableList() }
    var expanded by remember(detail.index) { mutableStateOf(emptySet<Int>()) }
    var working by remember(detail.index) { mutableStateOf(emptySet<Int>()) }

    val startAge = config.currentAge
    val lastRow = rows.lastOrNull()
    val hasSurvived = lastRow != null && lastRow.endBalance > 0
    val allExpanded = rows.isNotEmpty() && expanded.size == rows.size

    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      itemVerticalAlignment = CenterVertically,
    ) {
      NormalTextButton(text = Strings.monteCarloDetailBack, onClick = onBack)
      Text(
        text =
          if (hasSurvived) {
            Strings.monteCarloDetailSurvived(
              detail.index + 1,
              simulationCount,
              startAge + lastRow.year,
            )
          } else {
            Strings.monteCarloDetailRanOut(
              detail.index + 1,
              simulationCount,
              // The failure row's own age: the year that couldn't be funded
              startAge + (lastRow?.year ?: 1) - 1,
            )
          },
        style = typography.bodyMedium,
        fontWeight = SemiBold,
        color = colors.pageText,
      )
      BareTextButton(
        text =
          if (allExpanded) {
            Strings.monteCarloDetailCollapseAll
          } else {
            Strings.monteCarloDetailExpandAll
          },
        onClick = {
          if (allExpanded) {
            expanded = emptySet()
            working = emptySet()
          } else {
            expanded = rows.map { it.year }.toSet()
          }
        },
      )
    }

    CashflowGraph(chart = detail.cashflow, config = config)

    BodyText(totalsText(rows))

    val hasContributions = config.contributions.isNotEmpty() || config.pots.any { it.isSurplus }
    val hasIncome = config.incomeStreams.isNotEmpty()
    val showInflation = rows.any { it.inflation != null }
    val columns = Columns(hasContributions, hasIncome, showInflation)

    Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
      Column {
        HeaderRow(columns)
        HorizontalDivider(modifier = Modifier.width(columns.width), color = colors.tableBorder)
        for (row in rows) {
          val isExpanded = row.year in expanded
          YearRow(
            row = row,
            age = startAge + row.year - 1,
            isFailure = row === lastRow && !hasSurvived,
            isExpanded = isExpanded,
            columns = columns,
            onToggle = {
              if (isExpanded) {
                // Collapsing a year hides its working too
                expanded = expanded - row.year
                working = working - row.year
              } else {
                expanded = expanded + row.year
              }
            },
          )
          if (isExpanded) {
            YearBreakdown(
              row = row,
              config = config,
              showsWorking = row.year in working,
              onToggleWorking = {
                working = if (row.year in working) working - row.year else working + row.year
              },
              hasContributions = hasContributions,
              modifier = Modifier.width(columns.width),
            )
          }
          HorizontalDivider(modifier = Modifier.width(columns.width), color = colors.tableBorder)
        }
      }
    }

    lastRow?.inaccessibleBalance?.let { locked ->
      BodyText(Strings.monteCarloDetailFailedLocked(startAge + lastRow.year - 1, locked.money()))
    }
  }

private data class Columns(
  val hasContributions: Boolean,
  val hasIncome: Boolean,
  val showInflation: Boolean,
) {
  val width: Dp
    get() {
      var amounts = BASE_AMOUNT_COLUMNS
      if (hasContributions) amounts++
      if (hasIncome) amounts++
      var width = EXPAND_WIDTH + AGE_WIDTH + RATE_WIDTH + AMOUNT_WIDTH * amounts
      var cells = amounts + 3
      if (showInflation) {
        width += RATE_WIDTH
        cells++
      }
      return width + CELL_SPACING * (cells - 1)
    }
}

@Composable
private fun totalsText(rows: ImmutableList<McRunDetailRow>): String {
  val withdrawn = rows.sumOf { it.withdrawal }
  val tax = rows.sumOf { it.taxPaid }
  val fees = rows.sumOf { it.feesPaid }
  val income = rows.sumOf { it.income }
  val incomeTax = rows.sumOf { it.incomeTax }
  val totals =
    when {
      tax > 0 && fees > 0 ->
        Strings.monteCarloDetailTotalsTaxFees(withdrawn.money(), tax.money(), fees.money())
      tax > 0 -> Strings.monteCarloDetailTotalsTax(withdrawn.money(), tax.money())
      fees > 0 -> Strings.monteCarloDetailTotalsFees(withdrawn.money(), fees.money())
      else -> Strings.monteCarloDetailTotals(withdrawn.money())
    }
  if (income <= 0) return totals
  val incomeTotals =
    if (incomeTax > 0) {
      Strings.monteCarloDetailIncomeTotalsTax(income.money(), incomeTax.money())
    } else {
      Strings.monteCarloDetailIncomeTotals(income.money())
    }
  return "$totals $incomeTotals"
}

@Composable
private fun HeaderRow(columns: Columns) =
  Row(
    modifier = Modifier.padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(CELL_SPACING),
    verticalAlignment = CenterVertically,
  ) {
    Box(Modifier.width(EXPAND_WIDTH))
    GroupHeading(Strings.monteCarloDetailAge, Modifier.width(AGE_WIDTH))
    AmountHeading(Strings.monteCarloDetailStart)
    if (columns.hasContributions) AmountHeading(Strings.monteCarloDetailContributions)
    if (columns.hasIncome) AmountHeading(Strings.monteCarloDetailIncome)
    AmountHeading(Strings.monteCarloDetailWithdrawal)
    AmountHeading(Strings.monteCarloDetailSpent)
    AmountHeading(Strings.monteCarloDetailGrowth)
    GroupHeading(
      Strings.monteCarloDetailReturn,
      Modifier.width(RATE_WIDTH),
      textAlign = End,
    )
    if (columns.showInflation) {
      GroupHeading(
        Strings.monteCarloDetailInflation,
        Modifier.width(RATE_WIDTH),
        textAlign = End,
      )
    }
    AmountHeading(Strings.monteCarloDetailEnd)
  }

@Composable
private fun AmountHeading(text: String) =
  GroupHeading(text, Modifier.width(AMOUNT_WIDTH), textAlign = End)

@Composable
private fun YearRow(
  row: McRunDetailRow,
  age: Int,
  isFailure: Boolean,
  isExpanded: Boolean,
  columns: Columns,
  onToggle: () -> Unit,
) =
  Row(
    modifier = Modifier.clickable(onClick = onToggle).padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(CELL_SPACING),
    verticalAlignment = CenterVertically,
  ) {
    Box(modifier = Modifier.width(EXPAND_WIDTH), contentAlignment = Center) {
      Icon(
        modifier = Modifier.size(20.dp),
        imageVector = if (isExpanded) MaterialIcons.ArrowDropDown else MaterialIcons.ArrowRight,
        contentDescription =
          if (isExpanded) {
            Strings.monteCarloDetailHideBreakdown
          } else {
            Strings.monteCarloDetailShowBreakdown
          },
        tint = colors.pageText,
      )
    }
    TableCell("$age", Modifier.width(AGE_WIDTH), textAlign = Start)
    AmountCell(row.startBalance.money())
    if (columns.hasContributions) AmountCell((row.contributions + row.surplusSaved).money())
    if (columns.hasIncome) AmountCell((row.income - row.incomeTax).money())
    AmountCell(row.withdrawal.money())
    // A year that couldn't be fully paid for stands out
    AmountCell(
      row.spent.money(),
      color =
        if (row.spent < row.plannedSpending) colors.reportsNumberNegative else colors.pageText,
    )
    val growthColor =
      if (row.growth >= 0) colors.reportsNumberPositive else colors.reportsNumberNegative
    // No growth on a failure year: the plan stops there
    AmountCell(if (isFailure) "" else row.growth.money(), color = growthColor)
    val growthBase = row.startBalance + row.contributions + row.surplusSaved - row.withdrawal
    val growthRate = if (!isFailure && growthBase > 0) row.growth.toDouble() / growthBase else null
    TableCell(
      growthRate?.let(::formatPercent).orEmpty(),
      Modifier.width(RATE_WIDTH),
      color = growthColor,
    )
    if (columns.showInflation) {
      // Deliberately neutral, since colouring inflation good or bad would oversimplify
      TableCell(row.inflation?.let(::formatPercent).orEmpty(), Modifier.width(RATE_WIDTH))
    }
    // On a bridge-gap failure the true remaining balance is the locked money
    AmountCell((row.inaccessibleBalance ?: row.endBalance).money())
  }

@Composable
private fun AmountCell(text: String, color: Color = colors.pageText) =
  TableCell(text, Modifier.width(AMOUNT_WIDTH), color = color)

@Composable
private fun TableCell(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = colors.pageText,
  textAlign: TextAlign = End,
) =
  Text(
    modifier = modifier,
    text = text.redacted(),
    style = typography.bodySmall,
    color = color,
    textAlign = textAlign,
    maxLines = 1,
  )

@Composable
private fun YearBreakdown(
  row: McRunDetailRow,
  config: McConfig,
  showsWorking: Boolean,
  onToggleWorking: () -> Unit,
  hasContributions: Boolean,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier.padding(start = EXPAND_WIDTH + CELL_SPACING, end = 12.dp, bottom = 12.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    val surplusPotName = surplusPotLabel(config.pots)
    val story =
      yearStory(
        row = row,
        rule = config.withdrawalRule,
        surplusPotName = surplusPotName,
        hasSurplusPot = config.pots.any { it.isSurplus },
      )
    DetailText(story.joinToString(separator = " "), Modifier.widthIn(max = TEXT_MAX_WIDTH))

    Text(
      modifier = Modifier.clickable(onClick = onToggleWorking),
      text =
        if (showsWorking) {
          Strings.monteCarloDetailHideWorking
        } else {
          Strings.monteCarloDetailShowWorking
        },
      style = typography.bodySmall,
      color = colors.pageTextLight,
      textDecoration = Underline,
    )

    if (showsWorking) {
      Column(
        modifier = Modifier.padding(top = 6.dp).widthIn(max = TEXT_MAX_WIDTH),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        for (line in workingLines(row, config, surplusPotName)) DetailText(line)
      }
    }

    PotTable(row, config, hasContributions, surplusPotName, Modifier.padding(top = 14.dp))
  }

@Composable
private fun workingLines(row: McRunDetailRow, config: McConfig, surplusPotName: String) =
  buildList {
    row.ruleExplanation?.let { +ruleWorking(it, config.withdrawalRule) }
    if (row.minimumApplied) +Strings.monteCarloWorkingMinimum(row.plannedSpending.money())
    if (row.income > 0) {
      val income =
        if (row.incomeTax > 0) {
          Strings.monteCarloWorkingIncomeTaxed(
            row.income.money(),
            row.incomeTax.money(),
            (row.income - row.incomeTax).money(),
          )
        } else {
          Strings.monteCarloWorkingIncomeUntaxed(row.income.money())
        }
      val streams =
        if (config.incomeStreams.size > 1) {
          row.incomeAmounts.mapIndexedNotNull { i, amount ->
            if (amount > 0) {
              val name =
                config.incomeStreams[i].name.ifEmpty { Strings.monteCarloIncomeNumbered(i + 1) }
              Strings.monteCarloWorkingIncomeStream(name, amount.money())
            } else {
              null
            }
          }
        } else {
          emptyList()
        }
      if (streams.isEmpty()) +income else +"$income (${streams.joinToString("; ")})"
    }
    if (row.taxPaid > 0) {
      +Strings.monteCarloWorkingWithdrawalTaxed(
        row.withdrawal.money(),
        row.taxPaid.money(),
        (row.withdrawal - row.taxPaid).money(),
      )
    } else {
      +Strings.monteCarloWorkingWithdrawalUntaxed(row.withdrawal.money())
    }
    when {
      row.spent < row.plannedSpending ->
        +Strings.monteCarloWorkingSpentShort(
          row.spent.money(),
          row.plannedSpending.money(),
          (row.plannedSpending - row.spent).money(),
        )
      row.spent > row.plannedSpending ->
        +Strings.monteCarloWorkingSpentExtra(
          row.spent.money(),
          (row.spent - row.plannedSpending).money(),
          row.plannedSpending.money(),
        )
      else -> +Strings.monteCarloWorkingSpent(row.spent.money())
    }
    if (row.surplusSaved > 0) {
      +Strings.monteCarloWorkingSaved(surplusPotName, row.surplusSaved.money())
    }
    if (row.unspentIncome > 0 && row.surplusSaved == 0L) {
      +Strings.monteCarloWorkingUnspent(row.unspentIncome.money())
    }
    if (row.contributions > 0) {
      +Strings.monteCarloWorkingContributions(row.contributions.money())
    }
    if (row.feesPaid > 0) +Strings.monteCarloWorkingFees(row.feesPaid.money())
    row.inaccessibleBalance?.let { +Strings.monteCarloWorkingLocked(it.money()) }
  }

@Composable
private fun DetailText(text: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier,
    text = text.redacted(),
    style = typography.bodySmall,
    color = colors.pageText,
  )

@Composable
private fun PotTable(
  row: McRunDetailRow,
  config: McConfig,
  hasContributions: Boolean,
  surplusPotName: String,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier) {
    val hasSurplusPot = config.pots.any { it.isSurplus }
    Row(
      modifier = Modifier.padding(bottom = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(CELL_SPACING),
      verticalAlignment = CenterVertically,
    ) {
      GroupHeading(Strings.monteCarloDetailPot, Modifier.width(POT_NAME_WIDTH))
      PotHeading(Strings.monteCarloDetailPotStart)
      if (hasContributions) {
        PotHeading(
          Strings.monteCarloDetailPotContributed,
          help =
            if (hasSurplusPot) Strings.monteCarloDetailHelpContributed(surplusPotName) else null,
        )
      }
      PotHeading(Strings.monteCarloDetailPotWithdrawn)
      PotHeading(Strings.monteCarloDetailPotTaxable)
      PotHeading(Strings.monteCarloDetailPotTaxPaid, help = Strings.monteCarloDetailHelpTaxPaid)
      PotHeading(Strings.monteCarloDetailPotFees, help = Strings.monteCarloDetailHelpFees)
      GroupHeading(
        Strings.monteCarloDetailReturn,
        Modifier.width(RATE_WIDTH),
        textAlign = End,
      )
      PotHeading(Strings.monteCarloDetailPotEnd)
    }
    HorizontalDivider(color = colors.tableBorder)

    config.pots.forEachIndexed { i, pot ->
      Row(
        modifier = Modifier.padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(CELL_SPACING),
        verticalAlignment = CenterVertically,
      ) {
        TableCell(
          potLabel(config.pots, i),
          Modifier.width(POT_NAME_WIDTH),
          textAlign = Start,
        )
        AmountCell(row.potStartBalances.getOrElse(i) { 0 }.money())
        if (hasContributions) {
          val surplus = if (pot.isSurplus) row.surplusSaved else 0
          AmountCell((row.potContributions.getOrElse(i) { 0 } + surplus).money())
        }
        AmountCell(row.potWithdrawals.getOrElse(i) { 0 }.money())
        AmountCell(row.potTaxables.getOrElse(i) { 0 }.money())
        AmountCell(row.potTaxes.getOrElse(i) { 0 }.money())
        AmountCell(row.potFees.getOrElse(i) { 0 }.money())
        val potReturn = row.potReturns.getOrNull(i)
        TableCell(
          potReturn?.let(::formatPercent).orEmpty(),
          Modifier.width(RATE_WIDTH),
          color =
            when {
              potReturn == null -> colors.pageText
              potReturn >= 0 -> colors.reportsNumberPositive
              else -> colors.reportsNumberNegative
            },
        )
        AmountCell(row.potBalances.getOrElse(i) { 0 }.money())
      }
    }
  }

@Composable
private fun PotHeading(text: String, help: String? = null) =
  Row(
    modifier = Modifier.width(AMOUNT_WIDTH),
    horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.End),
    verticalAlignment = CenterVertically,
  ) {
    GroupHeading(text)
    help?.let { HelpTooltip(it) }
  }

// Start, withdrawal, spent, growth and end balance
private const val BASE_AMOUNT_COLUMNS = 5
private val CELL_SPACING = 10.dp
private val EXPAND_WIDTH = 28.dp
private val AGE_WIDTH = 40.dp
private val AMOUNT_WIDTH = 110.dp
private val RATE_WIDTH = 80.dp
private val POT_NAME_WIDTH = 120.dp
private val TEXT_MAX_WIDTH = 720.dp
