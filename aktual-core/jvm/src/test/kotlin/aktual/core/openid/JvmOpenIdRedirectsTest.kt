package aktual.core.openid

import aktual.core.model.Token
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class JvmOpenIdRedirectsTest {
  private val redirects = JvmOpenIdRedirects(strings = { "Done" })
  private val client = HttpClient.newHttpClient()

  @Test
  fun `Passes on the token from the callback`() = runBlocking {
    val returnUrl = redirects.returnUrl()
    assertTrue(returnUrl.startsWith("http://localhost:"))

    val response = get("$returnUrl/openid-cb?token=abc-123")

    assertEquals(200, response.statusCode())
    assertEquals("Done", response.body())
    assertEquals(Token("abc-123"), redirects.tokens.first())
  }

  @Test
  fun `Rejects a callback without a token`() = runBlocking {
    val returnUrl = redirects.returnUrl()

    assertEquals(400, get("$returnUrl/openid-cb").statusCode())
    assertEquals(returnUrl, redirects.returnUrl())
  }

  private fun get(url: String) =
    client.send(HttpRequest.newBuilder(URI(url)).build(), BodyHandlers.ofString())
}
