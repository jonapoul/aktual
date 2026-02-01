package aktual.gradle

import dev.detekt.gradle.Detekt
import org.gradle.api.Project
import org.gradle.api.tasks.TaskCollection
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

val Project.detektTasks: TaskCollection<Detekt>
  get() = tasks
    .withType<Detekt>()
    .matching { task -> !task.name.contains("release", ignoreCase = true) }

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
