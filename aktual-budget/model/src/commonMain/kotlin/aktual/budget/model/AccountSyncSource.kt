@file:Suppress("SpellCheckingInspection", "FunctionName")

package aktual.budget.model

// packages/loot-core/src/types/models/bank-sync.ts SYNC_PROVIDERS
@JvmInline
value class AccountSyncSource private constructor(val value: String) {
  override fun toString(): String = value

  companion object {
    val SimpleFin = AccountSyncSource(value = "simpleFin")
    val GoCardless = AccountSyncSource(value = "goCardless")
    val PluggyAi = AccountSyncSource(value = "pluggyai")
    val Akahu = AccountSyncSource(value = "akahu")
    val EnableBanking = AccountSyncSource(value = "enableBanking")

    fun Other(value: String) = AccountSyncSource(value)

    fun fromString(string: String): AccountSyncSource =
      when (string) {
        SimpleFin.value -> SimpleFin
        GoCardless.value -> GoCardless
        PluggyAi.value -> PluggyAi
        Akahu.value -> Akahu
        EnableBanking.value -> EnableBanking
        else -> Other(string)
      }
  }
}
