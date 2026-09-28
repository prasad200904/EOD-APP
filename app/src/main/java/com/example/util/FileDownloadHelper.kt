package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

data class DownloadResult(
  val success: Boolean,
  val fileName: String,
  val locationDescription: String,
  val contentUri: Uri? = null,
  val error: String? = null
)

object FileDownloadHelper {

  /**
   * Directly downloads and saves a report file into the device's public Downloads directory.
   * Does NOT require copy-paste or showing raw code.
   */
  fun downloadFileDirectly(
    context: Context,
    fileName: String,
    content: String,
    mimeType: String = "text/csv"
  ): DownloadResult {
    var savedUri: Uri? = null
    var savedLocation = "Downloads/$fileName"

    try {
      // 1. Primary path: Use MediaStore.Downloads for direct saving to system Downloads
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
          put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
          resolver.openOutputStream(uri)?.use { os ->
            os.write(content.toByteArray(Charsets.UTF_8))
            os.flush()
          }
          savedUri = uri
          savedLocation = "Downloads folder ($fileName)"
        }
      } else {
        // Fallback for older API: direct write to public Downloads directory
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!publicDownloads.exists()) publicDownloads.mkdirs()
        val file = File(publicDownloads, fileName)
        file.writeText(content, Charsets.UTF_8)
        savedLocation = file.absolutePath
      }

      // Also create a local cached copy for FileProvider opening
      val localCopyFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, fileName)
      localCopyFile.writeText(content, Charsets.UTF_8)

      val viewUri = try {
        FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          localCopyFile
        )
      } catch (e: Exception) {
        savedUri
      }

      return DownloadResult(
        success = true,
        fileName = fileName,
        locationDescription = savedLocation,
        contentUri = viewUri ?: savedUri
      )
    } catch (e: Exception) {
      // Secondary fallback: write to app external files directory
      return try {
        val fallbackFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, fileName)
        fallbackFile.writeText(content, Charsets.UTF_8)
        val fileUri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          fallbackFile
        )
        DownloadResult(
          success = true,
          fileName = fileName,
          locationDescription = "Downloads ($fileName)",
          contentUri = fileUri
        )
      } catch (e2: Exception) {
        DownloadResult(
          success = false,
          fileName = fileName,
          locationDescription = "",
          error = e.localizedMessage ?: e2.localizedMessage
        )
      }
    }
  }

  /**
   * Directly opens the downloaded file with installed viewer applications (Sheets, Excel, Drive, etc.)
   */
  fun openDownloadedFile(context: Context, uri: Uri, mimeType: String = "text/csv") {
    try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(Intent.createChooser(intent, "Open Downloaded File"))
    } catch (e: Exception) {
      Toast.makeText(context, "No app found to open this file directly.", Toast.LENGTH_SHORT).show()
    }
  }

  fun openDownloadedFile(context: Context, result: DownloadResult, mimeType: String = "text/csv") {
    val uri = result.contentUri
    if (uri != null) {
      openDownloadedFile(context, uri, mimeType)
    } else {
      Toast.makeText(context, "Saved to ${result.locationDescription}", Toast.LENGTH_SHORT).show()
    }
  }

  fun shareDownloadedFile(context: Context, result: DownloadResult, mimeType: String = "text/csv") {
    val uri = result.contentUri
    if (uri != null) {
      try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = mimeType
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share ${result.fileName}"))
      } catch (e: Exception) {
        Toast.makeText(context, "Unable to share: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(context, "File saved at ${result.locationDescription}", Toast.LENGTH_SHORT).show()
    }
  }
}
