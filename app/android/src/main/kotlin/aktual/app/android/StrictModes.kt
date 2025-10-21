/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
