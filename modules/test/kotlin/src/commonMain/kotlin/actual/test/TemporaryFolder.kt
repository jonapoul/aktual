package actual.test

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
  val fileSystem: FileSystem = FileSystem.SYSTEM,
) : TestInterceptor, CoroutineTestInterceptor {
  lateinit var root: Path
    private set

  override suspend fun intercept(testFunction: CoroutineTestFunction) = runTest { testFunction() }

  override fun intercept(testFunction: TestFunction) = runTest { testFunction() }

  private inline fun runTest(testFunction: () -> Unit) = try {
    root = createTempDirectory().toOkioPath()
    testFunction()
  } finally {
    fileSystem.deleteRecursively(root)
  }

  fun newFolder(name: String): Path = resolve(name)
    .also { fileSystem.createDirectory(it) }

  fun resolve(path: String): Path = root.resolve(path)
  fun list(path: Path = root): List<Path> = fileSystem.list(path)
  fun sink(path: Path): Sink = fileSystem.sink(path)
  fun source(path: Path): Source = fileSystem.source(path)
}
