package aktual.core.model

import java.io.InputStream

fun interface Assets {
  fun getStream(name: String): InputStream
}
