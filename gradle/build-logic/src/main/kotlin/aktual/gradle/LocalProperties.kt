package aktual.gradle

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import java.util.Properties

fun Project.localProperties(
  filename: String = DEFAULT_FILENAME,
): Provider<Map<String, String>> = providers.of(LocalPropertiesValueSource::class.java) {
  parameters {
    propertiesFile.set(rootProject.layout.projectDirectory.file(filename))
  }
}

fun Provider<Map<String, String>>.getOptional(key: String): String? =
  map { it[key] }.orNull?.takeIf { it.isNotEmpty() }

private abstract class LocalPropertiesValueSource :
  ValueSource<Map<String, String>, LocalPropertiesValueSource.Parameters> {
  interface Parameters : ValueSourceParameters {
    @get:[InputFile PathSensitive(NONE)] val propertiesFile: RegularFileProperty
  }

  override fun obtain(): Map<String, String> {
    val file = parameters.propertiesFile.asFile.get()
    if (!file.exists()) return emptyMap()
    return Properties()
      .apply { file.reader().use(::load) }
      .run { stringPropertyNames().associateWith(::getProperty) }
  }
}

private const val DEFAULT_FILENAME = "local.properties"
