package aktual.prefs

import dev.jonpoulton.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

// TODO: https://github.com/jonapoul/aktual/issues/587
@Inject
@ContributesBinding(AppScope::class, binding<EncryptedPreferences>())
class JvmEncryptedPreferences(private val prefs: JvmPreferences) : EncryptedPreferences, Preferences by prefs
