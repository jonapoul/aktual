package aktual.budget.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
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
    // The directory of the local copy, named after the cloud file ID unless it was created here
    val localId: BudgetId,
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
    // The directory of the local copy, named after the cloud file ID unless it was created here
    val localId: BudgetId,
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
    // The directory of the local copy, named after the cloud file ID unless it was created here
    val localId: BudgetId,
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
    // The directory of the local copy, named after the cloud file ID unless it was created here
    val localId: BudgetId,
  ) : Cloud {
    override val state
      get() = BudgetState.Unknown
  }
}

@get:Stable
val Budget.directoryId: BudgetId
  get() =
    when (this) {
      is Local -> id
      is Remote -> cloudFileId
      is Synced -> localId
      is Detached -> localId
      is Broken -> localId
      is Unknown -> localId
    }
