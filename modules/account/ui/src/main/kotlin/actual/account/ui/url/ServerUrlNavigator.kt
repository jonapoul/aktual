package actual.account.ui.url

import androidx.compose.runtime.Immutable

@Immutable
interface ServerUrlNavigator {
  fun toLogin()
  fun toAbout()
}
