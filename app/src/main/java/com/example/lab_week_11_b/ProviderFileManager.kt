package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.concurrent.Executor

// Helper class to manage files in MediaStore
class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {
    // Generate FileInfo for a new photo
    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_$time.jpg"
        // File disimpan di ExternalFilesDir (penyimpanan internal aplikasi yang bisa diakses secara eksternal)
        val file = File(
            context.getExternalFilesDir(fileHelper.getPicturesFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getPicturesFolder(),
            "image/jpeg"
        )
    }

    // Generate FileInfo for a new video
    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_$time.mp4"
        val file = File(
            context.getExternalFilesDir(fileHelper.getVideosFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getVideosFolder(),
            "video/mp4"
        )
    }

    // Insert the finished image file into MediaStore
    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                fileInfo,
                mediaContentHelper.getImageContentUri(),
                mediaContentHelper.generateImageContentValues(it)
            )
        }
    }

    // Insert the finished video file into MediaStore
    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                fileInfo,
                mediaContentHelper.getVideoContentUri(),
                mediaContentHelper.generateVideoContentValues(it)
            )
        }
    }

    // Core function to copy file from temporary location (FileProvider URI) to MediaStore (Content URI)
    private fun insertToStore(fileInfo: FileInfo, contentUri: Uri, contentValues: ContentValues) {
        executor.execute {
            // 1. Insert metadata to MediaStore, getting the final destination URI
            val insertedUri = contentResolver.insert(contentUri, contentValues)

            insertedUri?.let {
                // 2. Open input stream from the temporary file (via FileProvider)
                val inputStream = contentResolver.openInputStream(fileInfo.uri)
                // 3. Open output stream to the final MediaStore location
                val outputStream = contentResolver.openOutputStream(insertedUri)

                // 4. Copy the file content using commons-io IOUtils
                IOUtils.copy(inputStream, outputStream)
            }
        }
    }
}