package aktual.budget.model

import app.cash.burst.Burst
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

@Burst
class SyncedPrefKeyFeatureFlagTest {
  @Test
  fun decoding(flag: SyncedPrefKey.FeatureFlag) {
    assertThat(SyncedPrefKey.decode(flag.key)).isEqualTo(flag)
  }
}
