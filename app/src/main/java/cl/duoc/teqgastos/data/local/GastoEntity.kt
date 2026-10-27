package cl.duoc.teqgastos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla local de gastos. El índice único (proveedor, N° comprobante)
 * impide a nivel de base de datos que una boleta se rinda dos veces.
 */
@Entity(
    tableName = "gastos",
    indices = [Index(value = ["proveedor", "numeroComprobante"], unique = true)]
)
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoria: String,
    val medioPago: String,
    val monto: Int,
    val fechaEpochDay: Long,
    val proveedor: String,
    val numeroComprobante: String,
    val fotoUri: String,
    val latitud: Double?,
    val longitud: Double?,
    val estado: String,
    val observacion: String?,
    val tecnicoEmail: String,
    val creadoEn: Long = System.currentTimeMillis()
)

/** Bitácora de auditoría: cada cambio de estado queda registrado y no se borra. */
@Entity(
    tableName = "historial",
    foreignKeys = [
        ForeignKey(
            entity = GastoEntity::class,
            parentColumns = ["id"],
            childColumns = ["gastoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gastoId")]
)
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gastoId: Long,
    val estado: String,
    val usuarioEmail: String,
    val comentario: String?,
    val fechaMillis: Long = System.currentTimeMillis()
)
