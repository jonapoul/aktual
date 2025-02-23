package actual.gradle

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ConventionAndroidLibrary : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(LibraryPlugin::class.java)
      apply(ConventionAndroidBase::class.java)
    }

    extensions.configure<LibraryExtension> {
      buildFeatures {
        // Force-disable useless build steps. These can be re-enabled on a per-module basis, if they need them
        androidResources = false
        dataBinding = false
        mlModelBinding = false
        prefabPublishing = false
      }

      packaging {
        resources.excludes.add("META-INF/*")
      }

      buildTypes {
        debug {
          enableUnitTestCoverage = true
          enableAndroidTestCoverage = true
        }

        release {
          enableUnitTestCoverage = false
          enableAndroidTestCoverage = false
        }
      }
    }

    extensions.configure<LibraryAndroidComponentsExtension> {
      // disable instrumented tests if androidTest folder doesn't exist
      beforeVariants {
        it.enableAndroidTest = it.enableAndroidTest && projectDir.resolve("src/androidTest").exists()
      }
    }
  }
}
