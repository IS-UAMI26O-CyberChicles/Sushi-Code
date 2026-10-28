package mx.uam.ayd.proyecto.negocio;

import mx.uam.ayd.proyecto.datos.EmpleadoRepository;
import mx.uam.ayd.proyecto.negocio.modelo.Empleado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de autenticación general para empleados (HU-01).
 */
@Service
public class ServicioAutenticacion {

    private final EmpleadoRepository empleadoRepository;

    // Mapas para simular la gestión de sesiones y bloqueos temporales
    private final Map<String, Integer> intentosFallidos = new HashMap<>();
    private final Map<String, Boolean> sesionesActivas = new HashMap<>();

    @Autowired
    public ServicioAutenticacion(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    /**
     * Valida las credenciales, verifica bloqueos, sesiones activas y retorna el rol del empleado.
     * 
     * @param usuario El nombre de usuario ingresado.
     * @param contrasena La contraseña ingresada (en texto claro para propósitos de simulación).
     * @return El rol del usuario (ej. Administrador, Cocinero, Recepcionista, Repartidor) si el login es exitoso.
     * @throws IllegalArgumentException con el mensaje específico si falla la autenticación, hay bloqueo o sesión activa.
     */
    public String loginEmpleado(String usuario, String contrasena) {

        // 1. Verificación de bloqueos
        int intentos = intentosFallidos.getOrDefault(usuario, 0);
        if (intentos >= 5) {
            throw new IllegalArgumentException("Cuenta bloqueada temporalmente por exceder intentos fallidos.");
        }

        // 2. Verificación de sesiones simultáneas
        if (sesionesActivas.getOrDefault(usuario, false)) {
            throw new IllegalArgumentException("La sesión ya está activa en otro dispositivo.");
        }

        // 3. Búsqueda del usuario en la base de datos
        Optional<Empleado> empleadoOpt = empleadoRepository.findByUsuario(usuario);

        // 4. Validación de credenciales
        // OJO: Si la contraseña estuviera hasheada en la BD, aquí se usaría un verificador de hash.
        // Para este sprint (HU-01), validamos con la contraseña almacenada directamente.
        if (empleadoOpt.isPresent() && empleadoOpt.get().getPassword().equals(contrasena)) {
            
            // Login exitoso: Limpiamos intentos fallidos y marcamos la sesión como activa
            intentosFallidos.put(usuario, 0);
            sesionesActivas.put(usuario, true);
            
            // Retornamos el rol para la redirección en la capa de presentación
            return empleadoOpt.get().getRol();
        } else {
            // Login fallido: Incrementamos el contador de intentos
            intentosFallidos.put(usuario, intentos + 1);
            int restantes = 5 - (intentos + 1);
            throw new IllegalArgumentException("Credenciales incorrectas. Te quedan " + restantes + " intentos.");
        }
    }

    /**
     * Libera la sesión de un usuario para que pueda iniciar sesión nuevamente.
     * @param usuario El nombre de usuario.
     */
    public void cerrarSesion(String usuario) {
        sesionesActivas.put(usuario, false);
    }
}
