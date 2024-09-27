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

const val DIRECTORY = "Book Summary"

class FileManager(private val context: Context, private var uri: Uri? = null) {


    fun setUri(uri: Uri?) {
        this.uri = uri
    }

    fun save(summary: Summary): List<Summary> {

        val fileName = summary.fileName.replace(".txt", "").plus(".txt")

        when (summary.type) {
            Type.INTERNAL -> {
                val directory = context.createDirectory()
                val file = File(directory, fileName)
                file.writeText(summary.summary)
            }

            Type.PRIVATE_EXTERNAL -> {
                val directory = context.createPrivateDir()
                val file = File(directory, fileName)
                file.writeText(summary.summary)
            }

            Type.SHARED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOCUMENTS.plus("/$DIRECTORY")
                        )
                    }
                    val contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    val uri = context.contentResolver.insert(
                        contentUri, contentValues
                    )

                    uri?.let {
                        context.contentResolver.openOutputStream(it).use {
                            OutputStreamWriter(it).use {
                                it.write(summary.summary)
                            }
                        }
                    }

                } else {
                    val state = Environment.getExternalStorageState()
                    if (state == Environment.MEDIA_MOUNTED) {
                        val directory = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS.plus("/$DIRECTORY")
                        )
                        if (directory.exists().not()) directory.mkdirs()

                        val file = File(directory, fileName)
                        file.writeText(summary.summary)
                    }
                }


            }
        }

        return getSummries()
    }


    fun delete(summary: Summary): List<Summary> {
        when (summary.type) {
            Type.INTERNAL -> {
                val directory = context.createDirectory()
                val file = File(directory, summary.fileName)
                if (file.exists()) file.delete()
            }

            Type.PRIVATE_EXTERNAL -> {
                val directory = context.createPrivateDir()
                val file = File(directory, summary.fileName)
                if (file.exists()) file.delete()
            }

            Type.SHARED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val contentResolver = context.contentResolver
                    val projection = arrayOf(MediaStore.MediaColumns._ID)
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                    val selectionArgs = arrayOf(summary.fileName)
                    val pathUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    val cursor =
                        contentResolver.query(pathUri, projection, selection, selectionArgs, null)
                    var deleteUri = Uri.parse("")
                    cursor?.let {
                        while (it.moveToFirst()) {
                            val index = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                            val fileId = it.getLong(index)
                            deleteUri = ContentUris.withAppendedId(pathUri, fileId)
                        }
                        it.close()
                    }
                    context.contentResolver.delete(deleteUri, null, null)
                } else {
                    val directory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS.plus("/$DIRECTORY")
                    )
                    val file = File(directory, summary.fileName)
                    if (file.exists()) file.delete()
                }
            }
        }
        return getSummries()
    }

    fun update(summary: Summary): List<Summary> {
        when (summary.type) {
            Type.INTERNAL -> {
                val directory = context.createDirectory()
                val file = File(directory, summary.fileName)
                if (file.exists()) {
                    file.writeText(summary.summary)
                }
            }

            Type.PRIVATE_EXTERNAL -> {
                val directory = context.createPrivateDir()
                val file = File(directory, summary.fileName)
                if (file.exists()) file.writeText(summary.summary)
            }

            Type.SHARED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val contentResolver = context.contentResolver

                    val projection = arrayOf(MediaStore.MediaColumns._ID)
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                    val selectionArgs = arrayOf(summary.fileName)
                    val pathUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    val cursor =
                        contentResolver.query(pathUri, projection, selection, selectionArgs, null)

                    var updateUri = Uri.parse("")

                    cursor?.let {
                        while (it.moveToFirst()) {
                            val index = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                            val fileId = it.getLong(index)
                            updateUri = ContentUris.withAppendedId(pathUri, fileId)
                        }
                        it.close()
                    }
                    contentResolver.openOutputStream(updateUri)?.use {
                        it.write(summary.summary.toByteArray())
                    }
                } else {
                    val directory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS.plus("/$DIRECTORY")
                    )
                    val file = File(directory, summary.fileName)
                    if (file.exists()) {
                        file.writeText(summary.summary)
                    }
                }


            }
        }

        return getSummries()
    }

    fun getSummriesFlow() = callbackFlow<List<Summary>> {
        trySend(getSummries())
        awaitClose { }
    }

    fun getSummries(): List<Summary> {
        val list = mutableListOf<Summary>()

        context.createDirectory().listFiles()?.map {
            Summary(it.name, it.readText(), Type.INTERNAL)
        }?.let {
            list.addAll(it)
        }

        context.createPrivateDir().listFiles()?.map {
            Summary(it.name, it.readText(), Type.PRIVATE_EXTERNAL)
        }?.let {
            list.addAll(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && uri != null) {
            DocumentFile.fromTreeUri(context, uri!!)?.listFiles()?.filter { it.isFile }?.map {
                val fileName = it.name
                val content = context.contentResolver.openInputStream(it.uri)?.bufferedReader()
                    ?.use { it.readText() }
                Summary(fileName.toString(), content.toString(), Type.SHARED)

            }?.let {
                list.addAll(it)
            }

        } else {
            val directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS.plus("/$DIRECTORY")
            )
            directory.listFiles()?.map {
                Summary(it.name, it.readText(), Type.SHARED)
            }?.let {
                list.addAll(it)
            }

        }

        return list

    }

    fun Context.createDirectory(): File {
        val directory = filesDir
        val file = File(directory, DIRECTORY)
        if (!file.exists()) file.mkdir()
        return file
    }

    fun Context.createPrivateDir(): File {
        val directory = getExternalFilesDir(null)
        val file = File(directory, DIRECTORY)
        if (file.exists().not()) file.mkdir()
        return file
    }


}