package aktual.budget.model

import aktual.di.Closeable
import kotlinx.serialization.json.Json
import okio.FileNotFoundException
import okio.FileSystem
import okio.Path
import okio.buffer

class BudgetFiles(val fileSystem: FileSystem, val directoryPath: Path) : Closeable {
  override fun close() {
    val tmpDir = tmp(mkdirs = false)
    fileSystem.deleteRecursively(tmpDir)
  }

  fun directory(id: BudgetId, mkdirs: Boolean = false): Path =
    (directoryPath / id.value).also { if (mkdirs) fileSystem.createDirectories(it) }

  fun tmp(mkdirs: Boolean = false): Path =
    (directoryPath / "tmp").also { if (mkdirs) fileSystem.createDirectories(it) }

  fun database(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs) / "db.sqlite"

  fun metadata(id: BudgetId, mkdirs: Boolean = false): Path =
    directory(id, mkdirs) / "metadata.json"

  fun encryptedZip(id: BudgetId, mkdirs: Boolean = false): Path = tmp(mkdirs) / "$id-encrypted.zip"

  fun decryptedZip(id: BudgetId, mkdirs: Boolean = false): Path = tmp(mkdirs) / "$id-decrypted.zip"

  fun writeMetadata(metadata: DbMetadata) {
    val path = metadata(metadata.cloudFileId, mkdirs = true)
    val json = Json.encodeToString(DbMetadata.serializer(), metadata)
    fileSystem.sink(path).buffer().use { sink -> sink.writeUtf8(json) }
  }

  fun readMetadata(id: BudgetId): DbMetadata {
    val path = metadata(id, mkdirs = false)
    if (!fileSystem.exists(path)) throw FileNotFoundException("$path doesn't exist")
    val json = fileSystem.source(path).buffer().use { source -> source.readUtf8() }
    return Json.decodeFromString(DbMetadata.serializer(), json)
  }

  fun listLocal(): List<LocalBudget> {
    if (!fileSystem.exists(directoryPath)) return emptyList()
    return fileSystem
      .list(directoryPath)
      .filter { it.name != "tmp" && fileSystem.metadataOrNull(it)?.isDirectory == true }
      .map { dir ->
        val id = BudgetId(dir.name)
        val metadata = runCatching { readMetadata(id) }.getOrNull()
        LocalBudget(id = id, metadata = metadata)
      }
  }
}

data class LocalBudget(val id: BudgetId, val metadata: DbMetadata?)
