/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.app.android

import actual.core.di.ProviderMap
import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Intent
import androidx.core.app.AppComponentFactory

/**
 * From https://github.com/ZacSweers/metro/blob/main/samples/compose-navigation-app/src/main/kotlin/dev/zacsweers/metro/sample/androidviewmodel/components/MetroAppComponentFactory.kt
 */
@Suppress("unused") // referenced in XML
class ActualAppComponentFactory : AppComponentFactory() {
  override fun instantiateApplicationCompat(cl: ClassLoader, className: String): Application =
    super.instantiateApplicationCompat(cl, className).also { app ->
      val graph = (app as ActualApplication).get()
      activityProviders = graph.activityProviders
      broadcastReceiverProviders = graph.broadcastReceiverProviders
      contentProviderProviders = graph.contentProviderProviders
      serviceProviders = graph.serviceProviders
    }

  override fun instantiateActivityCompat(cl: ClassLoader, className: String, intent: Intent?): Activity =
    getInstance(cl, className, activityProviders) ?: super.instantiateActivityCompat(cl, className, intent)

  override fun instantiateReceiverCompat(cl: ClassLoader, className: String, intent: Intent?): BroadcastReceiver =
    getInstance(cl, className, broadcastReceiverProviders) ?: super.instantiateReceiverCompat(cl, className, intent)

  override fun instantiateServiceCompat(cl: ClassLoader, className: String, intent: Intent?): Service =
    getInstance(cl, className, serviceProviders) ?: super.instantiateServiceCompat(cl, className, intent)

  override fun instantiateProviderCompat(cl: ClassLoader, className: String): ContentProvider =
    getInstance(cl, className, contentProviderProviders) ?: super.instantiateProviderCompat(cl, className)

  private inline fun <reified T : Any> getInstance(
    cl: ClassLoader,
    className: String,
    providers: ProviderMap<T>,
  ): T? {
    val clazz = Class.forName(className, false, cl).asSubclass(T::class.java)
    val modelProvider = providers[clazz.kotlin] ?: return null
    return modelProvider()
  }

  // AppComponentFactory can be created multiple times
  private companion object {
    lateinit var activityProviders: ProviderMap<Activity>
    lateinit var broadcastReceiverProviders: ProviderMap<BroadcastReceiver>
    lateinit var contentProviderProviders: ProviderMap<ContentProvider>
    lateinit var serviceProviders: ProviderMap<Service>
  }
}
