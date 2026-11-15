package aktual.core.appicon

import aktual.di.AppScope
import aktual.prefs.AppIcon
import aktual.prefs.AppIconController
import alakazam.kotlin.CoroutineContexts
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

// Each icon is an activity-alias in the manifest with its own launcher intent filter. Exactly one
// is enabled at a time, and the enabled one is the source of truth, so there's no stored pref to
// fall out of sync with it.
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AndroidAppIconController
internal constructor(private val context: Context, private val contexts: CoroutineContexts) :
  AppIconController {
  private val mutableCurrent by lazy { MutableStateFlow(readEnabled()) }

  override val current: StateFlow<AppIcon>
    get() = mutableCurrent

  override suspend fun set(icon: AppIcon) {
    withContext(contexts.io) {
      // Enable the new alias before disabling the old one, so there's always a launcher entry
      setState(icon, COMPONENT_ENABLED_STATE_ENABLED)
      AppIcon.entries
        .filter { it != icon }
        .forEach { setState(it, COMPONENT_ENABLED_STATE_DISABLED) }
    }
    mutableCurrent.update { icon }
  }

  private fun readEnabled(): AppIcon =
    AppIcon.entries.firstOrNull { icon ->
      when (context.packageManager.getComponentEnabledSetting(icon.component())) {
        COMPONENT_ENABLED_STATE_ENABLED -> true
        // Only the default alias is enabled in the manifest
        COMPONENT_ENABLED_STATE_DEFAULT -> icon == Default
        else -> false
      }
    } ?: Default

  private fun setState(icon: AppIcon, state: Int) =
    context.packageManager.setComponentEnabledSetting(icon.component(), state, DONT_KILL_APP)

  private fun AppIcon.component() = ComponentName(context, "aktual.app.android.Launcher$name")
}
