package aktual.budget.model

@JvmInline
value class ScheduleJsonPathIndex(private val value: String) : Comparable<ScheduleJsonPathIndex> {
  val index: Int
    get() = value.removePrefix(prefix = "$[").removeSuffix(suffix = "]").toInt()

  constructor(int: Int) : this("$[$int]")

  override fun toString() = value

  override fun compareTo(other: ScheduleJsonPathIndex) = value.compareTo(other.value)
}
