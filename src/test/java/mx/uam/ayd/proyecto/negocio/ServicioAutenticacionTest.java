package mx.uam.ayd.proyecto.negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mx.uam.ayd.proyecto.datos.EmpleadoRepository;
import mx.uam.ayd.proyecto.negocio.modelo.Empleado;

/**
 * Pruebas unitarias de {@link ServicioAutenticacion} (HU-01).
 *
 * <p>Cubren los cuatro escenarios de aceptación:</p>
 * <ol>
 *   <li>Ingreso exitoso y rol devuelto.</li>
 *   <li>Bloqueo al quinto intento fallido (y desbloqueo a los 15 minutos).</li>
 *   <li>Prevención de sesión simultánea (y liberación por cierre o inactividad).</li>
 *   <li>Formato de usuario inválido.</li>
 * </ol>
 *
 * <p>El repositorio se simula con Mockito y el tiempo con {@link RelojFalso},
 * así que las pruebas no necesitan Spring, base de datos ni esperas reales.</p>
 */
@ExtendWith(MockitoExtension.class)
class ServicioAutenticacionTest {

    private static final String USUARIO = "cocinero1";
    private static final String PASSWORD = "Cocina123!";

    @Mock
    private EmpleadoRepository empleadoRepository;

    private RelojFalso reloj;
    private ServicioAutenticacion servicio;

    /** Reloj cuyo tiempo se avanza manualmente desde la prueba. */
    private static class RelojFalso extends Clock {
        private Instant instante = Instant.parse("2026-10-28T12:00:00Z");

        void avanzar(Duration duracion) {
            instante = instante.plus(duracion);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instante;
        }
    }

    @BeforeEach
    void preparar() {
        reloj = new RelojFalso();
        servicio = new ServicioAutenticacion(empleadoRepository, reloj);
    }

    /** Simula que el empleado existe en la BD con su contraseña ya hasheada. */
    private void existeEmpleado(String usuario, String password, String rol) {
        when(empleadoRepository.findByUsuario(usuario)).thenReturn(Optional.of(
                new Empleado("Nombre de Prueba", usuario, UtilidadPassword.encriptar(password), rol)));
    }

    /** Ejecuta un login que debe fallar y devuelve el mensaje de error. */
    private String mensajeDeError(String usuario, String password) {
        return assertThrows(IllegalArgumentException.class,
                () -> servicio.loginEmpleado(usuario, password)).getMessage();
    }

    // ---------------------------------------------------------------
    // Escenario 1: ingreso exitoso y redirección por rol
    // ---------------------------------------------------------------

    @ParameterizedTest(name = "{0} entra y recibe el rol {1}")
    @CsvSource({
        "cocinero1,Cocinero",
        "recepcion1,Recepcionista",
        "repartidor1,Repartidor",
        "admin,Administrador"
    })
    @DisplayName("Escenario 1: credenciales válidas devuelven el rol del empleado")
    void loginExitosoDevuelveElRol(String usuario, String rol) {
        existeEmpleado(usuario, PASSWORD, rol);

        assertEquals(rol, servicio.loginEmpleado(usuario, PASSWORD));
        assertTrue(servicio.haySesionActiva(usuario));
    }

    @Test
    @DisplayName("Escenario 1: un login exitoso reinicia el contador de intentos fallidos")
    void loginExitosoReiniciaIntentos() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");

        mensajeDeError(USUARIO, "mala1");
        mensajeDeError(USUARIO, "mala2");
        servicio.loginEmpleado(USUARIO, PASSWORD);
        servicio.cerrarSesion(USUARIO);

        assertTrue(mensajeDeError(USUARIO, "mala3").contains("Te quedan 4 intentos"));
    }

    @Test
    @DisplayName("Contraseña incorrecta: mensaje con los intentos restantes")
    void contrasenaIncorrectaInformaIntentosRestantes() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");

        String mensaje = mensajeDeError(USUARIO, "incorrecta");

        assertTrue(mensaje.contains("Credenciales incorrectas"));
        assertTrue(mensaje.contains("Te quedan 4 intentos"));
        assertFalse(servicio.haySesionActiva(USUARIO));
    }

    @Test
    @DisplayName("Usuario inexistente da el mismo mensaje que contraseña incorrecta")
    void usuarioInexistenteNoRevelaQueNoExiste() {
        when(empleadoRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());

        assertTrue(mensajeDeError("fantasma", PASSWORD).contains("Credenciales incorrectas"));
    }

    @Test
    @DisplayName("La contraseña guardada en texto plano (sin hash) no se acepta")
    void contrasenaEnTextoPlanoEnLaBdNoSeAcepta() {
        when(empleadoRepository.findByUsuario(USUARIO)).thenReturn(Optional.of(
                new Empleado("Nombre de Prueba", USUARIO, PASSWORD, "Cocinero")));

        assertTrue(mensajeDeError(USUARIO, PASSWORD).contains("Credenciales incorrectas"));
    }

    // ---------------------------------------------------------------
    // Escenario 2: bloqueo por exceder intentos fallidos
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Escenario 2: el quinto intento fallido bloquea la cuenta")
    void quintoIntentoFallidoBloqueaLaCuenta() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");

        for (int i = 1; i <= 4; i++) {
            assertTrue(mensajeDeError(USUARIO, "mala" + i).contains("Credenciales incorrectas"));
        }
        String mensaje = mensajeDeError(USUARIO, "mala5");

        assertTrue(mensaje.contains("bloqueada"));
        assertTrue(mensaje.contains("15 minutos"));
    }

    @Test
    @DisplayName("Escenario 2: con la cuenta bloqueada ni la contraseña correcta entra")
    void cuentaBloqueadaRechazaLaContrasenaCorrecta() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        for (int i = 1; i <= 5; i++) {
            mensajeDeError(USUARIO, "mala" + i);
        }

        assertTrue(mensajeDeError(USUARIO, PASSWORD).contains("bloqueada"));
        assertFalse(servicio.haySesionActiva(USUARIO));
    }

    @Test
    @DisplayName("El bloqueo termina a los 15 minutos y el contador se reinicia")
    void bloqueoTerminaDespuesDeQuinceMinutos() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        for (int i = 1; i <= 5; i++) {
            mensajeDeError(USUARIO, "mala" + i);
        }

        reloj.avanzar(Duration.ofMinutes(14));
        assertTrue(mensajeDeError(USUARIO, PASSWORD).contains("bloqueada"));

        reloj.avanzar(Duration.ofMinutes(1).plusSeconds(1));
        assertEquals("Cocinero", servicio.loginEmpleado(USUARIO, PASSWORD));
    }

    @Test
    @DisplayName("El bloqueo de un usuario no afecta a otro")
    void bloqueoEsPorUsuario() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        existeEmpleado("recepcion1", PASSWORD, "Recepcionista");
        for (int i = 1; i <= 5; i++) {
            mensajeDeError(USUARIO, "mala" + i);
        }

        assertEquals("Recepcionista", servicio.loginEmpleado("recepcion1", PASSWORD));
    }

    // ---------------------------------------------------------------
    // Escenario 3: prevención de sesión simultánea
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Escenario 3: no se permite una segunda sesión con el mismo usuario")
    void segundaSesionEsDenegada() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        servicio.loginEmpleado(USUARIO, PASSWORD);

        assertTrue(mensajeDeError(USUARIO, PASSWORD).contains("ya está activa"));
    }

    @Test
    @DisplayName("Con sesión activa, una contraseña incorrecta NO revela que hay sesión")
    void sesionActivaNoSeRevelaConContrasenaIncorrecta() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        servicio.loginEmpleado(USUARIO, PASSWORD);

        String mensaje = mensajeDeError(USUARIO, "incorrecta");

        assertTrue(mensaje.contains("Credenciales incorrectas"));
        assertFalse(mensaje.contains("sesión"));
    }

    @Test
    @DisplayName("Cerrar sesión permite volver a iniciar")
    void cerrarSesionLiberaAlUsuario() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        servicio.loginEmpleado(USUARIO, PASSWORD);

        servicio.cerrarSesion(USUARIO);

        assertFalse(servicio.haySesionActiva(USUARIO));
        assertEquals("Cocinero", servicio.loginEmpleado(USUARIO, PASSWORD));
    }

    @Test
    @DisplayName("La sesión se libera sola tras 10 minutos de inactividad")
    void sesionExpiraPorInactividad() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        servicio.loginEmpleado(USUARIO, PASSWORD);

        reloj.avanzar(ServicioAutenticacion.INACTIVIDAD_MAXIMA.plusSeconds(1));

        assertFalse(servicio.haySesionActiva(USUARIO));
        assertEquals("Cocinero", servicio.loginEmpleado(USUARIO, PASSWORD));
    }

    @Test
    @DisplayName("registrarActividad() mantiene viva la sesión")
    void registrarActividadExtiendeLaSesion() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        servicio.loginEmpleado(USUARIO, PASSWORD);

        reloj.avanzar(Duration.ofMinutes(6));
        servicio.registrarActividad(USUARIO);
        reloj.avanzar(Duration.ofMinutes(6)); // 12 min desde el login, 6 desde la actividad

        assertTrue(servicio.haySesionActiva(USUARIO));
        assertTrue(mensajeDeError(USUARIO, PASSWORD).contains("ya está activa"));
    }

    @Test
    @DisplayName("registrarActividad() de un usuario sin sesión no crea una sesión")
    void registrarActividadSinSesionNoHaceNada() {
        servicio.registrarActividad(USUARIO);

        assertFalse(servicio.haySesionActiva(USUARIO));
    }

    // ---------------------------------------------------------------
    // Escenario 4: formato de usuario inválido
    // ---------------------------------------------------------------

    @ParameterizedTest(name = "usuario inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"ad min", "admin!", "us@er", "<script>", "a'b", "ab",
            "nombre_demasiado_largo_para_el_sistema", "usuario;DROP", "ñandú"})
    @DisplayName("Escenario 4: un usuario con formato inválido se rechaza sin consultar la BD")
    void usuarioConFormatoInvalidoSeRechaza(String usuarioInvalido) {
        String mensaje = mensajeDeError(usuarioInvalido, PASSWORD);

        assertEquals("El usuario no es válido.", mensaje);
        verifyNoInteractions(empleadoRepository);
    }

    @Test
    @DisplayName("Un formato inválido no cuenta como intento fallido")
    void formatoInvalidoNoConsumeIntentos() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");

        for (int i = 0; i < 6; i++) {
            mensajeDeError("usuario inválido!", "x");
        }

        assertTrue(mensajeDeError(USUARIO, "incorrecta").contains("Te quedan 4 intentos"));
    }

    @ParameterizedTest(name = "contraseña vacía: [{0}]")
    @NullAndEmptySource
    @DisplayName("Una contraseña vacía se rechaza sin consultar la BD")
    void contrasenaVaciaSeRechaza(String vacia) {
        assertEquals("Ingresa tu usuario y contraseña.", mensajeDeError(USUARIO, vacia));
        verifyNoInteractions(empleadoRepository);
    }

    @ParameterizedTest(name = "[{0}] es un usuario válido")
    @ValueSource(strings = {"admin", "cocinero1", "Recepcion_2", "abc", "usuario_de_20_chars_"})
    @DisplayName("Usuarios con letras, números y guion bajo son válidos")
    void usuariosValidos(String usuario) {
        assertTrue(servicio.esUsuarioValido(usuario));
    }

    // ---------------------------------------------------------------
    // Sesión de administrador (la usa el módulo de personal)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Sin ningún login no hay administrador autenticado")
    void sinLoginNoHayAdministrador() {
        assertFalse(servicio.isAdminAutenticado());
    }

    @Test
    @DisplayName("Un administrador con sesión activa se reconoce como autenticado")
    void administradorConSesionEsReconocido() {
        existeEmpleado("admin", PASSWORD, "Administrador");

        servicio.loginEmpleado("admin", PASSWORD);

        assertTrue(servicio.isAdminAutenticado());
    }

    @Test
    @DisplayName("Un cocinero con sesión NO cuenta como administrador")
    void cocineroNoEsAdministrador() {
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");

        servicio.loginEmpleado(USUARIO, PASSWORD);

        assertFalse(servicio.isAdminAutenticado());
    }

    @Test
    @DisplayName("cerrarSesionAdministrador() cierra al admin y deja intacta la sesión de otros roles")
    void cerrarSesionAdministradorNoTocaOtrosRoles() {
        existeEmpleado("admin", PASSWORD, "Administrador");
        existeEmpleado(USUARIO, PASSWORD, "Cocinero");
        servicio.loginEmpleado("admin", PASSWORD);
        servicio.loginEmpleado(USUARIO, PASSWORD);

        servicio.cerrarSesionAdministrador();

        assertFalse(servicio.isAdminAutenticado());
        assertFalse(servicio.haySesionActiva("admin"));
        assertTrue(servicio.haySesionActiva(USUARIO));
    }

    @Test
    @DisplayName("Tras cerrar la sesión del administrador puede volver a entrar")
    void administradorPuedeVolverAEntrarTrasCerrar() {
        existeEmpleado("admin", PASSWORD, "Administrador");
        servicio.loginEmpleado("admin", PASSWORD);

        servicio.cerrarSesionAdministrador();

        assertEquals("Administrador", servicio.loginEmpleado("admin", PASSWORD));
    }

    @Test
    @DisplayName("La sesión de administrador expirada por inactividad ya no cuenta")
    void sesionDeAdministradorExpiradaNoCuenta() {
        existeEmpleado("admin", PASSWORD, "Administrador");
        servicio.loginEmpleado("admin", PASSWORD);

        reloj.avanzar(ServicioAutenticacion.INACTIVIDAD_MAXIMA.plusSeconds(1));

        assertFalse(servicio.isAdminAutenticado());
    }
}