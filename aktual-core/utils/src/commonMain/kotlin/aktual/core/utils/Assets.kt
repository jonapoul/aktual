package aktual.core.utils

import java.io.InputStream

fun interface Assets {
  fun getStream(name: String): InputStream
}
