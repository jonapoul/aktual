package aktual.gradle

import com.android.build.api.dsl.Packaging
import org.gradle.api.Project

internal fun Packaging.configurePackaging() {
  resources.excludes.add("META-INF/*")
}

// ":modules:path:to:module" -> "aktual.path.to.module", or ":app-android" -> "aktual.app.android"
internal fun Project.buildNamespace() = path
  .split(":", "-")
  .filter { it.isNotBlank() }
  .joinToString(".")
