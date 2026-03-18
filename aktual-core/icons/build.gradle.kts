import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

kotlin { commonMainDependencies { api(libs.compose.ui) } }
