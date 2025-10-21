/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.test

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
  sink.buffer().use { buffer ->
    buffer.writeAll(source)
  }
}

fun Source.copyTo(path: Path, fileSystem: FileSystem): Unit = copyTo(fileSystem.sink(path))
