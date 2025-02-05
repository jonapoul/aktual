package actual.core.coroutines

import alakazam.test.core.MainDispatcherRule
import kotlin.coroutines.CoroutineContext

class TestCoroutineContexts(
  context: CoroutineContext,
) : CoroutineContexts {
  override val main = context
  override val io = context
  override val default = context

  constructor(mainDispatcherRule: MainDispatcherRule) : this(mainDispatcherRule.dispatcher)
}
