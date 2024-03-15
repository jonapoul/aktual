plugins {
  id("convention-android-library")
  id("convention-style")
  id("convention-test")
  id("com.dropbox.dependency-guard")
}

dependencyGuard {
  configuration("debugCompileClasspath")
  configuration("debugRuntimeClasspath")
  configuration("releaseCompileClasspath")
  configuration("releaseRuntimeClasspath")
}
