package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File

data class AppVersion(
    val versionCode: Int = 1,
    val versionName: String = "1.0",
    val downloadUrl: String = "",
    val releaseNotes: String = "",
    val isMandatory: Boolean = false,
    val minRequiredVersion: Int = 1
)

object AppUpdateChecker {
    private const val TAG = "AppUpdateChecker"
    private const val COLLECTION_NAME = "app_config"
    private const val DOCUMENT_NAME = "version_info"

    /**
     * Check if there's a new version available
     * Returns AppVersion if update available, null otherwise
     */
    suspend fun checkForUpdate(context: Context): AppVersion? {
        return try {
            val currentVersion = getCurrentVersionCode(context)
            Log.d(TAG, "Current version: $currentVersion")

            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection(COLLECTION_NAME).document(DOCUMENT_NAME)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                val latestVersionCode = snapshot.getLong("versionCode")?.toInt() ?: 1
                val latestVersionName = snapshot.getString("versionName") ?: "1.0"
                val downloadUrl = snapshot.getString("downloadUrl") ?: ""
                val releaseNotes = snapshot.getString("releaseNotes") ?: ""
                val isMandatory = snapshot.getBoolean("isMandatory") ?: false
                val minRequiredVersion = snapshot.getLong("minRequiredVersion")?.toInt() ?: 1

                Log.d(TAG, "Latest version: $latestVersionCode (Current: $currentVersion)")

                // Check if update is needed
                if (latestVersionCode > currentVersion) {
                    return AppVersion(
                        versionCode = latestVersionCode,
                        versionName = latestVersionName,
                        downloadUrl = downloadUrl,
                        releaseNotes = releaseNotes,
                        isMandatory = isMandatory || currentVersion < minRequiredVersion,
                        minRequiredVersion = minRequiredVersion
                    )
                }
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            null
        }
    }

    /**
     * Get current app version code
     */
    private fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: Exception) {
            Log.e(TAG, "Error getting version code", e)
            1
        }
    }

    /**
     * Get current app version name
     */
    fun getCurrentVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting version name", e)
            "1.0"
        }
    }

    /**
     * Open download URL in browser
     */
    fun openDownloadUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening download URL", e)
        }
    }

    /**
     * Install APK from file
     */
    fun installApk(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK", e)
        }
    }

    /**
     * Save version info to SharedPreferences to avoid showing update dialog repeatedly
     */
    fun markUpdateDismissed(context: Context, versionCode: Int) {
        val prefs = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
        prefs.edit().putInt("dismissed_version", versionCode).apply()
    }

    /**
     * Check if user already dismissed this version's update
     */
    fun isUpdateDismissed(context: Context, versionCode: Int): Boolean {
        val prefs = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
        val dismissedVersion = prefs.getInt("dismissed_version", 0)
        return dismissedVersion >= versionCode
    }

    /**
     * Clear dismissed version (useful for mandatory updates)
     */
    fun clearDismissedVersion(context: Context) {
        val prefs = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
        prefs.edit().remove("dismissed_version").apply()
    }
}
