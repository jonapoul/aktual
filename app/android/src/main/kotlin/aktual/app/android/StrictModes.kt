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
package aktual.app.android

import android.os.Build
import android.os.Build.VERSION_CODES.BAKLAVA
import android.os.Build.VERSION_CODES.Q
import android.os.Build.VERSION_CODES.S
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.StrictMode
import androidx.annotation.ChecksSdkIntAtLeast

internal fun threadPolicy() = StrictMode.ThreadPolicy
  .Builder()
  .ifVersionAtLeast(UPSIDE_DOWN_CAKE) { detectExplicitGc() }
  .detectCustomSlowCalls()
  // .detectDiskReads() // probably should use these two, but too much faff
  // .detectDiskWrites()
  .detectNetwork()
  .detectResourceMismatches()
  .detectUnbufferedIo()
  .penaltyLog()
  .penaltyDeath()
  .build()

internal fun vmPolicy() = StrictMode.VmPolicy
  .Builder()
  .detectActivityLeaks()
  .ifVersionAtLeast(BAKLAVA) { detectBlockedBackgroundActivityLaunch() }
  .detectCleartextNetwork()
  .detectContentUriWithoutPermission()
  .ifVersionAtLeast(Q) { detectCredentialProtectedWhileLocked() }
  .detectFileUriExposure()
  .ifVersionAtLeast(Q) { detectImplicitDirectBoot() }
  .ifVersionAtLeast(S) { detectIncorrectContextUse() }
  .detectLeakedClosableObjects()
  .detectLeakedRegistrationObjects()
  .detectLeakedSqlLiteObjects()
  // .detectNonSdkApiUsage() // blocks usage of layout inspector
  .ifVersionAtLeast(S) { detectUnsafeIntentLaunch() }
  // .detectUntaggedSockets() // need to add manual tags to all network requests - too much faff
  .penaltyLog()
  .penaltyDeath()
  .build()

@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
private inline fun <B> B.ifVersionAtLeast(version: Int, action: B.() -> B): B =
  if (Build.VERSION.SDK_INT >= version) action() else this
