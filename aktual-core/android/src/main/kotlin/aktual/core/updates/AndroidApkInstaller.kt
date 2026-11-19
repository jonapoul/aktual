package aktual.core.updates

import aktual.di.AppScope
import aktual.log
import aktual.updates.domain.ApkInstaller
import alakazam.kotlin.CoroutineContexts
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AndroidApkInstaller
internal constructor(
  private val context: Context,
  private val fileSystem: FileSystem,
  private val contexts: CoroutineContexts,
) : ApkInstaller {
  override val isSupported: Boolean = true

  // The system reports the session's status here, including when it needs the user to confirm
  private val receiver by lazy {
    val receiver = StatusReceiver()
    ContextCompat.registerReceiver(
      context,
      receiver,
      IntentFilter(ACTION_STATUS),
      ContextCompat.RECEIVER_NOT_EXPORTED,
    )
    receiver
  }

  override suspend fun install(apk: Path) {
    receiver
    withContext(contexts.io) {
      val installer = context.packageManager.packageInstaller
      val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
      params.setAppPackageName(context.packageName)
      val sessionId = installer.createSession(params)
      installer.openSession(sessionId).use { session ->
        val size = fileSystem.metadata(apk).size ?: -1L
        session.openWrite(APK_NAME, 0, size).use { output ->
          fileSystem.source(apk).buffer().inputStream().use { input -> input.copyTo(output) }
          session.fsync(output)
        }
        session.commit(statusIntent(sessionId).intentSender)
      }
    }
  }

  // Mutable so the system can add the status extras
  private fun statusIntent(sessionId: Int): PendingIntent =
    PendingIntent.getBroadcast(
      context,
      sessionId,
      Intent(ACTION_STATUS).setPackage(context.packageName),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
    )

  private class StatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val status =
        intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
      if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
        val confirm =
          IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
        confirm?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let(context::startActivity)
      } else if (status != PackageInstaller.STATUS_SUCCESS) {
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        log.w { "Nightly install failed with status $status: $message" }
      }
    }
  }

  private companion object {
    const val ACTION_STATUS = "aktual.core.updates.INSTALL_STATUS"
    const val APK_NAME = "aktual.apk"
  }
}
