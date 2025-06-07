package actual.core.model

import android.content.Context
import java.io.InputStream
import javax.inject.Inject

class AndroidAssets @Inject constructor(private val context: Context) : Assets {
  override fun getStream(name: String): InputStream = context.assets.open(name)
}
