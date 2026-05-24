plugins { id("aktual.module.jvm") }

dependencies {
  compileOnly(libs.detekt.api)
  testImplementation(kotlin("test"))
  testImplementation(libs.detekt.test)
  testImplementation(libs.detekt.testJunit)
  testImplementation(libs.detekt.testUtils)
  testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
