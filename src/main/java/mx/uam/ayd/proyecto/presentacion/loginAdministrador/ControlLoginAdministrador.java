package mx.uam.ayd.proyecto.presentacion.loginAdministrador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mx.uam.ayd.proyecto.negocio.ServicioAutenticacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Controlador de la ventana de login exclusivo para administradores
 * (HU-03, Escenario 0: acceso restringido al módulo de personal).
 *
 * <p>Esta clase es la "puerta" del módulo de personal. Antes, el botón
 * "Administrar Personal" abría la tabla de empleados directamente y cualquiera
 * podía entrar. Ahora el menú principal pasa primero por aquí, y solo si el
 * login sale bien se abre la tabla.</p>
 *
 * <h3>Decisiones de diseño (el porqué de las cosas)</h3>
 * <ul>
 *   <li><b>Es un bean de Spring ({@code @Component}) y se carga con
 *       {@code setControllerFactory(springContext::getBean)}.</b> Si no
 *       hiciéramos esto, el {@code FXMLLoader} crearía el controlador por su
 *       cuenta con un constructor vacío y Spring nunca le inyectaría el
 *       {@link ServicioAutenticacion}; el campo quedaría en {@code null} y
 *       tendríamos un {@code NullPointerException} al presionar "Ingresar".
 *       Con la factory le decimos al loader: "no lo instancies tú, pídeselo a
 *       Spring".</li>
 *   <li><b>Es singleton</b> (el comportamiento por defecto de Spring), así que
 *       el mismo objeto se reutiliza cada vez que se abre la ventana. Por eso
 *       {@link #muestra()} reinicia la bandera {@code exito}: si no, un login
 *       exitoso anterior haría que la siguiente apertura "ya contara" como
 *       exitosa aunque el usuario cancele.</li>
 *   <li><b>La validación de credenciales NO está aquí</b>, está en
 *       {@link ServicioAutenticacion}. Este controlador solo maneja la
 *       pantalla (leer campos, mostrar mensajes, cerrar la ventana). Así
 *       separamos presentación de negocio, y además podemos probar la lógica
 *       con pruebas unitarias sin levantar JavaFX.</li>
 * </ul>
 *
 * <p><b>Limitación conocida:</b> no hay límite de intentos fallidos, así que
 * en teoría se podría probar contraseñas indefinidamente. Para el alcance del
 * curso es aceptable, pero en un sistema real habría que bloquear la cuenta
 * temporalmente tras varios intentos.</p>
 */
@Component
public class ControlLoginAdministrador {

    @Autowired
    private ServicioAutenticacion servicioAutenticacion;

    @Autowired
    private ApplicationContext springContext;

    // Los campos con @FXML los llena el FXMLLoader a partir de los fx:id del archivo
    // vista-login-administrador.fxml. Si el nombre aquí no coincide con el fx:id, queda null.
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblMensaje;

    private Stage stage;

    /** Resultado del último intento de login; lo lee {@link #muestra()} al cerrarse la ventana. */
    private boolean exito;

    /**
     * Abre el login como ventana modal y se queda esperando hasta que se cierre.
     *
     * <p>Usamos {@code showAndWait()} (en lugar de {@code show()}) a propósito:
     * detiene la ejecución en este punto hasta que la ventana se cierra, así
     * podemos <i>devolver</i> el resultado de forma directa y quien nos llame
     * (el {@code ControlPrincipal}) puede escribir simplemente
     * {@code if (login.muestra()) { abrirTabla(); }}. La alternativa sería
     * pasar callbacks, que es más enredado para este caso.</p>
     *
     * <p>La ventana es {@code APPLICATION_MODAL}: mientras esté abierta no se
     * puede interactuar con el menú principal, lo que evita abrir dos logins a
     * la vez.</p>
     *
     * <p>Si el FXML no se puede cargar, devolvemos {@code false}: ante un error
     * el sistema "falla cerrado" (niega el acceso) en lugar de dejar pasar a
     * alguien sin autenticar.</p>
     *
     * @return {@code true} si el administrador inició sesión correctamente;
     *         {@code false} si canceló, cerró la ventana o hubo un error.
     */
    public boolean muestra() {
        exito = false; // singleton: limpiamos el resultado de la vez anterior
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vista-login-administrador.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            stage = new Stage();
            stage.setTitle("Acceso de Administrador - Ryuho Sushi");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exito;
    }

    /**
     * Se ejecuta al presionar "Ingresar" (enlazado con {@code onAction} en el FXML).
     *
     * <p>Detalles que vale la pena explicar:</p>
     * <ul>
     *   <li><b>Primero revisamos campos vacíos</b> antes de llamar al servicio.
     *       Así damos un mensaje más útil ("ingresa tu usuario y contraseña")
     *       y nos ahorramos una consulta a la base de datos que de todos
     *       modos fallaría.</li>
     *   <li><b>Al usuario le hacemos {@code trim()}, a la contraseña no.</b> Es
     *       muy común que al copiar y pegar un usuario se cuele un espacio al
     *       final; pero en una contraseña un espacio puede ser parte legítima
     *       de ella, y recortarla cambiaría lo que el usuario escribió.</li>
     *   <li><b>El mensaje de error es genérico a propósito.</b> No distingue
     *       entre "el usuario no existe", "contraseña incorrecta" o "existe
     *       pero no es administrador". Si lo distinguiera, alguien podría ir
     *       probando nombres para descubrir cuáles usuarios existen
     *       (enumeración de usuarios).</li>
     *   <li><b>Se limpia la contraseña tras un fallo</b> para que no quede
     *       visible/almacenada en el campo y obligar a reescribirla.</li>
     * </ul>
     */
    @FXML
    public void actionIngresar() {
        lblMensaje.setText("");
        String usuario = txtUsuario.getText();
        String contrasena = txtContrasena.getText();

        // 1. Validación de campos vacíos
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isEmpty()) {
            lblMensaje.setText("Ingresa tu usuario y contraseña.");
            return;
        }

        try {
            // 2. Llamamos al nuevo método general de autenticación (que programaremos en el siguiente paso)
            // Este método lanzará excepciones si la cuenta está bloqueada o ya tiene sesión activa.
            String rol = servicioAutenticacion.loginEmpleado(usuario.trim(), contrasena);
            
            exito = true;
            
            // Aquí delegaremos la redirección a la Subtarea 3 basándonos en la variable "rol"
            System.out.println("Acceso concedido. Redirigiendo a panel de: " + rol);
            
            stage.close(); 
            
        } catch (IllegalArgumentException e) {
            // 3. Capturamos los errores de negocio: 
            // "Credenciales incorrectas", "Cuenta bloqueada temporalmente", "Sesión ya iniciada"
            lblMensaje.setText(e.getMessage());
            txtContrasena.clear();
        }
    }


    /**
     * Se ejecuta al presionar "Cancelar". Solo cierra la ventana; como
     * {@code exito} sigue en {@code false}, {@link #muestra()} devolverá
     * {@code false} y la tabla de empleados no se abrirá.
     *
     * <p>El chequeo de {@code null} es defensivo: evita un error si por alguna
     * razón el método se invoca antes de que exista el {@code Stage}.</p>
     */
    @FXML
    public void actionCancelar() {
        if (stage != null) {
            stage.close();
        }
    }
}