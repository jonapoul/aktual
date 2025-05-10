package actual.api.client

import alakazam.kotlin.core.StateHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActualApisStateHolder @Inject constructor() : StateHolder<ActualApis?>(initialState = null) {
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
