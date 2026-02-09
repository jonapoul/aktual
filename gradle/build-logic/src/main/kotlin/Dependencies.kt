import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension as KMPExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

fun KMPExtension.androidHostTestDependencies(
    handler: Action<KotlinDependencyHandler>,
): NamedDomainObjectProvider<KotlinSourceSet> =
    sourceSets.named("androidHostTest") { dependencies(handler) }
