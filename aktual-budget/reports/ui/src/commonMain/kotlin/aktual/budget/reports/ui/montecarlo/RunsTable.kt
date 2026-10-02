package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.montecarlo.MonteCarloRun
import aktual.budget.reports.vm.montecarlo.RunPercentile
import aktual.core.l10n.Strings
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.NormalTextButton
import aktual.core.ui.formattedString
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private enum class SortOrder {
  WorstFirst,
  BestFirst,
}

// MonteCarloRunsTable: every run ranked by outcome, a page at a time. Tapping a run opens its
// year-by-year detail
@Composable
internal fun RunsTable(
  runs: ImmutableList<MonteCarloRun>,
  onSelectRun: (Int) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    var sortOrder by rememberSaveable(runs) { mutableStateOf(SortOrder.WorstFirst) }
    var page by rememberSaveable(runs) { mutableIntStateOf(0) }
    var highlightedRank by rememberSaveable(runs) { mutableStateOf<Int?>(null) }

    val count = runs.size
    val pageCount = maxOf(1, (count + PAGE_SIZE - 1) / PAGE_SIZE)
    val currentPage = page.coerceAtMost(pageCount - 1)
    val pageStart = currentPage * PAGE_SIZE
    val pageSize = minOf(PAGE_SIZE, count - pageStart).coerceAtLeast(0)

    // Best-first reads the same ranking from the other end
    fun runAt(rank: Int) = runs[if (sortOrder == SortOrder.BestFirst) count - 1 - rank else rank]

    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      itemVerticalAlignment = CenterVertically,
    ) {
      BodyText(Strings.monteCarloRunsShowing(pageStart + 1, pageStart + pageSize, count))
      AktualExposedDropDownMenu(
        value = sortOrder,
        onValueChange = { order ->
          sortOrder = order
          page = 0
          highlightedRank = null
        },
        options = SortOrder.entries.toImmutableList(),
        string = { order ->
          when (order) {
            SortOrder.WorstFirst -> Strings.monteCarloRunsWorstFirst
            SortOrder.BestFirst -> Strings.monteCarloRunsBestFirst
          }
        },
      )
    }

    Column {
      Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        GroupHeading(Strings.monteCarloRunsRank, Modifier.width(RANK_WIDTH.dp))
        GroupHeading(Strings.monteCarloRunsOutcome, Modifier.weight(1f))
        GroupHeading(Strings.monteCarloRunsEnding, Modifier.weight(1f), textAlign = End)
        GroupHeading(Strings.monteCarloRunsWithdrawn, Modifier.weight(1f), textAlign = End)
      }
      HorizontalDivider(color = colors.tableBorder)

      for (offset in 0 until pageSize) {
        val rank = pageStart + offset
        RunRow(
          rank = rank,
          run = runAt(rank),
          isHighlighted = rank == highlightedRank,
          onClick = onSelectRun,
        )
        HorizontalDivider(color = colors.tableBorder)
      }
    }

    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      itemVerticalAlignment = CenterVertically,
    ) {
      // An action picker rather than a setting, so it always reads "Jump to…" at rest
      AktualExposedDropDownMenu(
        value = null,
        onValueChange = { percentile: RunPercentile? ->
          if (percentile != null) {
            val worstFirstRank = (percentile.fraction * (count - 1)).roundToInt()
            val rank =
              if (sortOrder == SortOrder.WorstFirst) worstFirstRank else count - 1 - worstFirstRank
            page = rank / PAGE_SIZE
            highlightedRank = rank
          }
        },
        options = RunPercentile.entries.toImmutableList<RunPercentile?>(),
        string = { it?.string() ?: Strings.monteCarloRunsJump },
      )
      NormalTextButton(
        text = Strings.monteCarloRunsPrevious,
        isEnabled = currentPage > 0,
        onClick = {
          page = currentPage - 1
          highlightedRank = null
        },
      )
      NormalTextButton(
        text = Strings.monteCarloRunsNext,
        isEnabled = currentPage < pageCount - 1,
        onClick = {
          page = currentPage + 1
          highlightedRank = null
        },
      )
    }
  }

@Composable
private fun RunRow(
  rank: Int,
  run: MonteCarloRun,
  isHighlighted: Boolean,
  onClick: (Int) -> Unit,
) {
  val background = if (isHighlighted) colors.tableRowBackgroundHighlight else Color.Transparent
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(background)
        .clickable { onClick(run.index) }
        .padding(horizontal = 8.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = CenterVertically,
  ) {
    Cell("${rank + 1}", Modifier.width(RANK_WIDTH.dp))
    val depletionAge = run.depletionAge
    Cell(
      text =
        if (depletionAge == null) {
          Strings.monteCarloRunsSurvived
        } else {
          Strings.monteCarloRunsRanOut(depletionAge)
        },
      modifier = Modifier.weight(1f),
      color =
        if (depletionAge == null) colors.reportsNumberPositive else colors.reportsNumberNegative,
    )
    Cell(
      text = if (depletionAge == null) run.endingBalance.formattedString() else "-",
      modifier = Modifier.weight(1f),
      textAlign = End,
    )
    Cell(run.totalWithdrawn.formattedString(), Modifier.weight(1f), textAlign = End)
  }
}

@Composable
private fun Cell(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = colors.pageText,
  textAlign: TextAlign? = null,
) =
  Text(
    modifier = modifier,
    text = text,
    style = typography.bodyMedium,
    color = color,
    textAlign = textAlign,
  )

private const val PAGE_SIZE = 20
private const val RANK_WIDTH = 48
