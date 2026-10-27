package cl.duoc.teqgastos.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate

/** Guarda el CSV de exportación en caché y abre el selector para compartirlo (correo, Drive, etc.). */
object ShareHelper {
    fun compartirCsv(context: Context, contenido: String) {
        val dir = File(context.cacheDir, "exportes").apply { mkdirs() }
        val archivo = File(dir, "teqgastos_erp_${LocalDate.now()}.csv")
        // BOM UTF-8 para que Excel muestre bien los acentos
        archivo.writeText("﻿" + contenido, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Exportación TeqGastos para ERP")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar a ERP"))
    }
}
