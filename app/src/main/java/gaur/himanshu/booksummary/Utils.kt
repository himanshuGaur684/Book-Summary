package gaur.himanshu.booksummary

import android.content.Context
import android.os.Environment
import java.io.File


val DIRECTORY = "Book Summary"

fun Context.createDirectory(): File {
    val directory = File(this.filesDir, DIRECTORY)
    if (directory.exists().not()) {
        directory.mkdir()
    }
    return directory
}

fun Context.createPrivateExternalDir(): File {
    val extDirectory = getExternalFilesDir(null)
    val directory = File(extDirectory, DIRECTORY)
    if (directory.exists().not()) directory.mkdirs()
    return directory
}

fun hasExternalSdCard() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

