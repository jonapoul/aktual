package aktual.budget.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
sealed interface Budget {
  val name: String
  val hasKey: Boolean
  val encryptKeyId: String?
  val state: BudgetState

  sealed interface Cloud : Budget {
    val cloudFileId: BudgetId
    val groupId: String
  }

  data class Local(
    val id: BudgetId,
    override val name: String,
    override val hasKey: Boolean,
    override val encryptKeyId: String?,
  ) : Budget {
    override val state
      get() = BudgetState.Local
  }

  data class Remote(
    override val name: String,
    override val hasKey: Boolean,
    override val encryptKeyId: String?,
    override val cloudFileId: BudgetId,
    override val groupId: String,
    val owner: String?,
    val usersWithAccess: ImmutableList<BudgetUserWithAccess> = persistentListOf(),
  ) : Cloud {
    override val state
      get() = BudgetState.Remote
  }

  data class Synced(
    override val name: String,
    override val hasKey: Boolean,
    override val encryptKeyId: String?,
    override val cloudFileId: BudgetId,
    override val groupId: String,
    val owner: String?,
    val usersWithAccess: ImmutableList<BudgetUserWithAccess> = persistentListOf(),
  ) : Cloud {
    override val state
      get() = BudgetState.Synced
  }

  data class Detached(
    override val name: String,
    override val hasKey: Boolean,
    override val encryptKeyId: String?,
    override val cloudFileId: BudgetId,
    override val groupId: String,
    val owner: String?,
    val usersWithAccess: ImmutableList<BudgetUserWithAccess> = persistentListOf(),
  ) : Cloud {
    override val state
      get() = BudgetState.Detached
  }

  data class Broken(
    override val name: String,
    override val hasKey: Boolean,
    override val encryptKeyId: String?,
    override val cloudFileId: BudgetId,
    override val groupId: String,
  ) : Cloud {
    override val state
      get() = BudgetState.Broken
  }

  data class Unknown(
    override val name: String,
    override val hasKey: Boolean,
    override val encryptKeyId: String?,
    override val cloudFileId: BudgetId,
    override val groupId: String,
  ) : Cloud {
    override val state
      get() = BudgetState.Unknown
  }
}

val Budget.directoryId: BudgetId
  get() =
    when (this) {
      is Budget.Cloud -> cloudFileId
      is Budget.Local -> id
    }
