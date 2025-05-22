package actual.budget.model

import alakazam.kotlin.serialization.SimpleSerializer
import dev.drewhamilton.poko.Poko
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Corresponds to packages/crdt/src/crdt/timestamp.ts#Timestamp
 */
@Serializable(Timestamp.Serializer::class)
@Poko class Timestamp(
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
