package actual.core.coroutines

import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface CoroutineContexts {
  val io: CoroutineContext
  val main: CoroutineContext
  val default: CoroutineContext
}

@Suppress("InjectDispatcher")
class DefaultCoroutineContexts @Inject constructor() : CoroutineContexts {
  override val main = Dispatchers.Main
  override val io = Dispatchers.IO
  override val default = Dispatchers.Default
}
