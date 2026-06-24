package aktual.budget.list.vm

import aktual.api.model.sync.UserFile
import aktual.api.model.sync.UserWithAccess
import aktual.budget.model.Budget
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetUserWithAccess
import aktual.budget.model.DbMetadata
import aktual.budget.model.LocalBudget
import aktual.core.model.KeyId
import aktual.prefs.KeyPreferences
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext

@Inject
class BudgetReconciler
internal constructor(
  private val files: BudgetFiles,
  private val keyPreferences: KeyPreferences,
  private val contexts: CoroutineContexts,
) {
  suspend fun reconcile(remote: List<UserFile>?): List<Budget> {
    val locals = withContext(contexts.io) { files.listLocal() }
    val localsByCloudId =
      locals.mapNotNull { local -> local.metadata?.cloudFileIdOrNull?.let { it to local } }.toMap()
    // A downloaded budget's directory is named after its cloud file id, so also match locals by
    // directory id. This covers the window during/just after a download where metadata.json hasn't
    // been written yet, which would otherwise emit both a Remote and a Local entry sharing the same
    // directoryId and crash the list with a duplicate Compose key
    val localsById = locals.associateBy { it.id }

    if (remote == null) {
      return locals.map { it.toOfflineBudget() }.sortedBy { it.name }
    }

    val matchedLocalIds = mutableSetOf<BudgetId>()
    val out = mutableListOf<Budget>()

    for (file in remote.filter { it.deleted == 0 }) {
      val local = localsByCloudId[file.fileId] ?: localsById[file.fileId]
      val encryptKeyId = file.encryptKeyId?.value
      val hasKey = file.encryptKeyId in keyPreferences
      val users = file.usersWithAccess.toModelList()

      out +=
        if (local != null) {
          matchedLocalIds += local.id
          val localGroupId = local.metadata?.get(DbMetadata.GroupId)
          if (localGroupId == file.groupId) {
            Budget.Synced(
              name = file.name,
              hasKey = hasKey,
              encryptKeyId = encryptKeyId,
              cloudFileId = file.fileId,
              groupId = file.groupId,
              owner = file.owner,
              usersWithAccess = users,
            )
          } else {
            Budget.Detached(
              name = file.name,
              hasKey = hasKey,
              encryptKeyId = encryptKeyId,
              cloudFileId = file.fileId,
              groupId = file.groupId,
              owner = file.owner,
              usersWithAccess = users,
            )
          }
        } else {
          Budget.Remote(
            name = file.name,
            hasKey = hasKey,
            encryptKeyId = encryptKeyId,
            cloudFileId = file.fileId,
            groupId = file.groupId,
            owner = file.owner,
            usersWithAccess = users,
          )
        }
    }

    for (local in locals) {
      if (local.id in matchedLocalIds) continue
      val cloudId = local.metadata?.cloudFileIdOrNull
      out += if (cloudId != null) local.toBroken(cloudId) else local.toLocal()
    }

    return out.sortedBy { it.name }
  }

  private suspend fun LocalBudget.toOfflineBudget(): Budget {
    val cloudId = metadata?.cloudFileIdOrNull
    return if (cloudId != null) toUnknown(cloudId) else toLocal()
  }

  private suspend fun LocalBudget.toUnknown(cloudId: BudgetId): Budget.Unknown {
    val md = metadata
    return Budget.Unknown(
      name = md?.get(DbMetadata.BudgetName) ?: id.value,
      hasKey = md?.encryptKeyIdOrNull?.let { KeyId(it) } in keyPreferences,
      encryptKeyId = md?.encryptKeyIdOrNull,
      cloudFileId = cloudId,
      groupId = md?.get(DbMetadata.GroupId).orEmpty(),
    )
  }

  private suspend fun LocalBudget.toBroken(cloudId: BudgetId): Budget.Broken {
    val md = metadata
    return Budget.Broken(
      name = md?.get(DbMetadata.BudgetName) ?: id.value,
      hasKey = md?.encryptKeyIdOrNull?.let { KeyId(it) } in keyPreferences,
      encryptKeyId = md?.encryptKeyIdOrNull,
      cloudFileId = cloudId,
      groupId = md?.get(DbMetadata.GroupId).orEmpty(),
    )
  }

  private suspend fun LocalBudget.toLocal(): Budget.Local =
    Budget.Local(
      id = id,
      name = metadata?.get(DbMetadata.BudgetName) ?: id.value,
      hasKey = metadata?.encryptKeyIdOrNull?.let { KeyId(it) } in keyPreferences,
      encryptKeyId = metadata?.encryptKeyIdOrNull,
    )
}

private fun List<UserWithAccess>.toModelList(): ImmutableList<BudgetUserWithAccess> = map { user ->
  BudgetUserWithAccess(
    userId = user.userId,
    userName = user.userName,
    displayName = user.displayName,
    isOwner = user.isOwner,
  )
}
  .toImmutableList()

private val DbMetadata.cloudFileIdOrNull: BudgetId?
  get() = get(DbMetadata.CloudFileId)

private val DbMetadata.encryptKeyIdOrNull: String?
  get() = get(DbMetadata.EncryptKeyId)
