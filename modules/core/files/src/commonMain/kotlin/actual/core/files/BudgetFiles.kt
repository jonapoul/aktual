package actual.core.files

import actual.budget.model.BudgetId
import okio.FileSystem
import okio.Path

interface BudgetFiles {
  val fileSystem: FileSystem
  fun directory(id: BudgetId, mkdirs: Boolean = false): Path
  fun tmp(mkdirs: Boolean = false): Path
}

fun BudgetFiles.database(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("db.sqlite")

fun BudgetFiles.metadata(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("metadata.json")

fun BudgetFiles.encryptedZip(id: BudgetId, mkdirs: Boolean = false): Path = tmp(mkdirs).resolve("$id-encrypted.zip")

fun BudgetFiles.decryptedZip(id: BudgetId, mkdirs: Boolean = false): Path = tmp(mkdirs).resolve("$id-decrypted.zip")
