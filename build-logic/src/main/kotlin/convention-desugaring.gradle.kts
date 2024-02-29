@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs

val libExtension = extensions.findByType(LibraryExtension::class)
val appExtension = extensions.findByType(ApplicationExtension::class)

when {
  libExtension != null -> extensions.configure<LibraryExtension> {
    compileOptions {
      isCoreLibraryDesugaringEnabled = true
    }
  }

  appExtension != null -> extensions.configure<ApplicationExtension> {
    compileOptions {
      isCoreLibraryDesugaringEnabled = true
    }
  }

  else -> error("No android extension found!")
}

val libs = the<LibrariesForLibs>()
val coreLibraryDesugaring by configurations

dependencies {
  coreLibraryDesugaring(libs.android.desugaring)
}
