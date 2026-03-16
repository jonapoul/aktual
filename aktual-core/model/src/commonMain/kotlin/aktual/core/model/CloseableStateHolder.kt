package aktual.core.model

import alakazam.kotlin.StateHolder

open class CloseableStateHolder<T>(initialState: T, private val onClose: (T) -> Unit) :
  StateHolder<T>(initialState) {
  override var value: T
    get() = super.value
    set(new) {
      val old = value
      super.value = new
      if (old !== new) onClose(old)
    }

  override fun compareAndSet(expect: T, update: T): Boolean {
    val replaced = super.compareAndSet(expect, update)
    if (replaced && expect !== update) onClose(expect)
    return replaced
  }
}
