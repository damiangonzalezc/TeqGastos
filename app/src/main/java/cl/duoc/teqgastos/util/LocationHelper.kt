package cl.duoc.teqgastos.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat

/** Obtiene una ubicación puntual con el LocationManager del sistema (sin Google Play Services). */
object LocationHelper {

    /** Llamar solo después de que el usuario concedió el permiso de ubicación. */
    @SuppressLint("MissingPermission")
    fun obtenerUbicacion(context: Context, alTerminar: (Location?) -> Unit) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val proveedor = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> {
                alTerminar(null)
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            lm.getCurrentLocation(
                proveedor,
                null as CancellationSignal?,
                ContextCompat.getMainExecutor(context)
            ) { ubicacion ->
                alTerminar(ubicacion ?: lm.getLastKnownLocation(proveedor))
            }
        } else {
            lm.getLastKnownLocation(proveedor)?.let {
                alTerminar(it)
                return
            }
            @Suppress("DEPRECATION")
            lm.requestSingleUpdate(proveedor, object : LocationListener {
                override fun onLocationChanged(location: Location) = alTerminar(location)
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                override fun onProviderEnabled(provider: String) = Unit
                override fun onProviderDisabled(provider: String) = alTerminar(null)
            }, Looper.getMainLooper())
        }
    }
}
