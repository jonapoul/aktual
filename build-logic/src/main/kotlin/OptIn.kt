import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.optIn(className: String) {
  tasks.withType(KotlinCompile::class.java) {
    kotlinOptions.freeCompilerArgs += "-opt-in=$className"
  }
}
