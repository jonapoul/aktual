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
package aktual.api.builder

import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

fun interface ClientFactory {
  operator fun invoke(json: Json): HttpClient
}

@Inject
@ContributesBinding(AppScope::class)
class ClientFactoryImpl(private val buildConfig: BuildConfig) : ClientFactory {
  override fun invoke(json: Json) = buildKtorClient(json, isDebug = buildConfig.isDebug, tag = "ACTUAL")
}
