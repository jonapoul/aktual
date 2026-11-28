package aktual.gradle

import blueprint.core.intProperty
import com.android.build.api.dsl.CompileSdkSpec
import org.gradle.api.provider.ProviderFactory

fun CompileSdkSpec.fromProperties(providers: ProviderFactory) {
  version =
    release(providers.intProperty("aktual.android.compileSdk").get()) {
      minorApiLevel = providers.intProperty("aktual.android.compileSdkMinor").get()
    }
}
