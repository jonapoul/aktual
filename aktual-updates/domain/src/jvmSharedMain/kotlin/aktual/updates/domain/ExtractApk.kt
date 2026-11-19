package aktual.updates.domain

import java.util.zip.ZipInputStream
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.source
import okio.use

internal actual fun extractApk(fileSystem: FileSystem, zip: Path, apk: Path) {
  ZipInputStream(fileSystem.source(zip).buffer().inputStream()).use { input ->
    while (true) {
      val entry = input.nextEntry ?: error("No APK found in $zip")
      if (!entry.isDirectory && entry.name.endsWith(".apk")) {
        fileSystem.sink(apk).buffer().use { sink -> sink.writeAll(input.source()) }
        return
      }
    }
  }
}
