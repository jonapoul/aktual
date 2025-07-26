package actual.core.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Bytes(val numBytes: Long) : Comparable<Bytes> {
  override fun toString(): String = toString(precision = 1)

  fun toString(precision: Int): String = when {
    this < kB -> "%d B".format(numBytes)
    this < MB -> "%.${precision}f kB".format(this / kB)
    this < GB -> "%.${precision}f MB".format(this / MB)
    this < TB -> "%.${precision}f GB".format(this / GB)
    else -> "%.${precision}f TB".format(this / TB)
  }

  override fun compareTo(other: Bytes): Int = numBytes.compareTo(other.numBytes)

  operator fun times(other: Bytes): Bytes = (numBytes * other.numBytes.toDouble()).bytes
  operator fun times(other: Number): Bytes = (numBytes * other.toDouble()).bytes

  operator fun div(other: Bytes): Double = numBytes / other.numBytes.toDouble()
  operator fun div(other: Number): Double = numBytes / other.toDouble()

  companion object {
    val Zero = 0L.bytes
    val kB = 1000L.bytes
    val MB = kB * 1000L
    val GB = MB * 1000L
    val TB = GB * 1000L
  }
}

operator fun Number.times(other: Bytes): Bytes = other * this

val Number.bytes: Bytes get() = Bytes(toLong())

val Number.kB: Bytes get() = this * Bytes.kB
val Number.MB: Bytes get() = this * Bytes.MB
val Number.GB: Bytes get() = this * Bytes.GB
val Number.TB: Bytes get() = this * Bytes.TB
