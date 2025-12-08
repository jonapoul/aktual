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
