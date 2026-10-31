package aktual.app.android

import android.os.Build
import android.os.Build.VERSION_CODES.BAKLAVA
import android.os.Build.VERSION_CODES.Q
import android.os.Build.VERSION_CODES.S
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.StrictMode
import androidx.annotation.ChecksSdkIntAtLeast

internal fun threadPolicy() =
  StrictMode.ThreadPolicy.Builder()
    .ifVersionAtLeast(UPSIDE_DOWN_CAKE) { detectExplicitGc() }
    .detectCustomSlowCalls()
    .detectNetwork()
    .detectResourceMismatches()
    .detectUnbufferedIo()
    .penaltyLog()
    .penaltyDeath()
    .build()

internal fun vmPolicy() =
  StrictMode.VmPolicy.Builder()
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
    .penaltyLog()
    .penaltyDeath()
    .build()

@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
private inline fun <B> B.ifVersionAtLeast(version: Int, action: B.() -> B): B =
  if (Build.VERSION.SDK_INT >= version) action() else this
