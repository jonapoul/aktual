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
package aktual.budget.model

import alakazam.kotlin.serialization.SimpleSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Corresponds to packages/crdt/src/crdt/timestamp.ts#Timestamp
 */
@Serializable(Timestamp.Serializer::class)
data class Timestamp(
  val instant: Instant,
  val counter: Long,
  val node: String,
) {
  val milliseconds: Long get() = instant.toEpochMilliseconds()

  override fun toString(): String {
    val counter = counter.toString(radix = 16).uppercase()
    return listOf(
      instant.toString(),
      (BASE_COUNTER + counter).removeRange(startIndex = 0, endIndex = counter.length),
      "$BASE_NODE$node".removeRange(startIndex = 0, endIndex = 16),
    ).joinToString(SEPARATOR)
  }

  internal object Serializer : SimpleSerializer<Timestamp>(serialName = "Timestamp", Timestamp::parse)

  companion object {
    private const val BASE_COUNTER = "0000"
    private const val BASE_NODE = "0000000000000000"
    private const val MAX_COUNTER = 0xFFFF
    private const val MAX_NODE_LENGTH = 16
    private const val SEPARATOR = "-"

    val MAXIMUM = parse("9999-12-31T23:59:59.999Z-FFFF-FFFFFFFFFFFFFFFF")

    @Suppress("MagicNumber")
    fun parse(string: String): Timestamp {
      val parts = string.split("-")
      if (parts.size == 5) {
        val instantString = parts.slice(0 until 3).joinToString(SEPARATOR)
        val counter = parts[3].toLong(radix = 16)
        val node = parts[4]

        if (counter <= MAX_COUNTER && node.length <= MAX_NODE_LENGTH) {
          return Timestamp(
            instant = Instant.parse(instantString),
            counter = counter,
            node = node,
          )
        }
      }

      throw IllegalArgumentException("Couldn't parse invalid timestamp '$string'")
    }
  }
}
