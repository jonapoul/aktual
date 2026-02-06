package aktual.api.client

import alakazam.kotlin.StateHolder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class AktualApisStateHolder : StateHolder<AktualApis?>(initialState = null) {
  override var value: AktualApis?
    get() = super.value
    set(value) {
      super.value?.close()
      super.value = value
    }

  override fun compareAndSet(expect: AktualApis?, update: AktualApis?): Boolean {
    value?.close()
    return super.compareAndSet(expect, update)
  }
}
