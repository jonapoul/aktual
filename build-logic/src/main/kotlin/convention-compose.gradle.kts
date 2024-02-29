import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("convention-kotlin")
  id("convention-android-base")
}

val libs = the<LibrariesForLibs>()

val ext: CommonExtension<*, *, *, *, *> = extensions.findByType<BaseAppModuleExtension>()
  ?: extensions.findByType<LibraryExtension>()
  ?: error("No android extension found in $path")

ext.apply {
  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
  }
}

tasks.withType(KotlinCompile::class.java) {
  kotlinOptions {
    freeCompilerArgs += listOf(
      "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
      "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
      "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
    )
  }
}

val implementation by configurations
val debugImplementation by configurations

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.uiToolingPreview)
  implementation(libs.androidx.compose.material3)
  debugImplementation(libs.androidx.compose.uiTooling)
}
