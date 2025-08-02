
package actual.android.app

import actual.l10n.R
import alakazam.kotlin.core.BasicBuildConfig
import android.content.Context
import android.os.Build
import actual.android.app.BuildConfig as AppBuildConfig
import alakazam.kotlin.core.BuildConfig as AlakazamBuildConfig

internal fun buildConfig(context: Context): AlakazamBuildConfig = BasicBuildConfig(
  debug = AppBuildConfig.DEBUG,
  applicationId = AppBuildConfig.APPLICATION_ID,
  versionCode = AppBuildConfig.VERSION_CODE,
  versionName = AppBuildConfig.VERSION_NAME,
  buildTime = AppBuildConfig.BUILD_TIME,
  gitId = AppBuildConfig.GIT_HASH,
  manufacturer = Build.MANUFACTURER,
  model = Build.MODEL,
  os = Build.VERSION.SDK_INT,
  platform = context.getString(R.string.app_name),
  repoName = REPO_NAME,
  repoUrl = "https://github.com/${REPO_NAME}",
)

private const val REPO_NAME = "jonapoul/actual-android"
