package actual.budget.model

import alakazam.kotlin.core.StateHolder

class MutableDbMetadata(
  initial: DbMetadata,
  private val onChange: (DbMetadata) -> Unit = {},
) : StateHolder<DbMetadata>(initial) {
  override var value: DbMetadata
    get() = super.value
    set(value) {
      super.value = value
      onChange(value)
    }

  override fun compareAndSet(expect: DbMetadata, update: DbMetadata): Boolean = super
    .compareAndSet(expect, update)
    .also { success -> if (success) onChange(update) }
}
