package actual.licenses.data

import java.io.InputStream

fun interface AssetsProvider {
  fun licensesJsonStream(): InputStream
}
