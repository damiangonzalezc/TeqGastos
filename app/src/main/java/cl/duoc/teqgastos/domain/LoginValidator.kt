package cl.duoc.teqgastos.domain

data class LoginErrores(val email: String? = null, val password: String? = null) {
    val hayErrores: Boolean get() = email != null || password != null
}

object LoginValidator {

    const val DOMINIO = "@teqmed.cl"
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validar(email: String, password: String): LoginErrores = LoginErrores(
        email = when {
            email.isBlank() -> "Ingresa tu correo"
            !EMAIL_REGEX.matches(email.trim()) -> "Correo no válido"
            !email.trim().lowercase().endsWith(DOMINIO) -> "Usa tu correo corporativo $DOMINIO"
            else -> null
        },
        password = when {
            password.isBlank() -> "Ingresa tu contraseña"
            password.length < 6 -> "Mínimo 6 caracteres"
            !password.any(Char::isDigit) -> "Debe incluir al menos un número"
            else -> null
        }
    )
}
