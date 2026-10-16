package aktual.prefs

import aktual.budget.model.ScheduleSort
import aktual.budget.model.SortDirection
import aktual.di.AppScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class SchedulePreferencesImpl(dataStore: DataStore<Preferences>) : SchedulePreferences {
  override val showCompleted: Preference<Boolean> =
    dataStore
      .boolean(key = booleanPreferencesKey("showCompletedSchedules"), default = false)
      .required()

  override val sortField: Preference<ScheduleSort.Field> =
    dataStore
      .translated(
        key = stringPreferencesKey("scheduleSortField"),
        default = ScheduleSort.Field.Default,
        translator = enumStringTranslator(),
      )
      .required()

  override val sortDirection: Preference<SortDirection> =
    dataStore
      .translated(
        key = stringPreferencesKey("scheduleSortDirection"),
        default = SortDirection.Default,
        translator = enumStringTranslator(),
      )
      .required()
}
