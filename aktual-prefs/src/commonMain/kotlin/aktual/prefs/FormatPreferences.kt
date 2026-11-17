package aktual.prefs

import aktual.core.model.DistanceUnit

interface FormatPreferences {
  val distanceUnit: Preference<DistanceUnit>
}
