package aktual.core.model

import aktual.di.AppScope
import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import java.io.InputStream

@ContributesBinding(AppScope::class)
class AndroidAssets(private val context: Context) : Assets {
  override fun getStream(name: String): InputStream = context.assets.open(name)
}
