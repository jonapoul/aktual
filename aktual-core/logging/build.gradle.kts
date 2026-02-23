import blueprint.core.commonMainDependencies

plugins { id("aktual.module.multiplatform") }

kotlin { commonMainDependencies { api(libs.logcat) } }
