package aktual.core.model

import okio.Path

fun interface AppDirectory {
  fun get(): Path
}
