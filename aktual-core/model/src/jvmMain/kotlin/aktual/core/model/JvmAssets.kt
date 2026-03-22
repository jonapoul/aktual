package aktual.core.model

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.io.InputStream

@ContributesBinding(AppScope::class)
class JvmAssets : Assets {
  // Without the /, it looks relative to the class's package. With /, it looks at the classpath root
  override fun getStream(name: String): InputStream =
    javaClass.getResourceAsStream("/$name") ?: error("Failed to load '$name' from resources")
}
