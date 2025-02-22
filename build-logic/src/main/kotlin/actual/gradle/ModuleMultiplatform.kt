package actual.gradle

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class ModuleMultiplatform : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(KotlinMultiplatformPluginWrapper::class.java)
      apply(ConventionAndroidLibrary::class.java)
      apply(ConventionDiagrams::class.java)
      apply(ConventionKotlinBase::class.java)
      apply(ConventionKover::class.java)
      apply(ConventionIdea::class.java)
      apply(ConventionStyle::class.java)
      apply(ConventionTest::class.java)
//      apply(DependencyAnalysisPlugin::class.java) // doesn't support KMP
//      apply(ConventionSortDependencies::class.java) // doesn't support KMP
    }

    kotlin {
      jvm()
      androidTarget()
    }

    commonTestDependencies {
      testLibraries.forEach { lib -> implementation(lib) }
    }
  }
}

fun Project.commonMainDependencies(handler: KotlinDependencyHandler.() -> Unit) {
  kotlin {
    sourceSets {
      commonMain {
        dependencies(handler)
      }
    }
  }
}

fun Project.androidMainDependencies(handler: KotlinDependencyHandler.() -> Unit) {
  kotlin {
    sourceSets {
      androidMain {
        dependencies(handler)
      }
    }
  }
}

fun Project.jvmMainDependencies(handler: KotlinDependencyHandler.() -> Unit) {
  kotlin {
    sourceSets {
      jvmMain {
        dependencies(handler)
      }
    }
  }
}

fun Project.commonTestDependencies(handler: KotlinDependencyHandler.() -> Unit) {
  kotlin {
    sourceSets {
      commonTest {
        dependencies(handler)
      }
    }
  }
}

fun Project.androidUnitTestDependencies(handler: KotlinDependencyHandler.() -> Unit) {
  kotlin {
    sourceSets {
      androidUnitTest {
        dependencies(handler)
      }
    }
  }
}

fun Project.kspAllConfigs(dependency: Any) {
  dependencies {
    configurations
      .map { config -> config.name }
      .filter { name -> name.startsWith("ksp") && name != "ksp" }
      .ifEmpty { error("No KSP configurations found in $path") }
      .onEach { name -> logger.info("Applying $dependency to config $name") }
      .forEach { name -> add(name, dependency) }
  }
}

private fun Project.kotlin(action: KotlinMultiplatformExtension.() -> Unit) {
  extensions.configure<KotlinMultiplatformExtension>(action)
}

private fun KotlinMultiplatformExtension.sourceSets(configure: Action<NamedDomainObjectContainer<KotlinSourceSet>>) =
  (this as ExtensionAware).extensions.configure("sourceSets", configure)
