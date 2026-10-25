package aktual.di

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Pass newScope() to the ViewModel constructor so tests can swap the dispatcher without setMain
fun interface ViewModelContext {
  fun context(): CoroutineContext

  companion object {
    val Default = ViewModelContext { Dispatchers.Main.immediate }
  }
}

fun ViewModelContext.newScope(): CoroutineScope = CoroutineScope(context() + SupervisorJob())
