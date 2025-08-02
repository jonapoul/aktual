package actual.core.model

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.io.InputStream

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<Assets>())
class AndroidAssets(private val context: Context) : Assets {
  override fun getStream(name: String): InputStream = context.assets.open(name)
}
