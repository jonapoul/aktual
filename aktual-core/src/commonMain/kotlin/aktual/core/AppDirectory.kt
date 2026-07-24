package aktual.core

import okio.Path

fun interface AppDirectory {
  fun get(): Path
}
