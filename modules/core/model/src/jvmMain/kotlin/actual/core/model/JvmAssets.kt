package actual.core.model

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.io.InputStream

@Inject
@ContributesBinding(AppScope::class)
class JvmAssets : Assets {
  override fun getStream(name: String): InputStream = error("Not supported yet?")
}
