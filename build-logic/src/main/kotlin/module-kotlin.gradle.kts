plugins {
  kotlin("jvm")
  id("convention-kotlin")
  id("convention-style")
  id("convention-test")
  id("com.dropbox.dependency-guard")
}

dependencyGuard {
  configuration("runtimeClasspath")
}
