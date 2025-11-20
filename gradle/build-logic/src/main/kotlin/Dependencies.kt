import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension as KMPExtension

fun Project.kspAllConfigs(dependency: Any) = dependencies {
  configurations
    .map { config -> config.name }
    .filter { name -> name.startsWith("ksp") && name != "ksp" && !name.contains("test", ignoreCase = true) }
    .ifEmpty { error("No KSP configurations found in $path") }
    .onEach { name -> logger.info("Applying $dependency to config $name") }
    .forEach { name -> add(name, dependency) }
}

fun KMPExtension.commonMainDependencies(handler: Action<KotlinDependencyHandler>) =
  multiplatformDependencies(name = "commonMain", handler)

fun KMPExtension.commonTestDependencies(handler: Action<KotlinDependencyHandler>) =
  multiplatformDependencies(name = "commonTest", handler)

fun KMPExtension.jvmMainDependencies(handler: Action<KotlinDependencyHandler>) =
  multiplatformDependencies(name = "jvmMain", handler)

fun KMPExtension.jvmTestDependencies(handler: Action<KotlinDependencyHandler>) =
  multiplatformDependencies(name = "jvmTest", handler)

fun KMPExtension.androidMainDependencies(handler: Action<KotlinDependencyHandler>) =
  multiplatformDependencies(name = "androidMain", handler)

fun KMPExtension.androidHostTestDependencies(handler: Action<KotlinDependencyHandler>) =
  multiplatformDependencies(name = "androidHostTest", handler)

private fun KMPExtension.multiplatformDependencies(
  name: String,
  handler: Action<KotlinDependencyHandler>,
) = sourceSets { getByName(name) { dependencies(handler) } }
