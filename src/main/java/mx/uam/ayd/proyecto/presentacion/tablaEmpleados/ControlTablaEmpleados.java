package mx.uam.ayd.proyecto.presentacion.tablaEmpleados;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import mx.uam.ayd.proyecto.negocio.ServicioAutenticacion;
import mx.uam.ayd.proyecto.negocio.ServicioEmpleado;
import mx.uam.ayd.proyecto.negocio.modelo.Empleado;
import mx.uam.ayd.proyecto.presentacion.agregarEmpleado.ControlAgregarEmpleado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import java.io.IOException;

import java.util.List;

/**
 * Controlador de la ventana "Gestión de Personal": la tabla con la lista de
 * empleados registrados (HU-03).
 *
 * <p>Esta clase hace tres cosas: (1) pinta la tabla con celdas personalizadas
 * (avatar, badges de colores, etc.), (2) abre el formulario de registro y
 * refresca la tabla cuando se guarda un empleado, y (3) protege el acceso
 * para que solo se pueda abrir si hay una sesión de administrador activa.</p>
 *
 * <h3>Cosas que conviene saber antes de tocar esta clase</h3>
 * <ul>
 *   <li><b>Es un bean singleton de Spring, pero cada vez que se abre la
 *       ventana se vuelve a cargar el FXML.</b> El {@code FXMLLoader} usa
 *       {@code setControllerFactory(springContext::getBean)}, o sea que
 *       reutiliza este mismo objeto, y los campos {@code @FXML} se
 *       reasignan con los controles nuevos de cada carga. Por eso
 *       {@link #initialize()} se ejecuta en cada apertura y llama a
 *       {@link #actualizarTabla()}: así la tabla siempre muestra datos
 *       frescos de la base de datos.</li>
 *   <li><b>Hay dos "defensas" del acceso, y ambas son necesarias.</b> El login
 *       controla el flujo normal (menú, login, tabla), pero
 *       {@link #muestra()} vuelve a verificar la sesión por su cuenta. Es
 *       porque este método es público y otro código (por ejemplo
 *       {@code handleGestionarPersonal} en {@code ControlPrincipal}) lo
 *       puede llamar sin pasar por el login. Ocultar un botón no es
 *       seguridad; revisar en el punto de entrada sí.</li>
 *   <li><b>Los textos de los subtítulos por rol</b> ("Estación Nigiri • Turno
 *       Noche", etc.) son decorativos/de maqueta, no datos reales de la base
 *       de datos. Los dejamos para que la interfaz se vea como el diseño.</li>
 * </ul>
 */
@Component
public class ControlTablaEmpleados {

    @Autowired
    private ServicioEmpleado servicioEmpleado;

    @Autowired
    private ControlAgregarEmpleado controlAgregarEmpleado;

    /**
     * Se usa para saber si hay sesión de administrador y para cerrarla al
     * salir de esta ventana (HU-03, Escenario 0).
     */
    @Autowired
    private ServicioAutenticacion servicioAutenticacion;

    @Autowired
    private ApplicationContext springContext;

    @FXML
    private TableView<Empleado> tablaEmpleados;

    // OJO con los tipos: columnaNombre y columnaAcciones son TableColumn<Empleado, Empleado>
    // (y no <Empleado, String>) porque su celda necesita el empleado completo.
    // La de nombre dibuja avatar + nombre + subtítulo según el rol, y la de acciones
    // necesita saber a cuál empleado pertenece cada botón.
    @FXML
    private TableColumn<Empleado, Empleado> columnaNombre;

    @FXML
    private TableColumn<Empleado, String> columnaUsuario;

    @FXML
    private TableColumn<Empleado, String> columnaRol;

    @FXML
    private TableColumn<Empleado, Empleado> columnaAcciones;

    @FXML
    private Label lblConteoEmpleados;

    @FXML
    private TextField txtBuscar;

    @FXML
    private Button btnRegistrarEmpleado;

    /**
     * Lo llama JavaFX automáticamente justo después de cargar el FXML (y de
     * inyectar los campos {@code @FXML}). Es el lugar correcto para configurar
     * la tabla, porque en el constructor los controles todavía no existen.
     *
     * <p>El botón "Registrar Empleado" se conecta por código con
     * {@code setOnAction} en lugar de usar {@code onAction} en el FXML; el
     * resultado es el mismo, pero aquí podemos pasarle {@code this} al
     * formulario para que luego nos avise cuándo refrescar la tabla.</p>
     */
    @FXML
    public void initialize() {
        configurarColumnaNombre();
        configurarColumnaUsuario();
        configurarColumnaRol();
        configurarColumnaAcciones();

        if (btnRegistrarEmpleado != null) {
            btnRegistrarEmpleado.setOnAction(event -> {
                if (controlAgregarEmpleado != null) {
                    controlAgregarEmpleado.muestra(this);
                }
            });
        }

        actualizarTabla();
    }

    /**
     * Columna "NOMBRE COMPLETO": avatar con iniciales + nombre + subtítulo del rol.
     *
     * <p>Una celda normal solo muestra texto. Como aquí queremos algo más
     * elaborado, usamos un {@code CellFactory} que arma la celda con
     * contenedores ({@code HBox}/{@code VBox}) y labels. El {@code CellValueFactory}
     * entrega el objeto {@code Empleado} completo (no solo el nombre) para
     * poder leer también el rol y colorear el avatar.</p>
     *
     * <p>Se revisa {@code empty || emp == null} porque JavaFX <b>reutiliza</b>
     * las celdas al hacer scroll: una celda que ya mostró un empleado puede
     * volver a usarse vacía, y si no la limpiamos con {@code setGraphic(null)}
     * se quedarían "fantasmas" de datos anteriores.</p>
     */
    private void configurarColumnaNombre() {
        columnaNombre.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        columnaNombre.setCellFactory(column -> new TableCell<Empleado, Empleado>() {
            @Override
            protected void updateItem(Empleado emp, boolean empty) {
                super.updateItem(emp, empty);
                if (empty || emp == null) {
                    setGraphic(null);
                } else {
                    HBox contenedor = new HBox(12);
                    contenedor.setAlignment(Pos.CENTER_LEFT);

                    String iniciales = obtenerIniciales(emp.getNombre());
                    Label lblAvatar = new Label(iniciales);
                    lblAvatar.setStyle(obtenerEstiloAvatar(emp.getRol()));
                    // min y max iguales para que el avatar sea un círculo de tamaño fijo
                    lblAvatar.setMinSize(36, 36);
                    lblAvatar.setMaxSize(36, 36);
                    lblAvatar.setAlignment(Pos.CENTER);

                    VBox textContainer = new VBox(2);
                    Label lblNombre = new Label(emp.getNombre());
                    lblNombre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

                    Label lblSubtitulo = new Label(obtenerSubtituloRol(emp.getRol()));
                    lblSubtitulo.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");

                    textContainer.getChildren().addAll(lblNombre, lblSubtitulo);
                    contenedor.getChildren().addAll(lblAvatar, textContainer);

                    setGraphic(contenedor);
                }
            }
        });
    }

    /**
     * Columna "NOMBRE DE USUARIO": muestra el usuario como una "cápsula" con
     * prefijo {@code @}, estilo red social. Es solo presentación: el valor
     * guardado en la base de datos NO lleva la arroba, se la anteponemos
     * únicamente al dibujar.
     */
    private void configurarColumnaUsuario() {
        columnaUsuario.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getUsuario()));
        columnaUsuario.setCellFactory(column -> new TableCell<Empleado, String>() {
            @Override
            protected void updateItem(String usuario, boolean empty) {
                super.updateItem(usuario, empty);
                if (empty || usuario == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lblUsuarioCapsula = new Label("@" + usuario);
                    lblUsuarioCapsula.setStyle("-fx-text-fill: #e5e7eb; -fx-background-color: #22252b; -fx-background-radius: 8; -fx-padding: 4 10 4 10; -fx-font-size: 12px; -fx-font-weight: bold;");
                    setGraphic(lblUsuarioCapsula);
                    setText(null); // solo usamos el graphic; así no se duplica el texto
                }
            }
        });
    }

    /**
     * Columna "ROL": un badge con un puntito de color y el nombre del rol.
     * Los colores salen de los métodos {@code obtener...Rol}, de modo que cada
     * rol tenga siempre la misma identidad visual en toda la tabla (avatar,
     * badge y texto comparten la misma paleta).
     */
    private void configurarColumnaRol() {
        columnaRol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getRol()));
        columnaRol.setCellFactory(column -> new TableCell<Empleado, String>() {
            @Override
            protected void updateItem(String rol, boolean empty) {
                super.updateItem(rol, empty);
                if (empty || rol == null) {
                    setGraphic(null);
                } else {
                    HBox badge = new HBox(6);
                    badge.setAlignment(Pos.CENTER_LEFT);
                    badge.setStyle(obtenerEstiloBadgeRol(rol));

                    Circle punto = new Circle(3, Color.web(obtenerColorPuntoRol(rol)));
                    Label lblRol = new Label(rol);
                    lblRol.setStyle("-fx-text-fill: " + obtenerColorTextoRol(rol) + "; -fx-font-weight: bold; -fx-font-size: 11px;");

                    badge.getChildren().addAll(punto, lblRol);
                    setGraphic(badge);
                }
            }
        });
    }

    /**
     * Columna "ACCIONES": botones de editar (✏) y desactivar (🚫) por fila.
     *
     * <p><b>Pendiente:</b> por ahora los botones solo son visuales y no tienen
     * acción asignada, porque HU-03 cubre únicamente registro y listado. Se
     * dejaron para que el diseño coincida con la maqueta y para engancharlos
     * cuando se implementen edición y baja de empleados.</p>
     */
    private void configurarColumnaAcciones() {
        columnaAcciones.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        columnaAcciones.setCellFactory(column -> new TableCell<Empleado, Empleado>() {
            @Override
            protected void updateItem(Empleado emp, boolean empty) {
                super.updateItem(emp, empty);
                if (empty || emp == null) {
                    setGraphic(null);
                } else {
                    HBox acciones = new HBox(10);
                    acciones.setAlignment(Pos.CENTER);

                    Button btnEditar = new Button("✏");
                    btnEditar.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-cursor: hand; -fx-font-size: 13px;");

                    Button btnDesactivar = new Button("🚫");
                    btnDesactivar.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-cursor: hand; -fx-font-size: 13px;");

                    acciones.getChildren().addAll(btnEditar, btnDesactivar);
                    setGraphic(acciones);
                }
            }
        });
    }

    /**
     * Vuelve a consultar los empleados a la base de datos y repinta la tabla.
     *
     * <p>Es {@code public} porque lo llama {@code ControlAgregarEmpleado}
     * justo después de guardar un empleado nuevo. Esto es lo que cumple la
     * parte del Escenario 1 que dice que el empleado "aparece inmediatamente
     * en la lista": no esperamos a que el usuario reabra la ventana, sino que
     * el formulario nos avisa y nos refrescamos al instante.</p>
     *
     * <p>El texto del conteo repite {@code lista.size()} en ambos lados
     * ("Mostrando N de N") porque todavía no hay paginación ni filtros
     * funcionando; cuando existan, el primer número será el de los resultados
     * filtrados y el segundo el total.</p>
     */
    public void actualizarTabla() {
        if (servicioEmpleado != null && tablaEmpleados != null) {
            List<Empleado> lista = servicioEmpleado.recuperarEmpleados();
            ObservableList<Empleado> datos = FXCollections.observableArrayList(lista);
            tablaEmpleados.setItems(datos);

            if (lblConteoEmpleados != null) {
                lblConteoEmpleados.setText("Mostrando " + lista.size() + " de " + lista.size() + " empleados");
            }
        }
    }

    /**
     * Calcula las iniciales para el avatar: con dos o más palabras toma la
     * primera letra de las dos primeras ("Juan Pérez López" produce "JP"); con
     * una sola palabra toma sus dos primeras letras. Si el nombre viene vacío
     * o nulo devuelve "EM" (de "empleado") para que el avatar nunca quede
     * vacío.
     */
    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "EM";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
        }
        return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
    }

    /**
     * Texto secundario que aparece bajo el nombre. Es contenido de maqueta (no
     * viene de la base de datos). Sí está protegido contra rol nulo y tiene un
     * valor por defecto para roles desconocidos.
     */
    private String obtenerSubtituloRol(String rol) {
        if (rol == null) return "Asignado • Turno General";
        switch (rol.toLowerCase()) {
            case "cocinero": return "Estación Nigiri • Turno Noche";
            case "administrador": return "Supervisión General • Bodega & Finanzas";
            case "recepcionista": return "Mostrador Principal • Reservas Omakase";
            case "repartidor": return "Rutas VIP & Envíos Refrigerados";
            default: return "Asignado • Turno General";
        }
    }

    /**
     * Estilo CSS del círculo del avatar según el rol (fondo oscuro + letras de
     * un tono más claro del mismo color).
     *
     * <p>Ojo: a diferencia de {@code obtenerSubtituloRol}, este método y los
     * siguientes llaman {@code rol.toLowerCase()} sin revisar {@code null}. En
     * la práctica no truena porque la columna {@code rol} de la entidad es
     * {@code nullable = false}, pero si algún día cambia esa regla habría que
     * agregar la misma protección.</p>
     */
    private String obtenerEstiloAvatar(String rol) {
        String base = "-fx-background-radius: 50%; -fx-font-weight: bold; -fx-font-size: 12px; ";
        switch (rol.toLowerCase()) {
            case "cocinero": return base + "-fx-background-color: #3f2a1d; -fx-text-fill: #f59e0b;";
            case "administrador": return base + "-fx-background-color: #321c42; -fx-text-fill: #c084fc;";
            case "recepcionista": return base + "-fx-background-color: #1a3848; -fx-text-fill: #38bdf8;";
            case "repartidor": return base + "-fx-background-color: #19382c; -fx-text-fill: #4ade80;";
            default: return base + "-fx-background-color: #2a2d34; -fx-text-fill: #e5e7eb;";
        }
    }

    /** Estilo CSS del fondo del badge de rol (la versión oscura del color del rol). */
    private String obtenerEstiloBadgeRol(String rol) {
        String base = "-fx-padding: 4 10 4 10; -fx-background-radius: 12; ";
        switch (rol.toLowerCase()) {
            case "cocinero": return base + "-fx-background-color: #3b2818;";
            case "administrador": return base + "-fx-background-color: #2f193e;";
            case "recepcionista": return base + "-fx-background-color: #143140;";
            case "repartidor": return base + "-fx-background-color: #143226;";
            default: return base + "-fx-background-color: #2a2d34;";
        }
    }

    /** Color "principal" de cada rol (puntito del badge y base del texto). */
    private String obtenerColorPuntoRol(String rol) {
        switch (rol.toLowerCase()) {
            case "cocinero": return "#f59e0b";
            case "administrador": return "#c084fc";
            case "recepcionista": return "#38bdf8";
            case "repartidor": return "#4ade80";
            default: return "#9ca3af";
        }
    }

    /**
     * El texto del badge usa el mismo color que el puntito. Se dejó como
     * método aparte (en vez de reutilizar el anterior directamente) por si
     * después se quiere un tono de texto distinto sin tocar el del puntito.
     */
    private String obtenerColorTextoRol(String rol) {
        return obtenerColorPuntoRol(rol);
    }

    /**
     * Abre la ventana de Gestión de Personal, pero <b>solo si hay una sesión
     * de administrador activa</b> (HU-03, Escenario 0).
     *
     * <p>Si no hay sesión, simplemente no hace nada. No mostramos error porque
     * el flujo normal nunca llega aquí sin login; este chequeo es una red de
     * seguridad por si alguien llama el método directamente (ver la nota en la
     * documentación de la clase).</p>
     *
     * <p>El {@code setOnHidden} registra una acción que se ejecuta cuando la
     * ventana se oculta, sin importar cómo: con la X, con el botón 🚪 de la
     * tarjeta de perfil o por código. Ahí cerramos la sesión de
     * administrador, de modo que para volver a entrar al módulo hay que
     * iniciar sesión otra vez. Se usa {@code setOnHidden} (y no
     * {@code setOnCloseRequest}) porque este último solo se dispara con la X
     * de la ventana, no cuando la cerramos con {@code close()}.</p>
     */
    public void muestra() {
        // Protección: no abrir la tabla sin sesión de administrador
        if (!servicioAutenticacion.isAdminAutenticado()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vista-tabla-empleados.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Personal - Ryuho Sushi");
            stage.setScene(new Scene(root, 1180, 720));
            // Tamaño mínimo para que el contenido siga viéndose bien al reducir la ventana
            stage.setMinWidth(900);
            stage.setMinHeight(560);
            // Al cerrar la ventana (X o botón 🚪) se cierra la sesión de administrador
            stage.setOnHidden(e -> servicioAutenticacion.cerrarSesionAdministrador());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Acción del botón 🚪 de la tarjeta de perfil: cierra la ventana de gestión.
     *
     * <p>No llama a {@code cerrarSesion()} directamente a propósito: al cerrar
     * la ventana se dispara el {@code setOnHidden} que registramos en
     * {@link #muestra()}, y ese ya se encarga de terminar la sesión. Así la
     * sesión se cierra por un solo camino (sin importar si el usuario usó la
     * X o este botón) y evitamos duplicar lógica.</p>
     *
     * <p>La ventana se obtiene a partir de la propia tabla
     * ({@code tablaEmpleados.getScene().getWindow()}) porque este controlador
     * es singleton y no guarda el {@code Stage} en un campo.</p>
     */
    @FXML
    public void actionCerrarSesion() {
        ((Stage) tablaEmpleados.getScene().getWindow()).close();
    }
}