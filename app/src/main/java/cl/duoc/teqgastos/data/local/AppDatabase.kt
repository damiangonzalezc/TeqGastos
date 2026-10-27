package cl.duoc.teqgastos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GastoEntity::class, HistorialEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gastoDao(): GastoDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teqgastos.db"
                )
                    // Prototipo: si cambia el esquema se recrea la base local
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
    }
}
