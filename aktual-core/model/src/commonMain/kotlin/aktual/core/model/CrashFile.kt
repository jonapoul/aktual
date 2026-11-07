package aktual.core.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CrashFile(val name: String) : Comparable<CrashFile> {
  override fun compareTo(other: CrashFile) = name.compareTo(other.name)

  override fun toString(): String = name
}
