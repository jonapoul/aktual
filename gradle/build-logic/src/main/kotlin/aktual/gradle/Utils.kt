package aktual.gradle

import dev.detekt.gradle.Detekt
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFilter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskCollection
import org.gradle.kotlin.dsl.configure
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

fun Project.koverExcludes(config: Action<KoverReportFilter>) = extensions.configure<KoverProjectExtension> {
  reports.total.filters { excludes(config) }
}
