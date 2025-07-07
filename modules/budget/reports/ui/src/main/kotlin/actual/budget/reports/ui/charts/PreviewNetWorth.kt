package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.reports.ui.date
import actual.budget.reports.vm.NetWorthData
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.datetime.Month

internal object PreviewNetWorth {
  val DATA = NetWorthData(
    items = persistentMapOf(
      date(2023, Month.JANUARY) to Amount(-44_000.00),
      date(2024, Month.JANUARY) to Amount(-18_000.00),
      date(2024, Month.FEBRUARY) to Amount(21_000.34),
      date(2024, Month.MARCH) to Amount(25_000.67),
      date(2024, Month.JULY) to Amount(-10123.98),
      date(2024, Month.AUGUST) to Amount(-5123.69),
      date(2024, Month.DECEMBER) to Amount(-6789.12),
      date(2025, Month.JANUARY) to Amount(198.0),
      date(2025, Month.FEBRUARY) to Amount(1.98),
      date(2025, Month.MARCH) to Amount(256.0),
      date(2025, Month.JUNE) to Amount(30000.0),
    ),
  )

  val POSITIVE_DATA = DATA.copy(
    items = DATA
      .items
      .mapValues { (_, amount) -> amount + Amount(100_000.0) }
      .toPersistentMap(),
  )

  val NEGATIVE_DATA = DATA.copy(
    items = DATA
      .items
      .mapValues { (_, amount) -> amount - Amount(100_000.0) }
      .toPersistentMap(),
  )
}
