package aktual.core.api

import logcat.logcat
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal class AktualKtorLogger(private val tag: String?) : KtorLogger {
  override fun log(message: String) = logcat.v(tag) { message }
}
