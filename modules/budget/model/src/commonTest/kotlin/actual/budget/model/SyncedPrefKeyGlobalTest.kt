package actual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import org.junit.Test
import kotlin.test.assertEquals

@Burst
class SyncedPrefKeyGlobalTest(
  private val key: SyncedPrefKey.Global = burstValues(
    SyncedPrefKey.Global.BudgetType,
    SyncedPrefKey.Global.DateFormat,
    SyncedPrefKey.Global.FirstDayOfWeekIdx,
    SyncedPrefKey.Global.HideFraction,
    SyncedPrefKey.Global.IsPrivacyEnabled,
    SyncedPrefKey.Global.LearnCategories,
    SyncedPrefKey.Global.NumberFormat,
    SyncedPrefKey.Global.UpcomingScheduledTransactionLength,
  ),
) {
  @Test
  fun decoding() {
    assertEquals(
      expected = key,
      actual = SyncedPrefKey.decode(key.key),
    )
  }
}
