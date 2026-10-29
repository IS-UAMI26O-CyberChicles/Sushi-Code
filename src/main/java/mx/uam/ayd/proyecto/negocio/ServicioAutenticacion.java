package mx.uam.ayd.proyecto.negocio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.uam.ayd.proyecto.datos.EmpleadoRepository;
import mx.uam.ayd.proyecto.negocio.modelo.Empleado;

/**
 * Servicio de autenticación general para empleados (HU-01).
 *
 * <p>Reglas de negocio que implementa:</p>
 * <ol>
 *   <li><b>Formato de usuario (Escenario 4):</b> solo letras, números y guion
 *       bajo, de 3 a 20 caracteres. Un formato inválido se rechaza antes de
 *       consultar la base de datos y <i>no</i> cuenta como intento fallido.</li>
 *   <li><b>Bloqueo (Escenario 2):</b> tras {@value #MAX_INTENTOS} intentos
 *       fallidos consecutivos la cuenta se bloquea {@code DURACION_BLOQUEO}
 *       (15 minutos). Al vencer el plazo el contador se reinicia.</li>
 *   <li><b>Sesión única (Escenario 3):</b> un usuario con sesión activa no puede
 *       iniciar otra. La sesión se libera con {@link #cerrarSesion(String)} o
 *       por inactividad ({@code INACTIVIDAD_MAXIMA}, 10 minutos sin llamar a
 *       {@link #registrarActividad(String)}).</li>
 *   <li><b>Credenciales (Escenario 1):</b> se compara el hash SHA-256 de la
 *       contraseña (ver {@link UtilidadPassword}) y se devuelve el rol.</li>
 * </ol>
 *
 * <p><b>Decisiones de diseño:</b></p>
 * <ul>
 *   <li>El estado de bloqueos y sesiones vive en memoria. La base H2 también es
 *       en memoria, así que al reiniciar la aplicación todo parte limpio de
 *       cualquier manera.</li>
 *   <li>Se valida la contraseña <b>antes</b> de revelar que la sesión ya está
 *       activa; así un desconocido no puede averiguar quién está conectado.</li>
 *   <li>Usuario inexistente y contraseña incorrecta producen el mismo mensaje
 *       (evita la enumeración de usuarios).</li>
 *   <li>El {@link Clock} se inyecta para poder simular el paso del tiempo en
 *       las pruebas unitarias sin esperar minutos reales.</li>
 * </ul>
 */
@Service
public class ServicioAutenticacion {

    /** Rol con acceso al módulo de personal. */
    public static final String ROL_ADMIN = "Administrador";
    /** Rol que se redirige a la pantalla de cocina. */
    public static final String ROL_COCINERO = "Cocinero";
    /** Rol que se redirige a la pantalla de "Tipo de servicio". */
    public static final String ROL_RECEPCIONISTA = "Recepcionista";
    /** Rol que se redirige a la pantalla de repartidor. */
    public static final String ROL_REPARTIDOR = "Repartidor";

    /** Intentos fallidos consecutivos permitidos antes de bloquear. */
    public static final int MAX_INTENTOS = 5;
    /** Tiempo que dura el bloqueo temporal de una cuenta. */
    public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);
    /** Tiempo sin actividad después del cual una sesión se considera abandonada. */
    public static final Duration INACTIVIDAD_MAXIMA = Duration.ofMinutes(10);

    /** Letras, números y guion bajo; de 3 a 20 caracteres. */
    private static final Pattern FORMATO_USUARIO = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private final EmpleadoRepository empleadoRepository;
    private final Clock reloj;

    /** Estado de intentos fallidos y bloqueo por usuario. */
    private final Map<String, EstadoAcceso> estados = new HashMap<>();
    /** Última actividad de cada sesión abierta, por usuario. */
    private final Map<String, Instant> sesiones = new HashMap<>();
    /** Rol con el que inició sesión cada usuario (solo mientras su sesión está abierta). */
    private final Map<String, String> rolesEnSesion = new HashMap<>();

    /** Intentos fallidos y fecha de fin de bloqueo de un usuario. */
    private static final class EstadoAcceso {
        private int intentosFallidos;
        private Instant bloqueadoHasta;

        private void reiniciar() {
            intentosFallidos = 0;
            bloqueadoHasta = null;
        }
    }

    /**
     * Constructor usado por Spring: toma el reloj real del sistema.
     *
     * @param empleadoRepository Repositorio de empleados.
     */
    @Autowired
    public ServicioAutenticacion(EmpleadoRepository empleadoRepository) {
        this(empleadoRepository, Clock.systemDefaultZone());
    }

    /**
     * Constructor para pruebas: permite inyectar un reloj controlable.
     *
     * @param empleadoRepository Repositorio de empleados.
     * @param reloj              Reloj que determina "la hora actual".
     */
    ServicioAutenticacion(EmpleadoRepository empleadoRepository, Clock reloj) {
        this.empleadoRepository = empleadoRepository;
        this.reloj = reloj;
    }

    /**
     * Inicia sesión: valida formato, bloqueo, credenciales y sesión única.
     *
     * @param usuario    Nombre de usuario ingresado.
     * @param contrasena Contraseña ingresada en texto plano.
     * @return El rol del empleado (ej. Cocinero, Recepcionista, Repartidor).
     * @throws IllegalArgumentException con un mensaje apto para mostrar al usuario
     *         si el formato es inválido, la cuenta está bloqueada, las credenciales
     *         son incorrectas o la sesión ya está activa.
     */
    public synchronized String loginEmpleado(String usuario, String contrasena) {

        // 1. Formato del usuario (Escenario 4). No consume intentos ni toca la BD.
        if (!esUsuarioValido(usuario)) {
            throw new IllegalArgumentException("El usuario no es válido.");
        }
        if (contrasena == null || contrasena.isEmpty()) {
            throw new IllegalArgumentException("Ingresa tu usuario y contraseña.");
        }

        Instant ahora = reloj.instant();
        EstadoAcceso estado = estados.computeIfAbsent(usuario, u -> new EstadoAcceso());

        // 2. Bloqueo temporal (Escenario 2). Si ya venció, se reinicia el contador.
        if (estado.bloqueadoHasta != null) {
            if (ahora.isBefore(estado.bloqueadoHasta)) {
                throw new IllegalArgumentException(mensajeBloqueo(ahora, estado.bloqueadoHasta));
            }
            estado.reiniciar();
        }

        // 3. Credenciales (Escenario 1). Mismo resultado para "no existe" y "clave mala".
        Optional<Empleado> empleadoOpt = empleadoRepository.findByUsuario(usuario);
        boolean valido = empleadoOpt.isPresent()
                && UtilidadPassword.coincide(contrasena, empleadoOpt.get().getPassword());

        if (!valido) {
            estado.intentosFallidos++;
            if (estado.intentosFallidos >= MAX_INTENTOS) {
                estado.bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
                throw new IllegalArgumentException(mensajeBloqueo(ahora, estado.bloqueadoHasta));
            }
            int restantes = MAX_INTENTOS - estado.intentosFallidos;
            throw new IllegalArgumentException("Credenciales incorrectas. Te quedan "
                    + restantes + (restantes == 1 ? " intento." : " intentos."));
        }

        // Credenciales correctas: el contador de fallos se limpia.
        estado.reiniciar();

        // 4. Sesión única (Escenario 3). Se revisa hasta después de validar la contraseña.
        if (haySesionActiva(usuario, ahora)) {
            throw new IllegalArgumentException(
                    "La sesión ya está activa en otro dispositivo.");
        }

        sesiones.put(usuario, ahora);
        rolesEnSesion.put(usuario, empleadoOpt.get().getRol());
        return empleadoOpt.get().getRol();
    }

    /**
     * Libera la sesión de un usuario para que pueda iniciar sesión de nuevo.
     *
     * @param usuario Nombre de usuario.
     */
    public synchronized void cerrarSesion(String usuario) {
        sesiones.remove(usuario);
        rolesEnSesion.remove(usuario);
    }

    /**
     * Indica si hay un administrador con sesión vigente. Lo usa el módulo de
     * personal como red de seguridad antes de abrirse.
     *
     * @return {@code true} si algún usuario con rol Administrador tiene sesión activa.
     */
    public synchronized boolean isAdminAutenticado() {
        Instant ahora = reloj.instant();
        return sesiones.keySet().stream().anyMatch(
                u -> ROL_ADMIN.equalsIgnoreCase(rolesEnSesion.get(u)) && haySesionActiva(u, ahora));
    }

    /**
     * Cierra las sesiones de administrador. Se llama cuando se cierra el módulo
     * de personal. Las sesiones de otros roles no se tocan.
     *
     * <p>Limitación conocida: si hubiera dos administradores con sesión abierta
     * a la vez se cerrarían ambas. En este flujo el login de administrador es
     * modal, así que en la práctica solo hay uno.</p>
     */
    public synchronized void cerrarSesionAdministrador() {
        sesiones.keySet().removeIf(u -> ROL_ADMIN.equalsIgnoreCase(rolesEnSesion.get(u)));
        rolesEnSesion.keySet().removeIf(u -> !sesiones.containsKey(u));
    }

    /**
     * Marca actividad del usuario para que su sesión no expire por inactividad.
     * Las pantallas deben llamarlo al navegar o interactuar. Si el usuario no
     * tiene sesión abierta, no hace nada.
     *
     * @param usuario Nombre de usuario.
     */
    public synchronized void registrarActividad(String usuario) {
        if (sesiones.containsKey(usuario)) {
            sesiones.put(usuario, reloj.instant());
        }
    }

    /**
     * Indica si el usuario tiene una sesión vigente (no cerrada ni expirada).
     *
     * @param usuario Nombre de usuario.
     * @return {@code true} si la sesión sigue activa.
     */
    public synchronized boolean haySesionActiva(String usuario) {
        return haySesionActiva(usuario, reloj.instant());
    }

    /**
     * Valida el formato de un nombre de usuario.
     *
     * @param usuario Texto a evaluar.
     * @return {@code true} si solo tiene letras, números o guion bajo (3 a 20).
     */
    public boolean esUsuarioValido(String usuario) {
        return usuario != null && FORMATO_USUARIO.matcher(usuario).matches();
    }

    private boolean haySesionActiva(String usuario, Instant ahora) {
        Instant ultimaActividad = sesiones.get(usuario);
        return ultimaActividad != null
                && ahora.isBefore(ultimaActividad.plus(INACTIVIDAD_MAXIMA));
    }

    private String mensajeBloqueo(Instant ahora, Instant hasta) {
        long segundos = Math.max(1, Duration.between(ahora, hasta).getSeconds());
        long minutos = (segundos + 59) / 60; // se redondea hacia arriba
        return "Cuenta bloqueada temporalmente por exceder el límite de intentos fallidos. "
                + "Intenta de nuevo en " + minutos + (minutos == 1 ? " minuto." : " minutos.");
    }
}