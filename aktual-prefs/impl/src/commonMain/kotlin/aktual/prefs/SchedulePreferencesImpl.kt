package aktual.prefs

import aktual.di.AppScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class SchedulePreferencesImpl(dataStore: DataStore<Preferences>) : SchedulePreferences {
  override val showCompleted: Preference<Boolean> =
    dataStore
      .boolean(key = booleanPreferencesKey("showCompletedSchedules"), default = false)
      .required()
}
