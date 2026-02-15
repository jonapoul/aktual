package aktual.test

import java.io.File
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.buffer
import okio.sink

fun Source.readBytes() = buffer().use { it.readByteArray() }

fun Source.copyTo(file: File) = copyTo(file.sink())

fun Source.copyTo(sink: Sink) =
  buffer().use { source -> sink.buffer().use { buffer -> buffer.writeAll(source) } }

fun Source.copyTo(path: Path, fileSystem: FileSystem) = copyTo(fileSystem.sink(path))
