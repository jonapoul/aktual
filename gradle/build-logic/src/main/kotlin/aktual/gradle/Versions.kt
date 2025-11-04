package aktual.gradle

import java.time.LocalDate

fun versionName(): String = with(LocalDate.now()) {
  "%04d.%02d.%02d".format(year, monthValue, dayOfMonth)
}
