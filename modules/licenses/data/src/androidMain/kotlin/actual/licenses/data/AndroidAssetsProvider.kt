package actual.licenses.data

import android.content.Context
import java.io.InputStream
import javax.inject.Inject

class AndroidAssetsProvider @Inject constructor(private val context: Context) : AssetsProvider {
  override fun licensesJsonStream(): InputStream = context.assets.open("open_source_licenses.json")
}
