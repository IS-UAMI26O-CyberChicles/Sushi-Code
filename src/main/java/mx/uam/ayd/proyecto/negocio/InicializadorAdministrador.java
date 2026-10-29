package mx.uam.ayd.proyecto.negocio;

import mx.uam.ayd.proyecto.datos.EmpleadoRepository;
import mx.uam.ayd.proyecto.negocio.modelo.Empleado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial cuando la aplicación termina de arrancar.
 * La BD H2 vive en memoria y se vacía en cada ejecución, así que sin esto
 * no habría ningún administrador para entrar al módulo de personal.
 *
 * <p>La contraseña se guarda con hash (igual que al registrar un empleado desde
 * la interfaz), porque {@link ServicioAutenticacion} compara contra el hash.</p>
 */
@Component
public class InicializadorAdministrador implements ApplicationRunner {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (!empleadoRepository.existsByUsuario("admin")) {
            empleadoRepository.save(new Empleado(
                "Administrador General", "admin",
                UtilidadPassword.encriptar("Admin123!"), "Administrador"));
        }
    }
}