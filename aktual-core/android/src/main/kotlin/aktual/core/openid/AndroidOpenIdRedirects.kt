package aktual.core.openid

import aktual.core.model.Token
import aktual.core.utils.OpenIdRedirects
import aktual.core.utils.OpenIdRedirects.Companion.HOST
import aktual.core.utils.OpenIdRedirects.Companion.PATH
import aktual.core.utils.OpenIdRedirects.Companion.TOKEN
import aktual.di.AppScope
import android.content.Context
import android.content.Intent
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

// The manifest routes the app's package name scheme back to the activity, which passes it here
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AndroidOpenIdRedirects internal constructor(private val context: Context) : OpenIdRedirects {
  private val received = Channel<Token>(CONFLATED)

  override val tokens: Flow<Token> = received.receiveAsFlow()

  override suspend fun returnUrl(): String = "${context.packageName}://$HOST"

  fun onIntent(intent: Intent?) {
    val uri = intent?.data ?: return
    if (uri.scheme != context.packageName || uri.host != HOST || uri.path != PATH) return
    val token = uri.getQueryParameter(TOKEN)?.takeIf { it.isNotEmpty() } ?: return
    received.trySend(Token(token))
  }
}
