package aktual.core.api

import io.ktor.client.plugins.logging.Logger as KtorLogger
import logcat.logcat

internal class AktualKtorLogger(private val tag: String?) : KtorLogger {
  override fun log(message: String) = logcat.v(tag) { message }
}
