package mx.uam.ayd.proyecto.negocio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.uam.ayd.proyecto.datos.EmpleadoRepository;
import mx.uam.ayd.proyecto.negocio.modelo.Empleado;

/**
 * Servicio encargado de la logica de negocio para la gestion de empleados.
 * Incluye validaciones de seguridad, control de usuarios duplicados y encriptacion.
 * 
 * Cumple con DoD #5 (Documentacion de codigo).
 */
@Service
public class ServicioEmpleado {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    // Expresion regular: minimo 8 caracteres, 1 mayuscula, 1 minuscula, 1 numero y 1 caracter especial
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

    /**
     * Registra un nuevo empleado en el sistema tras validar las reglas de negocio.
     * 
     * @param nombre   Nombre completo del empleado.
     * @param usuario  Nombre de usuario unico.
     * @param password Contrasenia que debe cumplir con las politicas de seguridad.
     * @param rol      Rol asignado al empleado.
     * @return El {@link Empleado} registrado y guardado en la base de datos.
     * @throws IllegalArgumentException si los datos son invalidos, el usuario ya existe o la contrasenia es debil.
     */
    public Empleado registrarEmpleado(String nombre, String usuario, String password, String rol) {
        
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del empleado no puede estar vacio.");
        }
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacio.");
        }
        if (rol == null || rol.trim().isEmpty()) {
            throw new IllegalArgumentException("El rol del empleado no puede estar vacio.");
        }

        // Regla 1: Validar si el usuario ya existe
        if (empleadoRepository.existsByUsuario(usuario)) {
            throw new IllegalArgumentException("El nombre de usuario '" + usuario + "' ya se encuentra registrado.");
        }

        // Regla 2: Validar contrasenia segura
        if (!validarPasswordSegura(password)) {
            throw new IllegalArgumentException(
                "La contrasenia no cumple con las politicas de seguridad: " +
                "debe tener al menos 8 caracteres, una letra mayuscula, una minuscula, un numero y un caracter especial."
            );
        }

        // Regla 3: Encriptar la contrasenia
        String passwordEncriptada = encriptarPassword(password);

        Empleado nuevoEmpleado = new Empleado(nombre, usuario, passwordEncriptada, rol);
        return empleadoRepository.save(nuevoEmpleado);
    }

    /**
     * Recupera todos los empleados registrados.
     * 
     * @return Lista de empleados.
     */
    public List<Empleado> recuperarEmpleados() {
        return empleadoRepository.findAll();
    }

    /**
     * Valida si una contrasenia cumple con la politica de complejidad requerida.
     * 
     * @param password Contrasenia a evaluar.
     * @return true si cumple con todos los criterios de seguridad, false en caso contrario.
     */
    public boolean validarPasswordSegura(String password) {
        if (password == null) {
            return false;
        }
        return password.matches(PASSWORD_PATTERN);
    }

    /**
     * Aplica el algoritmo de hashing SHA-256 a la contrasenia proporcionada.
     * 
     * @param password Contrasenia en texto plano.
     * @return Representacion hexadecimal de la contrasenia encriptada.
     */
    private String encriptarPassword(String password) {
        return UtilidadPassword.encriptar(password);
    }
}