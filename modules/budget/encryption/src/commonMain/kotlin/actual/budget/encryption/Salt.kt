package actual.budget.encryption

@JvmInline
value class Salt(val value: String) : Comparable<Salt> {
  override fun toString(): String = value
  override fun compareTo(other: Salt) = value.compareTo(other.value)
}
