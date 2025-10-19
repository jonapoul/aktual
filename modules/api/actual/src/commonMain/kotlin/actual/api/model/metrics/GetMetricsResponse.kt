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
package aktual.api.model.metrics

import aktual.core.model.Bytes
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
data class GetMetricsResponse(
  @SerialName("mem") val memory: Memory,
  @SerialName("uptime") @Serializable(DurationAsDoubleSerializer::class) val uptime: Duration,
) {
  // See https://www.geeksforgeeks.org/node-js/node-js-process-memoryusage-method/
  @Serializable
  data class Memory(
    @SerialName("rss") val rss: Bytes,
    @SerialName("heapTotal") val heapTotal: Bytes,
    @SerialName("heapUsed") val heapUsed: Bytes,
    @SerialName("external") val external: Bytes,
    @SerialName("arrayBuffers") val arrayBuffers: Bytes,
  )
}

// Takes output from https://www.geeksforgeeks.org/node-js/node-js-process-uptime-method/
private object DurationAsDoubleSerializer : KSerializer<Duration> {
  private const val NANOSECS_PER_SEC = 1e9
  override val descriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.DOUBLE)
  override fun deserialize(decoder: Decoder) = decoder.decodeDouble().seconds
  override fun serialize(encoder: Encoder, value: Duration) =
    encoder.encodeDouble(value.inWholeNanoseconds / NANOSECS_PER_SEC)
}
