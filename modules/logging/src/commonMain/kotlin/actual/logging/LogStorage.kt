package actual.logging

import okio.Path

interface LogStorage {
  fun directory(): Path
}
