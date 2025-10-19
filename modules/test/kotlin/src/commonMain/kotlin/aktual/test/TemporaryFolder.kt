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

import app.cash.burst.TestFunction
import app.cash.burst.TestInterceptor
import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Sink
import okio.Source
import kotlin.io.path.createTempDirectory

class TemporaryFolder(
  override val fileSystem: FileSystem = FileSystem.SYSTEM,
) : ITemporaryFolder, TestInterceptor {
  private lateinit var mutableRoot: Path
  override val root: Path get() = mutableRoot

  override fun intercept(testFunction: TestFunction) = try {
    mutableRoot = createTempDirectory().toOkioPath()
    testFunction()
  } finally {
    fileSystem.deleteRecursively(root)
  }
}

class CoTemporaryFolder(
  override val fileSystem: FileSystem = FileSystem.SYSTEM,
) : ITemporaryFolder, CoroutineTestInterceptor {
  private lateinit var mutableRoot: Path
  override val root: Path get() = mutableRoot

  override suspend fun intercept(testFunction: CoroutineTestFunction) = try {
    mutableRoot = createTempDirectory().toOkioPath()
    testFunction()
  } finally {
    fileSystem.deleteRecursively(root)
  }
}

interface ITemporaryFolder {
  val fileSystem: FileSystem
  val root: Path

  fun newFolder(name: String): Path = resolve(name)
    .also { fileSystem.createDirectory(it) }

  fun resolve(path: String): Path = root.resolve(path)
  fun list(path: Path = root): List<Path> = fileSystem.list(path)
  fun sink(path: Path): Sink = fileSystem.sink(path)
  fun source(path: Path): Source = fileSystem.source(path)
}
