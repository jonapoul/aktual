plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.javaxInject)
  api(projects.core.coroutines)
  api(projects.db.impl)
}
