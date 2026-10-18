@file:Suppress("MagicNumber")

package aktual.budget.home.ui

import aktual.budget.home.vm.ReportsCardState
import aktual.budget.home.vm.ReportsCardState.Loaded
import aktual.budget.model.Amount
import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.pinned.PinnedReportChart
import aktual.budget.reports.ui.pinned.displayName
import aktual.budget.reports.vm.NetWorthData
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.budget.reports.vm.pinned.PinnedReport
import aktual.budget.reports.vm.pinned.PinnedReports.Companion.MAX_PINNED_REPORTS
import aktual.budget.reports.vm.pinned.ReportPage
import aktual.budget.reports.vm.pinned.ReportSummary
import aktual.core.icons.material.Add
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SwapHoriz
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BareTextButton
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth

private val RowMinHeight = 48.dp

// Shows nothing when no reports are pinned
@Composable
internal fun ReportsCard(
  state: ReportsCardState,
  choices: ImmutableList<ReportPage>,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  if (state == Empty) return

  var isManaging by rememberSaveable { mutableStateOf(false) }

  HomeCard(modifier = modifier) {
    when (state) {
      Empty -> Unit
      Loading -> ShimmerReports()
      Failed -> CardError(message = Strings.homeReportsFailed, onAction = onAction)
      is Loaded ->
        ReportsContent(state = state, onAction = onAction, onManage = { isManaging = true })
    }
  }

  if (isManaging && state is Loaded) {
    ManageReportsSheet(
      state = state,
      choices = choices,
      onAction = onAction,
      onDismiss = { isManaging = false },
    )
  }
}

@Composable
private fun ReportsContent(
  state: Loaded,
  onAction: HomeActionHandler,
  onManage: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    ReportsHeader(onManage = onManage)

    state.reports.fastForEachIndexed { index, report ->
      if (index > 0) HorizontalDivider(color = colors.tableBorder)
      PinnedReportChart(
        modifier =
          Modifier.fillMaxWidth()
            .hoverClickable { onAction(OpenReport(report.summary.id)) }
            .padding(horizontal = CardPadding - ChartInset),
        data = report.data,
      )
    }
  }
}

@Composable
private fun ShimmerReports(modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth()) {
    ReportsHeader(onManage = null)
    PinnedReportChart(modifier = Modifier.padding(horizontal = CardPadding), data = null)
  }
}

@Composable
private fun ReportsHeader(onManage: (() -> Unit)?, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = CardPadding),
    horizontalArrangement = spacedBy(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      modifier = Modifier.weight(1f).semantics { heading() },
      text = Strings.homeCardReports,
      style = typography.titleSmall,
      fontWeight = SemiBold,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )

    if (onManage != null) {
      BareTextButton(text = Strings.homeReportsManage, onClick = onManage)
    }
  }
}

// Picking either fills a free slot or, with [replacing], takes that report's place
private sealed interface SheetPage {
  data object Pinned : SheetPage

  data class Picking(val replacing: WidgetId?) : SheetPage
}

@Composable
private fun ManageReportsSheet(
  state: Loaded,
  choices: ImmutableList<ReportPage>,
  onAction: HomeActionHandler,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var page by remember { mutableStateOf<SheetPage>(Pinned) }

  AktualModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
    when (val current = page) {
      Pinned ->
        PinnedContent(
          state = state,
          onAction = onAction,
          onPick = { replacing -> page = SheetPage.Picking(replacing) },
        )

      is Picking ->
        PickerContent(
          choices = choices,
          pinned = state.reports,
          onBack = { page = Pinned },
          onPick = { id ->
            val replacing = current.replacing
            onAction(if (replacing == null) PinReport(id) else ReplaceReport(replacing, id))
            page = Pinned
          },
        )
    }
  }
}

@Composable
private fun PinnedContent(
  state: Loaded,
  onAction: HomeActionHandler,
  onPick: (replacing: WidgetId?) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().verticalScrollWithBar().padding(bottom = 20.dp),
  ) {
    SheetTitle(
      title = Strings.homeReportsSheetTitle,
      message = Plurals.homeReportsSheetMessage(MAX_PINNED_REPORTS, MAX_PINNED_REPORTS),
    )

    state.reports.fastForEach { report ->
      val name = report.summary.displayName()
      Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = RowMinHeight).padding(start = 16.dp),
        verticalAlignment = CenterVertically,
      ) {
        Text(
          modifier = Modifier.weight(1f),
          text = name,
          style = typography.bodyLarge,
          color = colors.pageText,
          maxLines = 1,
          overflow = Ellipsis,
        )

        BareIconButton(
          imageVector = MaterialIcons.SwapHoriz,
          contentDescription = Strings.homeReportsSwap(name),
          onClick = { onPick(report.summary.id) },
        )

        BareIconButton(
          imageVector = MaterialIcons.Clear,
          contentDescription = Strings.homeReportsRemove(name),
          onClick = { onAction(UnpinReport(report.summary.id)) },
        )
      }
    }

    if (state.canPinMore) {
      BottomSheetListItem(
        label = Strings.homeReportsAdd,
        isSelected = false,
        leadingContent = { BottomSheetIcon(MaterialIcons.Add) },
        onClick = { onPick(null) },
      )
    }
  }
}

// Reports already on the home page aren't offered again
@Composable
private fun PickerContent(
  choices: ImmutableList<ReportPage>,
  pinned: ImmutableList<PinnedReport>,
  onBack: () -> Unit,
  onPick: (WidgetId) -> Unit,
  modifier: Modifier = Modifier,
) {
  val available =
    remember(choices, pinned) {
      val pinnedIds = pinned.mapTo(HashSet()) { it.summary.id }
      choices
        .map { page ->
          page.copy(reports = page.reports.filter { it.id !in pinnedIds }.toImmutableList())
        }
        .filter { it.reports.isNotEmpty() }
        .toImmutableList()
    }

  Column(
    modifier = modifier.fillMaxWidth().verticalScrollWithBar().padding(bottom = 20.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
      verticalAlignment = CenterVertically,
    ) {
      SheetTitle(modifier = Modifier.weight(1f), title = Strings.homeReportsChoose, message = null)
      BareTextButton(text = Strings.homeReportsBack, onClick = onBack)
    }

    if (available.isEmpty()) {
      Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = Strings.homeReportsNoneToChoose,
        style = typography.bodyMedium,
        color = colors.pageTextSubdued,
      )
    }

    available.fastForEach { page ->
      // A single page needs no heading
      if (available.size > 1) {
        Text(
          modifier =
            Modifier.padding(horizontal = 16.dp).padding(top = 12.dp).semantics { heading() },
          text = page.page.name,
          style = typography.titleSmall,
          fontWeight = SemiBold,
          color = colors.pageTextSubdued,
        )
      }

      page.reports.fastForEach { report ->
        BottomSheetListItem(
          label = report.displayName(),
          isSelected = false,
          onClick = { onPick(report.id) },
        )
      }
    }
  }
}

@Composable
private fun SheetTitle(title: String, message: String?, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
    verticalArrangement = spacedBy(4.dp),
  ) {
    Text(
      modifier = Modifier.semantics { heading() },
      text = title,
      style = typography.headlineSmall,
    )

    if (message != null) {
      Text(text = message, style = typography.bodyMedium, color = colors.pageTextSubdued)
    }
  }
}

// The chart pads itself, so the card's padding is trimmed to line it up with the header
private val ChartInset = 8.dp

private const val PREVIEW_NET_WORTH = "Net worth"
private const val PREVIEW_SAVINGS = "Savings"

private fun previewReport(id: String, name: String?) =
  PinnedReport(
    ReportSummary(WidgetId(id), NetWorth, name),
    NetWorthData(
      title = name,
      items =
        persistentMapOf(
          YearMonth(2026, 1) to Amount(1_200_00),
          YearMonth(2026, 2) to Amount(1_450_00),
          YearMonth(2026, 3) to Amount(1_380_00),
          YearMonth(2026, 4) to Amount(1_720_00),
        ),
    ),
  )

internal val PREVIEW_REPORTS =
  Loaded(
    reports =
      persistentListOf(previewReport("a", PREVIEW_NET_WORTH), previewReport("b", PREVIEW_SAVINGS)),
    canPinMore = true,
  )

internal val PREVIEW_REPORT_CHOICES =
  persistentListOf(
    ReportPage(
      DashboardPage(DashboardPageId("main"), "Main"),
      persistentListOf(
        ReportSummary(WidgetId("a"), NetWorth, PREVIEW_NET_WORTH),
        ReportSummary(WidgetId("b"), NetWorth, PREVIEW_SAVINGS),
        ReportSummary(WidgetId("c"), CashFlow, name = null),
      ),
    ),
  )

@Preview
@Composable
private fun PreviewReportsCard(
  @PreviewParameter(ReportsCardStateProvider::class) params: ColoredParams<ReportsCardState>,
) =
  PreviewWithColoredParams(params) {
    ReportsCard(
      modifier = Modifier.padding(16.dp),
      state = this,
      choices = PREVIEW_REPORT_CHOICES,
      onAction = {},
    )
  }

@Preview
@Composable
private fun PreviewPinnedContent(
  @PreviewParameter(PinnedContentProvider::class) params: ColoredParams<Loaded>,
) = PreviewWithColoredParams(params) { PinnedContent(state = this, onAction = {}, onPick = {}) }

@Preview
@Composable
private fun PreviewPickerContent(
  @PreviewParameter(PickerContentProvider::class) params: ColoredParams<ImmutableList<ReportPage>>,
) =
  PreviewWithColoredParams(params) {
    PickerContent(choices = this, pinned = PREVIEW_REPORTS.reports, onBack = {}, onPick = {})
  }

private class ReportsCardStateProvider :
  ColoredParameterProvider<ReportsCardState>(PREVIEW_REPORTS, Loading, Failed)

private class PinnedContentProvider :
  ColoredParameterProvider<Loaded>(
    PREVIEW_REPORTS,
    Loaded(
      reports =
        persistentListOf(
          previewReport("a", PREVIEW_NET_WORTH),
          previewReport("b", PREVIEW_SAVINGS),
          previewReport("c", name = null),
        ),
      canPinMore = false,
    ),
  )

private class PickerContentProvider :
  ColoredParameterProvider<ImmutableList<ReportPage>>(PREVIEW_REPORT_CHOICES, persistentListOf())
