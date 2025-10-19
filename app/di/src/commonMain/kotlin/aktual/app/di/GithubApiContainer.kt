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
package aktual.app.di

import aktual.api.builder.buildKtorClient
import aktual.core.model.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import github.api.client.GithubApi
import github.api.client.GithubJson

@BindingContainer
@ContributesTo(AppScope::class)
object GithubApiContainer {
  @Provides
  fun apiFactory(
    buildConfig: BuildConfig,
  ): GithubApi.Factory = GithubApi.Factory {
    GithubApi(
      client = buildKtorClient(
        json = GithubJson,
        tag = "GITHUB",
        isDebug = buildConfig.isDebug,
      ),
    )
  }
}
