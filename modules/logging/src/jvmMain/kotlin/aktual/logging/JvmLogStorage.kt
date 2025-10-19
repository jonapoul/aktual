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
package aktual.logging

import okio.Path
import okio.Path.Companion.toPath

class JvmLogStorage(private val overrideUserPath: Path? = null) : LogStorage {
  override fun directory(): Path {
    if (overrideUserPath != null) {
      return overrideUserPath
    }

    val os = System.getProperty("os.name").lowercase()

    val path = when {
      os.contains("windows") -> System.getenv("APPDATA")
      else -> System.getProperty("user.home")
    }

    if (path == null) {
      error("Not sure where to store logs! os='$os'")
    }

    return path
      .toPath()
      .resolve(".aktual")
      .resolve("logs")
  }
}
