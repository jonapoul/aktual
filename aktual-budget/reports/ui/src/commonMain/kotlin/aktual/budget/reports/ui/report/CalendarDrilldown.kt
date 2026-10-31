package aktual.budget.reports.ui.report

import aktual.budget.reports.components.ChartAction.ClickCalendarDay
import aktual.budget.reports.vm.dayFilter
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.LocalDateFormatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

// Opens the transactions behind the tapped calendar day, titled with its date
@Composable
internal fun rememberCalendarDrilldown(
  toTransactions: TransactionsNavigator,
): (ClickCalendarDay) -> Unit {
  val formatter = LocalDateFormatter.current
  val open by rememberUpdatedState { click: ClickCalendarDay ->
    toTransactions(formatter.format(click.date), click.data.dayFilter(click.date))
  }
  return { open(it) }
}
