plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(projects.core.coroutines)
  api(projects.db.impl)
}
