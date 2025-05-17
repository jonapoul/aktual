package actual.api.builder

import alakazam.kotlin.logging.Logger as AlakazamLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal class ActualKtorLogger(private val tag: String?) : KtorLogger {
  private val delegate = if (tag == null) AlakazamLogger else AlakazamLogger.tag(tag)
  override fun log(message: String) = delegate.v(message)
}
