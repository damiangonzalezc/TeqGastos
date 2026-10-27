package cl.duoc.teqgastos.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FlujoAprobacionTest {

    private fun gasto(estado: EstadoGasto, tecnico: String = "ana@teqmed.cl") = Gasto(
        id = 1,
        categoria = Categoria.COMBUSTIBLE,
        medioPago = MedioPago.TARJETA_CORPORATIVA,
        monto = 25_000,
        fecha = LocalDate.of(2026, 10, 20),
        proveedor = "COPEC",
        numeroComprobante = "9988",
        fotoUri = "content://foto",
        latitud = null,
        longitud = null,
        estado = estado,
        tecnicoEmail = tecnico
    )

    @Test fun `jefatura decide solo sobre gastos pendientes`() {
        assertEquals(3, FlujoAprobacion.accionesPermitidas(Rol.JEFATURA, EstadoGasto.PENDIENTE).size)
        assertTrue(FlujoAprobacion.accionesPermitidas(Rol.JEFATURA, EstadoGasto.APROBADO).isEmpty())
    }

    @Test fun `finanzas solo paga gastos aprobados`() {
        assertEquals(listOf(EstadoGasto.PAGADO), FlujoAprobacion.accionesPermitidas(Rol.FINANZAS, EstadoGasto.APROBADO))
        assertTrue(FlujoAprobacion.accionesPermitidas(Rol.FINANZAS, EstadoGasto.PENDIENTE).isEmpty())
    }

    @Test fun `tecnico no aprueba sus propios gastos`() {
        EstadoGasto.entries.forEach {
            assertTrue(FlujoAprobacion.accionesPermitidas(Rol.TECNICO, it).isEmpty())
        }
    }

    @Test fun `solo el duenio corrige un gasto observado`() {
        val ana = Usuario("ana@teqmed.cl", Rol.TECNICO)
        val luis = Usuario("luis@teqmed.cl", Rol.TECNICO)
        assertTrue(FlujoAprobacion.puedeCorregir(ana, gasto(EstadoGasto.OBSERVADO)))
        assertFalse(FlujoAprobacion.puedeCorregir(luis, gasto(EstadoGasto.OBSERVADO)))
        assertFalse(FlujoAprobacion.puedeCorregir(ana, gasto(EstadoGasto.RECHAZADO)))
    }

    @Test fun `exportacion ERP incluye solo aprobados y pagados`() {
        val csv = ErpExporter.generarCsv(
            listOf(gasto(EstadoGasto.APROBADO), gasto(EstadoGasto.PENDIENTE), gasto(EstadoGasto.PAGADO))
        )
        val lineas = csv.trim().lines()
        assertEquals(3, lineas.size) // encabezado + 2 gastos
        assertTrue(lineas[0].startsWith("id;fecha;tecnico"))
        assertFalse(csv.contains("PENDIENTE"))
    }
}
