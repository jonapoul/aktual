package aktual.budget.encryption

import okio.Buffer
import okio.Source
import okio.Timeout

operator fun Source.plus(other: Source): Source = ConcatSource(this, other)

private class ConcatSource(sources: Iterable<Source>) : Source {
  @Deprecated("Not supported", level = DeprecationLevel.ERROR)
  constructor() : this(listOf(error("")))
  constructor(vararg sources: Source) : this(sources.toList())

  private val iterator = sources.iterator()
  private var current: Source? = iterator.nextOrNull()

  override fun timeout(): Timeout = Timeout.NONE

  override fun read(sink: Buffer, byteCount: Long): Long {
    while (current != null) {
      val result = current?.read(sink, byteCount) ?: -1L
      if (result != -1L) {
        return result
      } else {
        current?.close()
        current = iterator.nextOrNull()
      }
    }
    return -1L
  }

  override fun close() {
    current?.close()
    while (iterator.hasNext()) {
      iterator.next().close()
    }
  }

  private fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null
}
