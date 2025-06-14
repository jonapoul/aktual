package actual.diagrams.tasks

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

internal fun Project.fileInReportDirectory(path: String): Provider<RegularFile> =
  project.layout.buildDirectory.file("reports/actual/$path")

internal fun Project.boolPropertyProvider(key: String): Provider<Boolean> =
  providers.gradleProperty(key).map { it.toBoolean() }
