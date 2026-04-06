package aktual.budget.model

import alakazam.kotlin.SimpleSerializer
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/** Corresponds to packages/crdt/src/crdt/timestamp.ts#Timestamp */
@Serializable(Timestamp.Serializer::class)
data class Timestamp(val instant: Instant, val counter: Long, val node: String) {
  val milliseconds: Long
    get() = instant.toEpochMilliseconds()

  override fun toString(): String {
    val counter = counter.toString(radix = 16).uppercase()
    return listOf(
        instant.toString(),
        (BASE_COUNTER + counter).removeRange(startIndex = 0, endIndex = counter.length),
        "$BASE_NODE$node".removeRange(startIndex = 0, endIndex = 16),
      )
      .joinToString(SEPARATOR)
  }

  internal object Serializer :
    SimpleSerializer<Timestamp>(serialName = "Timestamp", Timestamp::parse)

  companion object {
    private const val BASE_COUNTER = "0000"
    const val MAX_COUNTER = 0xFFFF
    private const val MAX_NODE_LENGTH = 16
    private const val SEPARATOR = "-"

    const val BASE_NODE = "0000000000000000"

    val MAXIMUM = parse("9999-12-31T23:59:59.999Z-FFFF-FFFFFFFFFFFFFFFF")

    /**
     * Generate a unique, monotonic timestamp for a local change. Mirrors
     * packages/crdt/src/crdt/timestamp.ts#send. Advances [current] so that the logical time never
     * goes backward and the counter increments when physical time doesn't advance.
     */
    fun send(current: Timestamp, clock: Clock): Timestamp {
      val phys = clock.now().toEpochMilliseconds()
      val lOld = current.milliseconds
      val lNew = maxOf(lOld, phys)
      val cNew = if (lOld == lNew) current.counter + 1 else 0L
      check(cNew <= MAX_COUNTER) { "Timestamp counter overflow" }
      return fromMilliseconds(lNew, cNew, current.node)
    }

    fun fromMilliseconds(millis: Long, counter: Long = 0, node: String = BASE_NODE): Timestamp =
      Timestamp(instant = Instant.fromEpochMilliseconds(millis), counter = counter, node = node)

    @Suppress("MagicNumber")
    fun parse(string: String): Timestamp {
      val parts = string.split("-")
      if (parts.size == 5) {
        val instantString = parts.slice(0 until 3).joinToString(SEPARATOR)
        val counter = parts[3].toLong(radix = 16)
        val node = parts[4]

        if (counter <= MAX_COUNTER && node.length <= MAX_NODE_LENGTH) {
          return Timestamp(instant = Instant.parse(instantString), counter = counter, node = node)
        }
      }

      throw IllegalArgumentException("Couldn't parse invalid timestamp '$string'")
    }
  }
}
