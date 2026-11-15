package aktual.core.appicon

import aktual.di.AppCoroutineScope
import aktual.di.AppScope
import aktual.prefs.AppIcon
import aktual.prefs.AppIconController
import aktual.prefs.Preference
import aktual.prefs.Translator
import aktual.prefs.translated
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

// The window reads current to pick its icon, so a change shows up straight away
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DesktopAppIconController
internal constructor(dataStore: DataStore<Preferences>, scope: AppCoroutineScope) :
  AppIconController {
  private val preference: Preference<AppIcon> =
    dataStore
      .translated(key = stringPreferencesKey("appIcon"), default = AppIcon.Default, translator = Names)
      .required()

  override val current: StateFlow<AppIcon> =
    preference.asFlow().stateIn(scope, Eagerly, runBlocking { preference.get() })

  override suspend fun set(icon: AppIcon) = preference.set(icon)

  // Stored by name so reordering the enum doesn't change anyone's icon
  private object Names : Translator<String, AppIcon> {
    override fun encode(value: AppIcon): String = value.name

    override fun decode(value: String): AppIcon? = AppIcon.entries.firstOrNull { it.name == value }
  }
}
