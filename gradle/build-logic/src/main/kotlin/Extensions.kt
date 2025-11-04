import blueprint.core.multiplatformDependencies
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import blueprint.core.kspAllConfigs as blueprintKspAllConfigs
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension as KMPExtension

fun Project.kspAllConfigs(dependency: Any) = blueprintKspAllConfigs(dependency)

fun KMPExtension.commonMainDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "commonMain", handler)

fun KMPExtension.commonTestDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "commonTest", handler)

fun KMPExtension.jvmMainDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "jvmMain", handler)

fun KMPExtension.jvmTestDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "jvmTest", handler)

fun KMPExtension.androidMainDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "androidMain", handler)

fun KMPExtension.androidUnitTestDependencies(handler: KotlinDependencyHandler.() -> Unit): Unit =
  multiplatformDependencies(name = "androidUnitTest", handler)
