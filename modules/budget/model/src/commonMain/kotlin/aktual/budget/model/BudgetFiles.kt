/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import kotlinx.serialization.json.Json
import okio.FileNotFoundException
import okio.FileSystem
import okio.Path
import okio.buffer

interface BudgetFiles {
  val fileSystem: FileSystem
  val directoryPath: Path

  fun directory(id: BudgetId, mkdirs: Boolean = false): Path = directoryPath
    .resolve(id.value)
    .also { if (mkdirs) fileSystem.createDirectories(it) }

  fun tmp(mkdirs: Boolean = false): Path = directoryPath
    .resolve("tmp")
    .also { if (mkdirs) fileSystem.createDirectories(it) }
}

fun BudgetFiles.database(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("db.sqlite")

fun BudgetFiles.metadata(id: BudgetId, mkdirs: Boolean = false): Path = directory(id, mkdirs).resolve("metadata.json")

fun BudgetFiles.encryptedZip(id: BudgetId, mkdirs: Boolean = false): Path = tmp(mkdirs).resolve("$id-encrypted.zip")

fun BudgetFiles.decryptedZip(id: BudgetId, mkdirs: Boolean = false): Path = tmp(mkdirs).resolve("$id-decrypted.zip")

fun BudgetFiles.writeMetadata(metadata: DbMetadata) {
  val path = metadata(metadata.cloudFileId, mkdirs = true)
  val json = Json.encodeToString(DbMetadata.serializer(), metadata)
  fileSystem.sink(path).buffer().use { sink -> sink.writeUtf8(json) }
}

fun BudgetFiles.readMetadata(id: BudgetId): DbMetadata {
  val path = metadata(id, mkdirs = false)
  if (!fileSystem.exists(path)) throw FileNotFoundException("$path doesn't exist")
  val json = fileSystem.source(path).buffer().use { source -> source.readUtf8() }
  return Json.decodeFromString(DbMetadata.serializer(), json)
}
