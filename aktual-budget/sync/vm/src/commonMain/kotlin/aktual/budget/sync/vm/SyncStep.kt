package aktual.budget.sync.vm

import androidx.compose.runtime.Immutable

@Immutable
enum class SyncStep {
  FetchingFileInfo,
  DownloadingDatabase,
  ValidatingDatabase,
}
