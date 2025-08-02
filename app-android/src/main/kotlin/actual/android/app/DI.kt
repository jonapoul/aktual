@file:Suppress("USELESS_ELVIS", "UNNECESSARY_SAFE_CALL")

package actual.android.app

import actual.account.model.Password
import actual.app.di.AndroidAppGraph
import actual.app.di.DebugContainer
import actual.core.model.ServerUrl
import alakazam.kotlin.core.BuildConfig
import android.app.Activity
import android.content.Context
import dev.zacsweers.metro.MapKey
import dev.zacsweers.metro.createGraphFactory
import kotlin.reflect.KClass
import actual.android.app.BuildConfig as ActualBuildConfig

internal fun setUpDi(
  buildConfig: BuildConfig,
  context: Context,
): AndroidAppGraph {
  val factory = createGraphFactory<AndroidAppGraph.Factory>()
  val debug = DebugContainer(
    defaultPassword = { ActualBuildConfig.DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty },
    defaultServerUrl = { ActualBuildConfig.DEFAULT_URL?.let(::ServerUrl) },
  )
  return factory.create(
    context = context,
    buildConfig = buildConfig,
    debug = debug,
  )
}

@MapKey
@Target(AnnotationTarget.CLASS)
internal annotation class ActivityKey(val value: KClass<out Activity>)
