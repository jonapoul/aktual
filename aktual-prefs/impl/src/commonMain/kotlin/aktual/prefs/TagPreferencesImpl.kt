package aktual.prefs

import aktual.budget.model.TagSort
import aktual.di.AppScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class TagPreferencesImpl(dataStore: DataStore<Preferences>) : TagPreferences {
  override val lastUsedTagColor: NullablePreference<String> =
    dataStore.string(key = stringPreferencesKey("lastUsedTagColor"), default = null)

  override val sortField: Preference<TagSort.Field> =
    dataStore
      .translated(
        key = stringPreferencesKey("tagSortField"),
        default = TagSort.Field.Default,
        translator = enumStringTranslator(),
      )
      .required()

  override val sortDirection: Preference<TagSort.Direction> =
    dataStore
      .translated(
        key = stringPreferencesKey("tagSortDirection"),
        default = TagSort.Direction.Default,
        translator = enumStringTranslator(),
      )
      .required()
}
