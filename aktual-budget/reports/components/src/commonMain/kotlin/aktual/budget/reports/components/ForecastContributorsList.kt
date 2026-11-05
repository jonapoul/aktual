package aktual.budget.reports.components

import aktual.budget.model.Amount
import aktual.budget.model.ScheduleId
import aktual.budget.model.sum
import aktual.budget.reports.vm.ForecastContributor
import aktual.budget.reports.vm.ForecastContributors
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.formattedText
import aktual.core.ui.verticalScrollWithBar
import alakazam.compose.HorizontalSpacer
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ForecastContributorsSheet(
  contributors: ForecastContributors,
  onEditSchedule: (ScheduleId) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) =
  AktualModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
    ForecastContributorsList(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
      contributors = contributors,
      onEditSchedule = onEditSchedule,
    )
  }

// packages/desktop-client/src/components/reports/reports/BalanceForecastContributors.tsx
@Composable
internal fun ForecastContributorsList(
  contributors: ForecastContributors,
  onEditSchedule: (ScheduleId) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier.verticalScrollWithBar()) {
    Text(
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
      text = Strings.reportsBalanceForecastContributorsTitle,
      color = colors.pageText,
      style = typography.bodyLarge,
      fontWeight = Bold,
    )

    Text(
      modifier = Modifier.padding(horizontal = 4.dp),
      text = Strings.reportsBalanceForecastContributorsDescription,
      color = colors.pageTextSubdued,
      style = typography.bodySmall,
    )

    VerticalSpacer(8.dp)

    AmountRow(name = Strings.reportsBalanceForecastOpening, amount = contributors.openingBalance)
    AmountRow(
      name = Strings.reportsBalanceForecastRecordedNet,
      amount = contributors.recordedNet,
      includeSign = true,
    )
    AmountRow(
      name = Strings.reportsBalanceForecastScheduledNet,
      amount = contributors.scheduledNet,
      includeSign = true,
    )
    AmountRow(
      name = Strings.reportsBalanceForecastEnding,
      amount = contributors.endingBalance,
      isBold = true,
    )

    VerticalSpacer(12.dp)

    ContributorsTable(
      title = Strings.reportsBalanceForecastInflows,
      contributors = contributors.inflows,
      onEditSchedule = onEditSchedule,
    )

    VerticalSpacer(12.dp)

    ContributorsTable(
      title = Strings.reportsBalanceForecastOutflows,
      contributors = contributors.outflows,
      onEditSchedule = onEditSchedule,
    )

    val transfers = contributors.internalTransferCount
    if (transfers > 0) {
      Text(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        text = Plurals.reportsBalanceForecastInternalTransfers(transfers, transfers),
        color = colors.pageTextSubdued,
        style = typography.bodySmall,
      )
    }

    BottomSpacing()
  }

// packages/desktop-client/src/components/reports/reports/ForecastContributorsTable.tsx
@Composable
private fun ContributorsTable(
  title: String,
  contributors: ImmutableList<ForecastContributor>,
  onEditSchedule: (ScheduleId) -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier) {
    var showAll by rememberSaveable(contributors) { mutableStateOf(false) }
    val visible = if (showAll) contributors else contributors.take(TOP_COUNT)
    val remaining = contributors.drop(visible.size)

    Text(
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
      text = title,
      color = colors.pageText,
      style = typography.bodyMedium,
      fontWeight = Bold,
    )

    visible.fastForEach { contributor ->
      Divider()
      ContributorRow(contributor = contributor, onClick = { onEditSchedule(contributor.id) })
    }

    if (remaining.isNotEmpty()) {
      Divider()
      val count = remaining.size
      val occurrences = remaining.sumOf { it.count }
      AmountRow(
        name = Plurals.reportsBalanceForecastOtherSchedules(count, count),
        subtitle = Plurals.reportsBalanceForecastOccurrences(occurrences, occurrences),
        amount = remaining.map { it.amount }.sum(),
        nameColor = colors.pageTextSubdued,
      )
    }

    if (contributors.isEmpty()) {
      Text(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        text = Strings.reportsBalanceForecastNoContributors,
        color = colors.pageTextSubdued,
        style = typography.bodyMedium,
      )
    }

    Divider()
    AmountRow(
      name = Strings.reportsBalanceForecastTotal,
      amount = contributors.map { it.amount }.sum(),
      isBold = true,
    )

    if (contributors.size > TOP_COUNT) {
      LinkChip(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        text =
          if (showAll) {
            Strings.reportsBalanceForecastShowTop
          } else {
            Strings.reportsBalanceForecastShowAll
          },
        onClick = { showAll = !showAll },
      )
    }
  }

@Composable
private fun ContributorRow(contributor: ForecastContributor, onClick: () -> Unit) {
  val details = buildList {
    add(Plurals.reportsBalanceForecastOccurrences(contributor.count, contributor.count))
    if (contributor.isAccountless) add(Strings.reportsBalanceForecastNoAccount)
    if (contributor.isTransfer) add(Strings.reportsBalanceForecastTransfer)
  }
  AmountRow(
    name = contributor.name?.ifBlank { null } ?: Strings.reportsBalanceForecastUnnamed,
    subtitle = details.joinToString(separator = " · "),
    amount = contributor.amount,
    onClickName = onClick,
  )
}

@Composable
private fun LinkChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) =
  Chip(
    modifier = modifier,
    text = text,
    size = Small,
    textStyle = typography.bodyMedium,
    colors =
      ChipDefaults.colors(
        background = Transparent,
        content = colors.pageText,
        border = colors.pillBorder,
      ),
    onClick = onClick,
    role = Button,
  )

@Composable
private fun AmountRow(
  name: String,
  amount: Amount,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  includeSign: Boolean = false,
  isBold: Boolean = false,
  nameColor: Color = colors.pageText,
  // Shows the name as a chip
  onClickName: (() -> Unit)? = null,
) =
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      if (onClickName == null) {
        Text(
          text = name,
          color = nameColor,
          style = typography.bodyMedium,
          fontWeight = if (isBold) Bold else null,
          maxLines = 2,
          overflow = Ellipsis,
        )
      } else {
        LinkChip(text = name, onClick = onClickName)
      }

      if (subtitle != null) {
        Text(text = subtitle, color = colors.pageTextSubdued, style = typography.bodySmall)
      }
    }

    HorizontalSpacer(8.dp)

    Text(
      text = amount.formattedText(includeSign = includeSign),
      color =
        when {
          amount == Zero -> colors.pageText
          amount.isPositive() -> colors.reportsNumberPositive
          else -> colors.reportsNumberNegative
        },
      style = typography.bodyMedium,
      fontWeight = if (isBold) Bold else null,
    )
  }

@Composable private fun Divider() = HorizontalDivider(color = colors.tableBorder)

private const val TOP_COUNT = 5

@Preview
@Composable
private fun PreviewForecastContributorsList(
  @PreviewParameter(ForecastContributorsProvider::class) params: ColorParams<ForecastContributors>,
) =
  PreviewWithColorParams(params) {
    ForecastContributorsList(
      modifier = Modifier.background(colors.tableBackground, CardShape).width(400.dp).padding(5.dp),
      contributors = this,
      onEditSchedule = {},
    )
  }

private class ForecastContributorsProvider :
  ColorParamsProvider<ForecastContributors>(
    PREVIEW_FORECAST_CONTRIBUTORS,
    PREVIEW_FORECAST_CONTRIBUTORS.copy(
      inflows = persistentListOf(),
      internalTransferCount = 0,
    ),
  )

private fun previewContributor(
  id: String,
  name: String?,
  amount: Double,
  count: Int,
  isTransfer: Boolean = false,
  isAccountless: Boolean = false,
) = ForecastContributor(ScheduleId(id), name, Amount(amount), count, isTransfer, isAccountless)

internal val PREVIEW_FORECAST_CONTRIBUTORS =
  ForecastContributors(
    openingBalance = Amount(2400.0),
    recordedNet = Amount(-150.0),
    scheduledNet = Amount(1650.0),
    endingBalance = Amount(3900.0),
    inflows = persistentListOf(previewContributor("salary", "Salary", 33080.0, count = 12)),
    outflows =
      persistentListOf(
        previewContributor("rent", "Rent", -18000.0, count = 12),
        previewContributor("savings", "Savings", -6000.0, count = 12, isTransfer = true),
        previewContributor("car", "Car loan", -4200.0, count = 12),
        previewContributor("power", null, -1800.0, count = 12, isAccountless = true),
        previewContributor("phone", "Phone", -600.0, count = 12),
        previewContributor("gym", "Gym", -480.0, count = 12),
        previewContributor("insurance", "Insurance", -350.0, count = 1),
      ),
    internalTransferCount = 2,
  )
