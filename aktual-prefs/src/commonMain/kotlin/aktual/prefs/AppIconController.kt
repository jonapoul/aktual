package aktual.prefs

import kotlinx.coroutines.flow.StateFlow

// Bound per platform: Android swaps launcher aliases, desktop changes the window icon
interface AppIconController {
  val current: StateFlow<AppIcon>

  suspend fun set(icon: AppIcon)
}
