package aktual.prefs

import aktual.core.model.DistanceUnit
import aktual.di.AppScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class FormatPreferencesImpl(dataStore: DataStore<Preferences>) : FormatPreferences {
  override val distanceUnit: Preference<DistanceUnit> =
    dataStore
      .translated(
        key = intPreferencesKey("distanceUnit"),
        default = DistanceUnit.Default,
        translator = enumOrdinalTranslator(),
      )
      .required()
}
