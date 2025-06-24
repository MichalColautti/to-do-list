package com.example.todolist

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object AttachmentManager {

    fun getAttachmentDir(context: Context): File {
        val dir = File(context.filesDir, "attachments")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun copyAttachmentToAppStorage(context: Context, uri: Uri, name: String): Uri? {
        val destDir = getAttachmentDir(context)
        val destFile = File(destDir, name)

        if (destFile.exists()) {
            return Uri.fromFile(destFile)
        }

        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteUnusedAttachments(context: Context, allTasks: List<Task>) {
        val usedFiles = allTasks.flatMap { it.attachments }.map { File(it.uri.path ?: "") }.toSet()

        val attachmentDir = getAttachmentDir(context)
        attachmentDir.listFiles()?.forEach { file ->
            if (file !in usedFiles) {
                file.delete()
            }
        }
    }
}
