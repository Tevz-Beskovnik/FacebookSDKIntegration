package pora.predstavitev.app

import android.content.res.Resources
import android.net.Uri
import android.os.Environment.DIRECTORY_MOVIES
import java.io.File
import java.io.FileOutputStream

fun getVideoUriFromRawResource(rawResourceId: Int, resources: Resources, parent: File): Uri? {
    try {
        val videoFile = File(parent, "shared_video.mp4")
        val inputStream = resources.openRawResource(rawResourceId)
        val outputStream = FileOutputStream(videoFile)

        val buffer = ByteArray(1024)
        var length: Int
        while ((inputStream.read(buffer).also { length = it }) > 0) {
            outputStream.write(buffer, 0, length)
        }

        inputStream.close()
        outputStream.close()

        return Uri.fromFile(videoFile)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }
}

fun getVideoFromRawResource(rawResourceId: Int, resources: Resources, parent: File): File? {
    try {
        val videoFile = File(parent, "shared_video.mp4")
        val inputStream = resources.openRawResource(rawResourceId)
        val outputStream = FileOutputStream(videoFile)

        val buffer = ByteArray(1024)
        var length: Int
        while ((inputStream.read(buffer).also { length = it }) > 0) {
            outputStream.write(buffer, 0, length)
        }

        inputStream.close()
        outputStream.close()

        return videoFile
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }
}