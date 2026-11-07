package aktual.core.model

import androidx.compose.runtime.Immutable

@JvmInline
@Immutable
value class Distance(val meters: Double) : Comparable<Distance> {
  override fun compareTo(other: Distance): Int = meters.compareTo(other.meters)
}

val Number.meters: Distance
  get() = Distance(toDouble())
