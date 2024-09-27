package gaur.himanshu.booksummary

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.io.OutputStreamWriter

enum class Type {
    // private storage for app
    INTERNAL,
    PRIVATE_EXTERNAL,
    SHARED
}

const val BOOK_SUMMARY = "BOOK_SUMMARY"


class ScopeStorageManager(private val context: Context, private var uri: Uri? = null) {


    fun setUri(uri: Uri?) {
        this.uri = uri
    }

    fun save(type: Type, summary: Summary): List<Summary> {
        val fileName = summary.fileName.replace(".txt", "").plus(".txt")
        when (type) {
            Type.INTERNAL -> {
                val filePath = context.createDirectory()
                val file = File(filePath, fileName)
                file.writeText(summary.summary)
            }

            Type.PRIVATE_EXTERNAL -> {
                val filePath = context.createPrivateExternalDir()
                val file = File(filePath, fileName)
                file.writeText(summary.summary)
            }

            Type.SHARED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOCUMENTS.plus("/$BOOK_SUMMARY")
                        )
                    }
                    val contentResolver = context.contentResolver
                    val uri = contentResolver.insert(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                        contentValues
                    )
                    uri?.let {
                        context.contentResolver.openOutputStream(it)
                            .use { stream ->
                                OutputStreamWriter(stream).use { writer ->
                                    writer.write(summary.summary)
                                }
                            }
                    }
                } else {
                    val state = Environment.getExternalStorageState()
                    if (Environment.MEDIA_MOUNTED == state) {
                        val directory = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS.plus("/$BOOK_SUMMARY")
                        )
                        if (directory.exists().not()) {
                            directory.mkdirs()
                        }
                        val file = File(directory, fileName)
                        file.writeText(text = summary.summary)
                    }

                }
            }
        }
        return getSummaries(uri)
    }

    fun update(type: Type, summary: Summary): List<Summary> {
        when (type) {
            Type.INTERNAL -> {
                val filePath = context.createDirectory()
                val file = File(filePath, summary.fileName)
                if (file.exists()) {
                    file.writeText(summary.summary)
                }
            }

            Type.PRIVATE_EXTERNAL -> {
                val filePath = context.createPrivateExternalDir()
                val file = File(filePath, summary.fileName)
                if (file.exists()) {
                    file.writeText(summary.summary)
                }
            }

            Type.SHARED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val contentResolver = context.contentResolver

                    val projection = arrayOf(MediaStore.Files.FileColumns._ID)

                    val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
                    val selectionArgs = arrayOf(summary.fileName)

                    val volume = MediaStore.VOLUME_EXTERNAL
                    val uri = MediaStore.Files.getContentUri(volume)

                    val cursor =
                        contentResolver.query(uri, projection, selection, selectionArgs, null)

                    var updatedUri = Uri.parse("")

                    cursor?.let {
                        while (it.moveToFirst()) {
                            val index = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                            val fileId = it.getLong(index)
                            updatedUri = ContentUris.withAppendedId(uri, fileId)
                            break
                        }
                        cursor.close()
                    }

                    val outputStream = contentResolver.openOutputStream(updatedUri)
                    outputStream?.use {
                        outputStream.write(summary.summary.toByteArray())
                        outputStream.flush()
                        outputStream.close()
                    }
                } else {
                    val directory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS.plus("/$BOOK_SUMMARY")
                    )
                    val file = File(directory, summary.fileName)
                    if (file.exists()) {
                        file.writeText(summary.summary)
                    }
                }
            }
        }
        return getSummaries(uri)
    }

    fun delete(type: Type, summary: Summary): List<Summary> {
        when (type) {
            Type.INTERNAL -> {
                val filePath = context.createDirectory()
                val file = File(filePath, summary.fileName)
                if (file.exists()) {
                    file.delete()
                }
            }

            Type.PRIVATE_EXTERNAL -> {
                val filePath = context.createPrivateExternalDir()
                val file = File(filePath, summary.fileName)
                if (file.exists()) {
                    file.delete()
                }
            }

            Type.SHARED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val contentResolver = context.contentResolver
                    val projection = arrayOf(
                        MediaStore.Files.FileColumns._ID
                    )
                    val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
                    val selectionArgs = arrayOf(summary.fileName)

                    val volume = MediaStore.VOLUME_EXTERNAL
                    val uri = MediaStore.Files.getContentUri(volume)

                    val cursor =
                        contentResolver.query(uri, projection, selection, selectionArgs, null)
                    var deleteUri = Uri.parse("")
                    cursor?.let {
                        while (it.moveToFirst()) {
                            val index = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                            val fileId = it.getLong(index)
                            deleteUri = ContentUris.withAppendedId(uri, fileId)
                            break
                        }
                        it.close()
                    }
                    contentResolver.delete(deleteUri, null, null)
                } else {
                    val directory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS.plus("/$BOOK_SUMMARY")
                    )
                    if (directory.exists().not()) directory.mkdirs()
                    val file = File(directory, summary.fileName)
                    if (file.exists()) file.delete()
                }
            }
        }
        return getSummaries(uri)
    }

    fun readSummaries(uri: Uri? = null) = callbackFlow<List<Summary>> {
        trySend(getSummaries(uri))
        awaitClose { }
    }

    private fun getSummaries(uri: Uri? = null): List<Summary> {
        val list = mutableListOf<Summary>()
        // internal
        context.createDirectory().listFiles()?.map { file ->
            Summary(file.name, file.readText(), Type.INTERNAL)
        }?.let { list.addAll(it) }

        // private external
        context.createPrivateExternalDir().listFiles()?.map { file ->
            Summary(file.name, file.readText(), Type.PRIVATE_EXTERNAL)
        }?.let { list.addAll(it) }

        // public external
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && uri != null) {
            val tempList = mutableListOf<Summary>()
            val directory = DocumentFile.fromTreeUri(context, uri)
            if (directory != null && directory.isDirectory) {
                for (documentFile in directory.listFiles()) {
                    if (documentFile.isFile) {
                        val fileName = documentFile.name
                        val content = context.contentResolver.openInputStream(documentFile.uri)
                            ?.bufferedReader()?.use { it.readText() }
                        tempList.add(
                            Summary(fileName.toString(), content.toString(), Type.SHARED)
                        )
                    }
                }
            } else {
                println("FileInfo" + "The URI does not represent a valid directory.")
            }
            list.addAll(tempList)
        } else {
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS.plus("/$BOOK_SUMMARY"))
            directory.listFiles()?.map {
                Summary(it.name, it.readText(), Type.SHARED)
            }?.let { list.addAll(it) }
        }
        return list
    }
}


