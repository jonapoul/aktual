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
package actual.core.model

import kotlinx.serialization.Serializable
import java.util.stream.IntStream
import kotlin.io.encoding.Base64

@JvmInline
@Serializable
value class Base64String(val value: String) : Comparable<Base64String>, CharSequence by value {
  constructor(bytes: ByteArray) : this(Base64.encode(bytes))

  override fun toString() = value
  override fun compareTo(other: Base64String) = value.compareTo(other.value)

  fun decode(): ByteArray = Base64.decode(value)
  fun toCharArray() = value.toCharArray()
  fun toByteArray() = value.toByteArray(Charsets.UTF_8)

  override fun chars(): IntStream = value.chars()
  override fun codePoints(): IntStream = value.codePoints()
}

val String.base64 get() = Base64String(this)
val ByteArray.base64 get() = Base64String(this)
