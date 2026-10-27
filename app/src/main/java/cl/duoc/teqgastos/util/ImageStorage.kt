package cl.duoc.teqgastos.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Crea el archivo donde la cámara guardará la foto del comprobante.
 * Se usa el almacenamiento interno privado: otras apps no pueden leerlo.
 */
object ImageStorage {
    fun crearUriComprobante(context: Context): Uri {
        val dir = File(context.filesDir, "comprobantes").apply { mkdirs() }
        val archivo = File.createTempFile("boleta_${System.currentTimeMillis()}_", ".jpg", dir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }
}
