package com.example.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File

object AppUpdateInstaller {
    private const val TAG = "AppUpdateInstaller"

    /**
     * Downloads APK from downloadUrl and triggers installation once complete
     */
    fun downloadAndInstall(context: Context, downloadUrl: String, versionName: String) {
        if (downloadUrl.isBlank()) {
            Toast.makeText(context, "Download URL is invalid", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            Toast.makeText(context, "Downloading update v$versionName...", Toast.LENGTH_SHORT).show()

            val fileName = "workcore-update-v$versionName.apk"
            val destinationFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            // If file already exists from previous download, delete it
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("WorkCore Update v$versionName")
                setDescription("Downloading latest app update...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(destinationFile))
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(recvContext: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == downloadId) {
                        try {
                            context.applicationContext.unregisterReceiver(this)
                        } catch (e: Exception) {
                            Log.w(TAG, "Receiver already unregistered: ${e.message}")
                        }

                        if (destinationFile.exists()) {
                            Log.d(TAG, "Download completed, initiating installation: ${destinationFile.absolutePath}")
                            AppUpdateChecker.installApk(context, destinationFile)
                        } else {
                            Log.e(TAG, "Downloaded file not found at ${destinationFile.absolutePath}")
                            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    onCompleteReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    onCompleteReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initiating update download: ${e.message}", e)
            // Fallback: Open in external browser if DownloadManager fails
            AppUpdateChecker.openDownloadUrl(context, downloadUrl)
        }
    }
}
