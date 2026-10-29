package mx.uam.ayd.proyecto.negocio;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para el manejo de contraseñas (hash SHA-256).
 *
 * <p>Existe para que {@link ServicioEmpleado} (que guarda la contraseña) y
 * {@link ServicioAutenticacion} (que la compara al iniciar sesión) usen
 * exactamente el mismo algoritmo. Si cada uno tuviera su propia copia, bastaría
 * con cambiar una para que nadie pudiera volver a entrar.</p>
 *
 * <p><b>Mejora futura (backlog):</b> SHA-256 sin sal es suficiente para el
 * alcance del curso, pero en un sistema real se debería usar BCrypt o Argon2.</p>
 */
public final class UtilidadPassword {

    private UtilidadPassword() {
        // Clase de utilidad: no se instancia.
    }

    /**
     * Calcula el hash SHA-256 de una contraseña.
     *
     * @param password Contraseña en texto plano.
     * @return Hash en hexadecimal minúsculas (64 caracteres).
     * @throws IllegalArgumentException si la contraseña es nula.
     */
    public static String encriptar(String password) {
        if (password == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar la contraseña con SHA-256", e);
        }
    }

    /**
     * Verifica si una contraseña en texto plano corresponde al hash almacenado.
     * La comparación es de tiempo constante para no filtrar información.
     *
     * @param plana      Contraseña escrita por el usuario.
     * @param almacenada Hash guardado en la base de datos.
     * @return {@code true} si coinciden; {@code false} si no o si alguno es nulo.
     */
    public static boolean coincide(String plana, String almacenada) {
        if (plana == null || almacenada == null) {
            return false;
        }
        return MessageDigest.isEqual(
                encriptar(plana).getBytes(StandardCharsets.UTF_8),
                almacenada.getBytes(StandardCharsets.UTF_8));
    }
}