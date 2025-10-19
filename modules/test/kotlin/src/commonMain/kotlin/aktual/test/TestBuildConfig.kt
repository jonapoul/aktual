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
package aktual.test

import aktual.core.model.BuildConfig
import aktual.core.model.Password
import kotlin.time.Instant

val TestInstant = Instant.fromEpochMilliseconds(1710786854286L) // Mon Mar 18 2024 18:34:14

val TestBuildConfig = BuildConfig(
  buildTime = TestInstant,
  isDebug = false,
  gitHash = "abcd1234",
  versionCode = 123,
  versionName = "1.2.3",
  defaultPassword = Password.Empty,
  defaultServerUrl = null,
)
