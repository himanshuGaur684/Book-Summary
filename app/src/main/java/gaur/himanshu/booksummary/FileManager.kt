package gaur.himanshu.booksummary

import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

const val DIRECTORY = "Book Summary"

class FileManager(private val context: Context) {


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

            Type.SHARED -> TODO()
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

            Type.SHARED -> TODO()
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

            Type.SHARED -> TODO()
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