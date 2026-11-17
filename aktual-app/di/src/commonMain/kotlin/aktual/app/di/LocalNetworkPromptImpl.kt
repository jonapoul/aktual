package aktual.app.di

import aktual.core.utils.LocalNetworkPrompt
import aktual.di.AppScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class LocalNetworkPromptImpl internal constructor() : LocalNetworkPrompt {
  private val isShowing = MutableStateFlow(value = false)

  // The timeout stops requests hanging forever if the result never arrives
  override suspend fun awaitAnswer() {
    val answered = withTimeoutOrNull(MAX_WAIT) { isShowing.first { !it } }
    if (answered == null) log.w { "Gave up waiting for local network permission answer" }
  }

  override fun onShowing() = isShowing.update { true }

  override fun onAnswered() = isShowing.update { false }

  private companion object {
    val MAX_WAIT = 2.minutes
  }
}
