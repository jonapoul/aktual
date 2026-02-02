package aktual.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

const val EXPERIMENTAL_MATERIAL_3 = "androidx.compose.material3.ExperimentalMaterial3Api"

fun Project.optIn(klass: String) = optIn(listOf(klass))

fun Project.optIn(vararg classes: String) = optIn(classes.toList())

fun Project.optIn(classes: Collection<String>) {
  kotlin { compilerOptions.freeCompilerArgs.addAll(classes.map { "-opt-in=$it" }) }
}

fun Project.kspAllConfigs(dependency: Any) = dependencies {
  configurations
    .matching { c -> c.name.startsWith("ksp") && c.name != "ksp" && !c.name.contains("test", ignoreCase = true) }
    .configureEach { add(name, dependency) }
}
