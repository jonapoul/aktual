package aktual.core.utils

import aktual.core.model.Token
import kotlinx.coroutines.flow.Flow

// Catches the server sending the user back to the app after an OpenID sign-in in the browser
interface OpenIdRedirects {
  val tokens: Flow<Token>

  // Where the server sends the new session token. Starts listening for it if needed
  suspend fun returnUrl(): String

  companion object {
    const val HOST = "localhost"
    const val PATH = "/openid-cb"
    const val TOKEN = "token"
  }
}
