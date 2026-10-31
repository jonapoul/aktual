package aktual.budget.reports.ui.report

import aktual.budget.reports.components.intervalLabels
import aktual.budget.reports.components.label
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomDrilldown
import aktual.budget.reports.vm.CustomGroup
import aktual.budget.reports.vm.drilldownFilter
import aktual.core.nav.TransactionsNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

// Opens the transactions behind the tapped part of [preview], titled with its group and interval
@Composable
internal fun rememberDrilldown(
  preview: ChartData?,
  toTransactions: TransactionsNavigator,
): (CustomDrilldown) -> Unit {
  val data = preview as? CustomData
  val groupNames = buildMap {
    data?.groups?.forEach { put(it.group, it.group.label()) }
    data?.categoryGroups?.forEach { group ->
      put(group.group, group.group.label())
      group.categories.forEach { put(it.group, it.group.label()) }
    }
  }
  val intervalNames = data?.let { intervalLabels(it) }.orEmpty()
  val open by rememberUpdatedState { drilldown: CustomDrilldown ->
    val filter = data?.drilldownFilter(drilldown)
    if (filter != null) {
      val title = drilldownTitle(data, drilldown, groupNames, intervalNames)
      toTransactions(title, filter)
    }
  }
  return { open(it) }
}

private fun drilldownTitle(
  data: CustomData,
  drilldown: CustomDrilldown,
  groupNames: Map<CustomGroup, String>,
  intervalNames: List<String>,
): String {
  val interval = data.intervals.indexOfFirst { it.interval == drilldown.interval }
  return listOfNotNull(
      drilldown.group?.let(groupNames::get),
      intervalNames.getOrNull(interval),
    )
    .filter { it.isNotBlank() }
    .ifEmpty { listOf(data.title) }
    .joinToString(" · ")
}
