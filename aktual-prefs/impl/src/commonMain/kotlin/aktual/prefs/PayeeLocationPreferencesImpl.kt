package aktual.prefs

import aktual.budget.model.PayeeLocationSort
import aktual.budget.model.SortDirection
import aktual.di.AppScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class PayeeLocationPreferencesImpl(dataStore: DataStore<Preferences>) : PayeeLocationPreferences {
  override val sortField: Preference<PayeeLocationSort.Field> =
    dataStore
      .translated(
        key = stringPreferencesKey("payeeLocationSortField"),
        default = PayeeLocationSort.Field.Default,
        translator = enumStringTranslator(),
      )
      .required()

  override val sortDirection: Preference<SortDirection> =
    dataStore
      .translated(
        key = stringPreferencesKey("payeeLocationSortDirection"),
        default = SortDirection.Default,
        translator = enumStringTranslator(),
      )
      .required()
}
