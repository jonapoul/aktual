package aktual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

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
    assertThat(SyncedPrefKey.decode(key.key))
      .isEqualTo(key)
  }
}
