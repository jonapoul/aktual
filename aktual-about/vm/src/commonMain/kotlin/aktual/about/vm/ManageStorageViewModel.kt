package aktual.about.vm

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.readMetadata
import aktual.core.di.BudgetGraphHolder
import aktual.core.model.AppDirectory
import aktual.core.model.Bytes
import aktual.core.model.Percent
import aktual.core.model.bytes
import aktual.core.theme.CustomThemeCache
import aktual.prefs.AppPreferences
import aktual.prefs.delete
import alakazam.kotlin.CoroutineContexts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import java.io.File
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.logcat
import okio.Path

@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ManageStorageViewModel(
  private val files: BudgetFiles,
  private val appDirectory: AppDirectory,
  private val contexts: CoroutineContexts,
  private val customThemeCache: CustomThemeCache,
  private val appPreferences: AppPreferences,
  private val budgetGraphHolder: BudgetGraphHolder,
) : ViewModel() {

  private val mutableState = MutableStateFlow<ManageStorageState>(ManageStorageState.Loading)
  val state: StateFlow<ManageStorageState> = mutableState.asStateFlow()

  init {
    loadStorageInfo()
  }

  fun reload() {
    loadStorageInfo()
  }

  fun showDialog(dialog: StorageDialog) {
    mutableState.update { current ->
      if (current is ManageStorageState.Loaded) current.copy(dialog = dialog) else current
    }
  }

  fun dismissDialog() {
    mutableState.update { current ->
      if (current is ManageStorageState.Loaded) {
        current.copy(dialog = StorageDialog.None)
      } else {
        current
      }
    }
  }

  fun clearAllFiles() {
    viewModelScope.launch {
      dismissDialog()
      budgetGraphHolder.clear()
      withContext(contexts.io) {
        val root = appDirectory.get()
        with(files.fileSystem) { list(root).forEach { deleteRecursively(it) } }
      }
      logcat.d { "Cleared all files" }
      loadStorageInfo()
    }
  }

  fun clearBudget(id: BudgetId) {
    viewModelScope.launch {
      dismissDialog()
      if (budgetGraphHolder.value?.budgetId == id) {
        budgetGraphHolder.clear()
      }
      withContext(contexts.io) {
        val budgetDir = files.directory(id)
        if (files.fileSystem.exists(budgetDir)) {
          files.fileSystem.deleteRecursively(budgetDir)
        }
      }
      logcat.d { "Cleared budget $id" }
      loadStorageInfo()
    }
  }

  fun clearCache() {
    viewModelScope.launch {
      dismissDialog()
      withContext(contexts.io) {
        val tmp = files.tmp()
        if (files.fileSystem.exists(tmp)) {
          files.fileSystem.deleteRecursively(tmp)
          files.fileSystem.createDirectories(tmp)
        }
      }
      customThemeCache.clear()
      logcat.d { "Cleared cache" }
      loadStorageInfo()
    }
  }

  fun clearPreferences() {
    viewModelScope.launch {
      dismissDialog()
      appPreferences.token.delete()
      appPreferences.serverUrl.delete()
      logcat.d { "Cleared preferences" }
      loadStorageInfo()
    }
  }

  private fun loadStorageInfo() {
    viewModelScope.launch {
      val loaded = withContext(contexts.io) { computeStorageInfo() }
      mutableState.update { current ->
        val dialog = (current as? ManageStorageState.Loaded)?.dialog ?: StorageDialog.None
        loaded.copy(dialog = dialog)
      }
    }
  }

  private fun computeStorageInfo(): ManageStorageState.Loaded {
    val root = appDirectory.get()
    val budgetsDir = files.directoryPath
    val themesDir = root / "themes"
    val tmpDir = files.tmp()

    val budgetItems =
      buildList {
          if (files.fileSystem.exists(budgetsDir)) {
            for (entry in files.fileSystem.list(budgetsDir)) {
              val metadata = files.fileSystem.metadataOrNull(entry)
              if (entry == tmpDir || metadata?.isDirectory != true) continue

              val budgetId = BudgetId(entry.name)
              val name =
                try {
                  files.readMetadata(budgetId)[DbMetadata.BudgetName] ?: entry.name
                } catch (_: Exception) {
                  entry.name
                }
              val size = directorySize(entry)
              add(BudgetStorageItem(id = budgetId, name = name, size = size))
            }
          }
        }
        .sortedByDescending { it.size }
        .toImmutableList()

    val budgetsTotal = budgetItems.sumOf { it.size.numBytes }.bytes
    val cacheSize = (directorySize(tmpDir).numBytes + directorySize(themesDir).numBytes).bytes
    val totalSize = directorySize(root)
    val otherSize =
      (totalSize.numBytes - budgetsTotal.numBytes - cacheSize.numBytes).coerceAtLeast(0L).bytes
    val deviceTotalStorage = File(root.toString()).totalSpace.bytes

    return ManageStorageState.Loaded(
      totalSize = totalSize,
      percentTotalStorage =
        Percent(numerator = totalSize.numBytes, denominator = deviceTotalStorage.numBytes),
      budgets = budgetItems,
      cacheSize = cacheSize,
      otherSize = otherSize,
    )
  }

  private fun directorySize(path: Path): Bytes {
    if (!files.fileSystem.exists(path)) return Bytes.Zero
    return files.fileSystem
      .listRecursively(path)
      .sumOf { files.fileSystem.metadataOrNull(it)?.size ?: 0L }
      .bytes
  }
}
