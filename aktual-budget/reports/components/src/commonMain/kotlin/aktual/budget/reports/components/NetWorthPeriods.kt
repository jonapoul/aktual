package aktual.budget.reports.components

import aktual.budget.model.Interval
import aktual.budget.reports.vm.NetWorthData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

// x values are indexes into the periods, since daily and weekly periods don't map onto months
internal fun NetWorthData.periodAt(x: Double): LocalDate? =
  items.keys.elementAtOrNull(x.roundToInt())

internal fun periodLabel(
  interval: Interval,
  period: LocalDate,
  months: ImmutableMap<Month, String>,
) =
  when (interval) {
    Daily,
    Weekly -> "${period.day} ${months[period.month]} ${period.year}"
    Yearly -> period.year.toString()
    Monthly,
    Quarterly,
    Unknown -> "${months[period.month]} ${period.year}"
  }

@Composable
internal fun netWorthXAxisFormatter(data: NetWorthData): CartesianValueFormatter {
  val months = monthStringsMap()
  return remember(data, months) {
    CartesianValueFormatter { _, value, _ ->
      val period = data.periodAt(value) ?: return@CartesianValueFormatter ""
      when (data.interval) {
        Daily,
        Weekly -> "${period.day} ${months[period.month]}"
        Yearly -> period.year.toString()
        Monthly,
        Quarterly,
        Unknown -> "${months[period.month]} ${period.year.toString().substring(startIndex = 2)}"
      }
    }
  }
}
