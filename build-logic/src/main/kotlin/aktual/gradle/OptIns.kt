package aktual.gradle

import org.gradle.api.Project

const val EXPERIMENTAL_MATERIAL_3 = "androidx.compose.material3.ExperimentalMaterial3Api"

fun Project.optIn(klass: String) = optIn(listOf(klass))

fun Project.optIn(vararg classes: String) = optIn(classes.toList())

fun Project.optIn(classes: Collection<String>) {
  kotlin {
    compilerOptions {
      freeCompilerArgs.addAll(
        classes.map { "-opt-in=$it" },
      )
    }
  }
}
