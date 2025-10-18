/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.budget.encryption

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
      val result = current!!.read(sink, byteCount)
      if (result != -1L) {
        return result
      } else {
        current!!.close()
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
