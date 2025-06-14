package actual.test

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.buffer
import okio.sink
import java.io.File

fun resource(path: String) = FileSystem.RESOURCES.source(path.toPath())

fun Source.readBytes() = buffer().use { it.readByteArray() }

fun Source.copyTo(file: File) = copyTo(file.sink())

fun Source.copyTo(sink: Sink): Unit = buffer().use { source ->
  sink.buffer().use { sink ->
    sink.writeAll(source)
  }
}

fun Source.copyTo(path: Path, fileSystem: FileSystem): Unit = copyTo(fileSystem.sink(path))
