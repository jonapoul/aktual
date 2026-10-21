package aktual.prefs

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class AppLockTimeout(val duration: Duration) {
  Immediately(Duration.ZERO),
  Seconds30(30.seconds),
  Minutes1(1.minutes),
  Minutes5(5.minutes),
  Minutes15(15.minutes);

  companion object {
    val Default = Immediately
  }
}
