package com.phuzle.labs.repacks.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.phuzle.labs.repacks.data.remote.github.GitHubRelease
import java.io.File

/** Downloads the release APK asset via [DownloadManager] and hands it to the system package
 * installer via [FileProvider], per PRD §7.1 step 2-3. */
class UpdateInstaller(private val context: Context) {

    fun canRequestInstallPackages(): Boolean = context.packageManager.canRequestPackageInstalls()

    fun requestInstallPermissionIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))

    fun downloadAndInstall(release: GitHubRelease) {
        val url = release.apkAssetUrl ?: return
        val fileName = release.apkAssetName ?: "repacks-update.apk"
        val downloadDir = File(context.getExternalFilesDir(null), "updates").apply { mkdirs() }
        val destFile = File(downloadDir, fileName)
        if (destFile.exists()) destFile.delete()

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(context.getString(com.phuzle.labs.repacks.R.string.app_name) + " update")
            .setDestinationUri(Uri.fromFile(destFile))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (completedId == downloadId) {
                    receiverContext.unregisterReceiver(this)
                    installApk(destFile)
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
