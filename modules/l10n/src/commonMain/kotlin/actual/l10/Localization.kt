package actual.l10

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

@Immutable
interface Localization {
  fun string(id: Int): String
  fun string(id: Int, vararg args: Any): String
  fun dimen(id: Int): Dp

  companion object : Localization {
    private var nullableInstance: Localization? = null
    private val instance: Localization get() = nullableInstance ?: error("No Localization instance registered")

    fun get(): Localization = instance

    fun set(instance: Localization) {
      nullableInstance = instance
    }

    fun reset() {
      nullableInstance = null
    }

    override fun string(id: Int) = instance.string(id)
    override fun string(id: Int, vararg args: Any) = instance.string(id, *args)
    override fun dimen(id: Int) = instance.dimen(id)
  }
}
