package aktual.app.android

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.biometric.AuthenticationRequest.Biometric.Fallback.DeviceCredential
import androidx.biometric.AuthenticationRequest.Biometric.Strength
import androidx.biometric.AuthenticationRequest.Companion.biometricRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.registerForAuthenticationResult
import logcat.logcat

internal enum class AuthAvailability {
  Available,
  Unavailable,
}

internal sealed interface AuthResult {
  data object Success : AuthResult

  data object Cancelled : AuthResult

  // No screen lock is set up, or the device can't authenticate at all
  data object Unavailable : AuthResult

  data class Failed(val code: Int, val message: CharSequence) : AuthResult
}

// Must be created before the activity is started, since it registers for activity results
internal class AppLockAuthenticator(
  private val activity: ComponentActivity,
  onResult: (AuthResult) -> Unit,
) {
  private val launcher = activity.registerForAuthenticationResult { result ->
    logcat.d { "AppLockAuthenticator result = $result" }
    onResult(result.toAuthResult())
  }

  fun canAuthenticate(): AuthAvailability =
    when (BiometricManager.from(activity).canAuthenticate(AUTHENTICATORS)) {
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
      BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Unavailable
      // Other errors may be temporary or OEM quirks, so try anyway and let the prompt report them
      else -> Available
    }

  fun authenticate(title: String, subtitle: String) {
    val request =
      biometricRequest(title, DeviceCredential) {
        setMinStrength(if (IS_STRONG_SUPPORTED) Strength.Class3() else Strength.Class2)
        setSubtitle(subtitle)
      }
    launcher.launch(request)
  }

  private fun AuthenticationResult.toAuthResult(): AuthResult =
    when (this) {
      is Success -> Success
      is AuthenticationResult.Error ->
        when (errorCode) {
          BiometricPrompt.ERROR_USER_CANCELED,
          BiometricPrompt.ERROR_CANCELED,
          BiometricPrompt.ERROR_NEGATIVE_BUTTON -> Cancelled
          BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
          BiometricPrompt.ERROR_HW_NOT_PRESENT -> Unavailable
          else -> AuthResult.Failed(errorCode, errString)
        }
      is CustomFallbackSelected -> Cancelled
    }

  private companion object {
    // BIOMETRIC_STRONG with DEVICE_CREDENTIAL isn't supported before API 30
    val IS_STRONG_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    val AUTHENTICATORS =
      (if (IS_STRONG_SUPPORTED) BIOMETRIC_STRONG else BIOMETRIC_WEAK) or DEVICE_CREDENTIAL
  }
}
