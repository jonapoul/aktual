package aktual.core.logging

import okio.Path

fun interface LogStorage {
  fun directory(): Path
}
