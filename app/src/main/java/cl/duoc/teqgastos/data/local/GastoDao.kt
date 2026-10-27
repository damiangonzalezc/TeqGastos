package cl.duoc.teqgastos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {

    @Query("SELECT * FROM gastos ORDER BY creadoEn DESC")
    fun observarTodos(): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE tecnicoEmail = :email ORDER BY creadoEn DESC")
    fun observarPorTecnico(email: String): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE estado IN (:estados) ORDER BY creadoEn DESC")
    fun observarPorEstados(estados: List<String>): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE id = :id")
    fun observarPorId(id: Long): Flow<GastoEntity?>

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): GastoEntity?

    /** Cuenta comprobantes iguales, excluyendo el propio gasto cuando se está corrigiendo. */
    @Query(
        "SELECT COUNT(*) FROM gastos " +
            "WHERE proveedor = :proveedor AND numeroComprobante = :numero AND id != :excluirId"
    )
    suspend fun contarComprobante(proveedor: String, numero: String, excluirId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(gasto: GastoEntity): Long

    @Update
    suspend fun actualizar(gasto: GastoEntity)

    @Query("UPDATE gastos SET estado = :estado, observacion = :observacion WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String, observacion: String?)

    @Insert
    suspend fun insertarHistorial(evento: HistorialEntity)

    @Query("SELECT * FROM historial WHERE gastoId = :gastoId ORDER BY fechaMillis ASC, id ASC")
    fun observarHistorial(gastoId: Long): Flow<List<HistorialEntity>>
}
