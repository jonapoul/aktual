package actual.api.client

import alakazam.kotlin.core.StateHolder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class ActualApisStateHolder : StateHolder<ActualApis?>(initialState = null) {
  override var value: ActualApis?
    get() = super.value
    set(value) {
      super.value?.close()
      super.value = value
    }

  override fun compareAndSet(expect: ActualApis?, update: ActualApis?): Boolean {
    value?.close()
    return super.compareAndSet(expect, update)
  }
}
