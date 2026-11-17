package aktual.app.di

import aktual.core.l10n.Res
import aktual.core.l10n.StringLookup
import aktual.core.l10n.server_url_local_network_blocked
import aktual.core.utils.LocalNetworkBlockedException
import aktual.core.utils.LocalNetworkPermission
import aktual.core.utils.LocalNetworkPrompt
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import okio.IOException

// Holds requests while the startup permission prompt is showing, so they go through once it's
// given instead of timing out. Turns failures that the missing permission probably caused into a
// LocalNetworkBlockedException, so every screen shows why rather than a raw timeout
internal fun localNetworkPlugin(
  permission: LocalNetworkPermission,
  prompt: LocalNetworkPrompt,
  strings: StringLookup,
) =
  createClientPlugin("LocalNetwork") {
    // Before sending, so the wait doesn't count against HttpTimeout, which starts timing on Send
    onRequest { _, _ -> prompt.awaitAnswer() }

    on(Send) { request ->
      try {
        proceed(request)
      } catch (e: IOException) {
        val host = request.url.host.removeSurrounding("[", "]")
        if (!permission.isGranted() && permission.isRequiredForHost(host)) {
          throw LocalNetworkBlockedException(
            strings[Res.string.server_url_local_network_blocked],
            e,
          )
        }
        throw e
      }
    }
  }
