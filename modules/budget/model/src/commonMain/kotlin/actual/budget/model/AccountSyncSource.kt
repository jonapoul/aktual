package actual.budget.model

sealed class AccountSyncSource(open val value: String) {
  data object SimpleFin : AccountSyncSource(value = "simpleFin")
  data object GoCardless : AccountSyncSource(value = "goCardless")
  data class Other(override val value: String) : AccountSyncSource(value)

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): AccountSyncSource = when (string) {
      SimpleFin.value -> SimpleFin
      GoCardless.value -> GoCardless
      else -> Other(string)
    }
  }
}
