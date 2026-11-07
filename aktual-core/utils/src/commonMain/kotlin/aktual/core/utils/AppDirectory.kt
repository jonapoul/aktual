package aktual.core.utils

import okio.Path

fun interface AppDirectory {
  fun get(): Path
}
