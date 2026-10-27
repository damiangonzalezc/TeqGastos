package cl.duoc.teqgastos.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GastoValidatorTest {

    private val hoy = LocalDate.of(2026, 10, 27)

    private fun valido() = GastoInput(
        categoria = Categoria.PEAJE,
        medioPago = MedioPago.DINERO_PROPIO,
        monto = "3200",
        fecha = "25/10/2026",
        proveedor = "Autopista del Itata",
        numeroComprobante = "123456",
        fotoUri = "content://foto"
    )

    @Test fun `gasto completo no tiene errores`() {
        assertFalse(GastoValidator.validar(valido(), hoy).hayErrores)
        assertEquals(GastoInput.TOTAL_CAMPOS, valido().camposCompletos)
    }

    @Test fun `monto sobre el tope de la categoria es rechazado`() {
        assertNotNull(GastoValidator.validar(valido().copy(monto = "20000"), hoy).monto)
    }

    @Test fun `gasto fuera de plazo es rechazado`() {
        assertNotNull(GastoValidator.validar(valido().copy(fecha = "01/08/2026"), hoy).fecha)
    }

    @Test fun `al corregir, el plazo se mide desde el registro original`() {
        val registradoEl = LocalDate.of(2026, 9, 20)
        val input = valido().copy(fecha = "10/09/2026")
        assertNull(GastoValidator.validar(input, registradoEl).fecha)
    }

    @Test fun `fecha futura e invalida son rechazadas`() {
        assertNotNull(GastoValidator.validar(valido().copy(fecha = "30/10/2026"), hoy).fecha)
        assertNotNull(GastoValidator.validar(valido().copy(fecha = "31/02/2026"), hoy).fecha)
    }

    @Test fun `sin foto ni medio de pago es rechazado`() {
        val e = GastoValidator.validar(valido().copy(fotoUri = null, medioPago = null), hoy)
        assertNotNull(e.foto)
        assertNotNull(e.medioPago)
        assertEquals(2, e.cantidad)
    }

    @Test fun `rechazar exige observacion, aprobar no`() {
        assertNotNull(GastoValidator.validarRevision(EstadoGasto.RECHAZADO, ""))
        assertNotNull(GastoValidator.validarRevision(EstadoGasto.OBSERVADO, "corto"))
        assertNull(GastoValidator.validarRevision(EstadoGasto.APROBADO, ""))
    }

    @Test fun `login exige correo corporativo y contrasena con numero`() {
        assertTrue(LoginValidator.validar("juan@gmail.com", "secreto1").hayErrores)
        assertTrue(LoginValidator.validar("juan.perez@teqmed.cl", "secreto").hayErrores)
        assertFalse(LoginValidator.validar("juan.perez@teqmed.cl", "secreto1").hayErrores)
    }
}
