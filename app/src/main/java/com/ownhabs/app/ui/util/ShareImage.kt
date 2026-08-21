package com.ownhabs.app.ui.util

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Αποθηκεύει ένα recap ImageBitmap σε προσωρινό αρχείο στην cache και ανοίγει το
 * system share sheet — τίποτα δεν "φεύγει" από τη συσκευή παρά μόνο αν ο ίδιος ο
 * χρήστης επιλέξει ρητά έναν παραλήπτη/εφαρμογή στο share sheet.
 */
fun shareRecapImage(context: Context, bitmap: ImageBitmap, fileName: String = "ownhabs_recap.png") {
    val cacheDir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(cacheDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
