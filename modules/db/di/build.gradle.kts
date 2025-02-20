plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(projects.db.api)
  api(projects.db.impl)
}
