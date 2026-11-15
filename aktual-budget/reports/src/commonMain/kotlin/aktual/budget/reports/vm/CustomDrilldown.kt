package aktual.budget.reports.vm

import androidx.compose.runtime.Immutable

/**
 * The part of a custom report a tap drills down to: a [group] in an [interval]. A null [group]
 * covers every group, and a null [interval] the report's whole dates. [flow] narrows it to money in
 * or out, as the table's deposits and payments columns do
 */
@Immutable
data class CustomDrilldown(
  val group: CustomGroup? = null,
  val interval: CustomInterval? = null,
  val flow: CustomFlow? = null,
)

enum class CustomFlow {
  In,
  Out,
}

/** Budgeted reports have no transactions to show */
val CustomData.canDrillDown: Boolean
  get() = config.balanceType != Budgeted
