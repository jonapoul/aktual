package actual.core.model

import kotlinx.datetime.TimeZone

fun interface TimeZones {
  fun current(): TimeZone

  companion object {
    val System = TimeZones { TimeZone.currentSystemDefault() }
    val Utc = TimeZones { TimeZone.UTC }
  }
}
