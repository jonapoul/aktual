package actual.api.download

@JvmInline
value class Bytes(val numBytes: Long) : Comparable<Bytes> {
  override fun toString(): String = when {
    numBytes < KB -> "%d B".format(numBytes)
    numBytes < MB -> "%.1f kB".format(numBytes / KB)
    numBytes < GB -> "%.1f MB".format(numBytes / MB)
    numBytes < TB -> "%.1f GB".format(numBytes / GB)
    else -> "%.1f TB".format(numBytes / TB)
  }

  override fun compareTo(other: Bytes): Int = numBytes.compareTo(other.numBytes)

  companion object {
    val Zero = 0.bytes
    const val KB = 1000f
    const val MB = KB * 1000f
    const val GB = MB * 1000f
    const val TB = GB * 1000f
  }
}

val Number.bytes: Bytes get() = Bytes(toLong())
