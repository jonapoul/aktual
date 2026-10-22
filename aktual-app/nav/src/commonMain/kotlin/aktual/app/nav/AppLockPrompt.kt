package aktual.app.nav

import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember

@Immutable data class AppLockPrompt(val title: String, val subtitle: String)

@Composable
fun rememberAppLockPrompt(): AppLockPrompt {
  val title = Strings.appLockPromptTitle
  val subtitle = Strings.appLockPromptSubtitle
  return remember(title, subtitle) { AppLockPrompt(title, subtitle) }
}
