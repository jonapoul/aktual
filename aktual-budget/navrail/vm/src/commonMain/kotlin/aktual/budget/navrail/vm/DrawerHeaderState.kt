package aktual.budget.navrail.vm

import androidx.compose.runtime.Immutable

@Immutable
data class DrawerHeaderState(
  val budgetName: String?,
  val serverHost: String?,
  val canManageApiKeys: Boolean = false,
  val syncStatus: SidebarSyncStatus = Synced,
) {
  val isDemo: Boolean
    get() = serverHost == null
}

// packages/desktop-client/src/components/sidebar/redesign/SyncStatusLine.tsx
enum class SidebarSyncStatus {
  LocalOnly,
  Syncing,
  SyncError,
  Synced,
}
