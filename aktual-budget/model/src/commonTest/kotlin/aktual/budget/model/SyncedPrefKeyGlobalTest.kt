@file:Suppress("JUnitMalformedDeclaration")

package aktual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

@Burst
class SyncedPrefKeyGlobalTest {
  @Test
  fun decoding(
    key: SyncedPrefKey.Global =
      burstValues(
        SyncedPrefKey.Global.BudgetType,
        SyncedPrefKey.Global.DateFormat,
        FirstDayOfWeekIdx,
        HideFraction,
        IsPrivacyEnabled,
        LearnCategories,
        SyncedPrefKey.Global.NumberFormat,
        UpcomingScheduledTransactionLength,
      ),
  ) {
    assertThat(SyncedPrefKey.decode(key.key)).isEqualTo(key)
  }
}
