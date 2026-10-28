package mx.uam.ayd.proyecto.presentacion.armadoOrden;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import mx.uam.ayd.proyecto.datos.PlatilloRepository;
import mx.uam.ayd.proyecto.negocio.ServicioOrden;
import mx.uam.ayd.proyecto.negocio.ServicioPedido;
import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;
import mx.uam.ayd.proyecto.negocio.modelo.Platillo;
import mx.uam.ayd.proyecto.presentacion.enviarOrdenCocina.ControlEnviarOrdenCocina;

// Controlador principal para el armado de órdenes, gestión del menú, 
// aplicación de filtros de categorías y control interactivo de notas por platillo (HU-06).
@Controller
public class ControlArmadoOrden {

    // Componentes FXML (Inyección de elementos gráficos de la vista)
    @FXML private TextField txtBusqueda;
    @FXML private FlowPane flowPanePlatillos;
    @FXML private Label lblMesaTicket;
    @FXML private ListView<VBox> listResumenOrden; 
    @FXML private Label lblSubtotal;
    @FXML private Button btnConfirmarOrden;

    // Inyección de dependencias (Capas de datos y servicios de negocio)
    @Autowired
    private PlatilloRepository platilloRepository;

    @Autowired
    private ServicioOrden servicioOrden;

    @Autowired
    private ServicioPedido servicioPedido;

    @Autowired
    private ControlEnviarOrdenCocina controlEnviarOrdenCocina;

    // Pedido activo actual que se está configurando
    private Pedido pedidoActual;

    // Método de inicialización de la pantalla
    public void inicia(Pedido pedido) {
        this.pedidoActual = pedido;
        
        // Configura el número de ticket visual en la interfaz
        int numTicket = pedidoActual.getNumeroOrden() > 0 ? pedidoActual.getNumeroOrden() : 1;
        lblMesaTicket.setText("TICKET #" + numTicket);
        
        lblSubtotal.setText("$0.00");
        
        flowPanePlatillos.getChildren().clear();
        listResumenOrden.getItems().clear();
        
        // Listener para la barra de búsqueda en tiempo real
        txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarPlatillo(newValue);
        });
        
        // Carga inicial del menú completo
        cargarPlatillosMenu(null);
        actualizarVistaCarrito();
    }
    
    // Carga y filtrado de platillos del menú
    private void cargarPlatillosMenu(String categoria) {
        flowPanePlatillos.getChildren().clear();
        List<Platillo> catalogo;
        
        // Si la categoría es nula, muestra todo el catálogo; si no, filtra por área (ej. "Postres", "Rollos especiales")
        if (categoria == null) {
            catalogo = (List<Platillo>) platilloRepository.findAll();
        } else {
            catalogo = platilloRepository.findByTipoArea(categoria);
        }
        
        // Genera y añade las tarjetas visuales de cada platillo al contenedor
        for (Platillo platillo : catalogo) {
            VBox tarjeta = crearTarjetaPlatillo(platillo);
            flowPanePlatillos.getChildren().add(tarjeta);
        }
    }

    // Creación dinámica de tarjetas de platillos con botón de notas (HU-06)
    private VBox crearTarjetaPlatillo(Platillo platillo) {
        VBox tarjeta = new VBox(8);
        tarjeta.setPrefSize(160, 170);
        tarjeta.setPadding(new Insets(12));
        
        // Estilos visuales por defecto y al pasar el cursor (Hover)
        String estiloNormal = "-fx-background-color: #2b1c1c; -fx-background-radius: 12; -fx-border-color: #4a3333; -fx-border-radius: 12; -fx-border-width: 1;";
        String estiloHover = "-fx-background-color: #382424; -fx-background-radius: 12; -fx-border-color: #d62828; -fx-border-radius: 12; -fx-border-width: 1;";
        
        tarjeta.setStyle(estiloNormal);
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(estiloHover));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle(estiloNormal));

        // Etiqueta de precio ubicada en la esquina superior derecha
        Label lblPrecio = new Label("$" + platillo.getPrecio());
        lblPrecio.setTextFill(Color.web("#e8b1b1"));
        lblPrecio.setFont(Font.font("System", FontWeight.BOLD, 13));
        
        HBox filaSuperior = new HBox(lblPrecio);
        filaSuperior.setAlignment(Pos.TOP_RIGHT);
        
        // Nombre descriptivo del platillo
        Label lblNombre = new Label(platillo.getNombre());
        lblNombre.setTextFill(Color.WHITE);
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblNombre.setWrapText(true);
        lblNombre.setMinHeight(Region.USE_PREF_SIZE);

        Region espaciador = new Region();
        VBox.setVgrow(espaciador, Priority.ALWAYS);

        // Botón tradicional para agregar rápido
        Button btnAgregar = new Button("Agregar");
        btnAgregar.setStyle("-fx-background-color: #4a2222; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
        btnAgregar.setOnAction(e -> agregarPlatillo(String.valueOf(platillo.getIdPlatillo())));

        // Botón interactivo "Nota" requerido por la HU-06 para adjuntar especificaciones previas
        Button btnNota = new Button("Nota");
        btnNota.setStyle("-fx-background-color: #3a2c2c; -fx-text-fill: #e8b1b1; -fx-font-size: 11px; -fx-cursor: hand;");
        
        btnNota.setOnAction(e -> {
            // Ventana emergente interactiva para capturar especificaciones (alergias, dedicatorias, etc.)
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Especificaciones del Platillo");
            dialog.setHeaderText("Agregar nota para: " + platillo.getNombre());
            dialog.setContentText("Nota (máx. 50 caracteres):");

            // Configuración del límite estricto de 50 caracteres para evitar saturación en cocina
            dialog.getEditor().setTextFormatter(new TextFormatter<String>(change -> {
                if (change.getControlNewText().length() <= 50) {
                    return change;
                }
                return null;
            }));

            dialog.showAndWait().ifPresent(nota -> {
                agregarPlatilloConNota(platillo.getIdPlatillo(), nota);
            });
        });

        HBox filaBotones = new HBox(5, btnAgregar, btnNota);
        filaBotones.setAlignment(Pos.CENTER_RIGHT);

        tarjeta.getChildren().addAll(filaSuperior, lblNombre, espaciador, filaBotones);

        return tarjeta;
    }

    // Método auxiliar: Procesar platillo con su respectiva nota inicial
    public void agregarPlatilloConNota(long idPlatillo, String nota) {
        servicioOrden.procesarNuevoPlatillo(idPlatillo, pedidoActual.getIdPedido());
        
        List<DetallesPedido> detalles = servicioOrden.obtenerDetallesDePedido(pedidoActual.getIdPedido());
        if (!detalles.isEmpty()) {
            // Localiza el detalle recién agregado para asignarle la nota capturada inmediatamente
            for (int i = detalles.size() - 1; i >= 0; i--) {
                DetallesPedido det = detalles.get(i);
                if (det.getPlatillo().getIdPlatillo() == idPlatillo && (det.getNotas() == null || det.getNotas().isEmpty())) {
                    servicioOrden.agregarNota(det.getIdDetallePedido(), nota);
                    break;
                }
            }
        }
        actualizarVistaCarrito();
    }

    // Construcción de celdas para el resumen del ticket y visualización clara
    private VBox crearCeldaDetalleCarrito(String idDetalleStr, String nombrePlatillo, int cantidad, double precioUnitario, String notaGuardada) {
        VBox celda = new VBox(6);
        celda.setPadding(new Insets(10));
        celda.setStyle("-fx-border-color: #4a3333; -fx-border-width: 0 0 1 0; -fx-background-color: #241414;");

        Label lblNombre = new Label(cantidad + "x " + nombrePlatillo);
        lblNombre.setTextFill(Color.WHITE);
        lblNombre.setFont(Font.font("System", FontWeight.BOLD, 14));

        Region espacio1 = new Region();
        HBox.setHgrow(espacio1, Priority.ALWAYS);

        Label lblSubtotalLocal = new Label("$" + (precioUnitario * cantidad));
        lblSubtotalLocal.setTextFill(Color.web("#e8b1b1"));
        lblSubtotalLocal.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox filaSuperior = new HBox(lblNombre, espacio1, lblSubtotalLocal);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        // Contenedor y etiqueta estilizada para visualizar claramente la nota ingresada en el ticket
        VBox contenedorNota = new VBox(2);
        if (notaGuardada != null && !notaGuardada.trim().isEmpty()) {
            Label lblNotaVisual = new Label("Nota: " + notaGuardada);
            lblNotaVisual.setTextFill(Color.web("#ffb703")); 
            lblNotaVisual.setFont(Font.font("System", FontPosture.ITALIC, 12));
            lblNotaVisual.setWrapText(true);
            contenedorNota.getChildren().add(lblNotaVisual);
        }

        // Controles de decremento de cantidad (-)
        Button btnMenos = new Button("-");
        btnMenos.setPrefWidth(28);
        btnMenos.setStyle("-fx-background-color: #382424; -fx-text-fill: white; -fx-cursor: hand;");
        btnMenos.setOnAction(e -> modificarCantidad(idDetalleStr, -1));

        Label lblCantidad = new Label(String.valueOf(cantidad));
        lblCantidad.setTextFill(Color.WHITE);
        lblCantidad.setPrefWidth(20);
        lblCantidad.setAlignment(Pos.CENTER);

        // Controles de incremento de cantidad (+)
        Button btnMas = new Button("+");
        btnMas.setPrefWidth(28);
        btnMas.setStyle("-fx-background-color: #382424; -fx-text-fill: white; -fx-cursor: hand;");
        btnMas.setOnAction(e -> modificarCantidad(idDetalleStr, 1));

        // Campo de texto interactivo en el resumen para editar o agregar notas directamente
        TextField txtNota = new TextField();
        txtNota.setPromptText("Nota (máx. 50 car)");
        txtNota.setPrefWidth(130);
        txtNota.setStyle("-fx-background-color: #2b1c1c; -fx-text-fill: white; -fx-prompt-text-fill: #8c7a7a; -fx-font-size: 11px;");
        txtNota.setText(notaGuardada);

        // Restricción estricta de longitud máxima de 50 caracteres en el campo de texto del ticket
        txtNota.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() <= 50) {
                return change;
            }
            return null;
        }));

        txtNota.setOnAction(e -> agregarNota(idDetalleStr, txtNota.getText()));

        Region espacio2 = new Region();
        HBox.setHgrow(espacio2, Priority.ALWAYS);

        HBox filaInferior = new HBox(5, btnMenos, lblCantidad, btnMas, espacio2, txtNota);
        filaInferior.setAlignment(Pos.CENTER_LEFT);

        celda.getChildren().addAll(filaSuperior, contenedorNota, filaInferior);

        return celda;
    }

    // Actualización del estado del carrito y cálculo de totales
    private void actualizarVistaCarrito() {
        listResumenOrden.getItems().clear();
        List<DetallesPedido> detalles = servicioOrden.obtenerDetallesDePedido(pedidoActual.getIdPedido());
        
        for(DetallesPedido detalle : detalles) {
            String notaGuardada = detalle.getNotas() == null ? "" : detalle.getNotas();
            
            VBox celda = crearCeldaDetalleCarrito(
                String.valueOf(detalle.getIdDetallePedido()), 
                detalle.getPlatillo().getNombre(), 
                detalle.getCantidad(), 
                detalle.getPlatillo().getPrecio(),
                notaGuardada
            );
            listResumenOrden.getItems().add(celda);
        }
        
        double total = servicioOrden.calcularSubtotalTotal(pedidoActual.getIdPedido());
        lblSubtotal.setText("$" + total);
        servicioPedido.asignarDetallesAPedido(pedidoActual, detalles); 
    }

    // Métodos de acción generales (Agregar, Modificar Cantidad, Agregar Notas)
    public void agregarPlatillo(String idPlatilloStr) {
        long idPlatillo = Long.parseLong(idPlatilloStr);
        servicioOrden.procesarNuevoPlatillo(idPlatillo, pedidoActual.getIdPedido());
        actualizarVistaCarrito();
    }

    public void modificarCantidad(String idDetalleStr, int operacion) {
        long idDetalle = Long.parseLong(idDetalleStr);
        servicioOrden.procesarCambioCantidad(idDetalle, operacion);
        actualizarVistaCarrito();
    }

    public void agregarNota(String idDetalleStr, String nota) {
        long idDetalle = Long.parseLong(idDetalleStr);
        servicioOrden.agregarNota(idDetalle, nota);
        actualizarVistaCarrito();
    }

    // Métodos de filtrado por categorías FXML
    @FXML
    public void mostrarTodoElMenu() {
        cargarPlatillosMenu(null); 
    }

    @FXML
    public void filtrarRollos() {
        cargarPlatillosMenu("Rollos");
    }

    @FXML
    public void filtrarRollosEspeciales() {
        cargarPlatillosMenu("Rollos especiales");
    }

    @FXML
    public void filtrarPlancha() {
        cargarPlatillosMenu("Plancha");
    }

    @FXML
    public void filtrarBebidas() {
        cargarPlatillosMenu("Bebidas");
    }

    @FXML
    public void filtrarPostres() {
        cargarPlatillosMenu("Postres");
    }

    // Búsqueda dinámica de platillos (Por nombre o precio)
    private void buscarPlatillo(String textoBuscado) {
        flowPanePlatillos.getChildren().clear();
        List<Platillo> resultados;

        if (textoBuscado == null || textoBuscado.trim().isEmpty()) {
            resultados = (List<Platillo>) platilloRepository.findAll();
        } else {
            try {
                int precioBuscado = Integer.parseInt(textoBuscado.trim());
                resultados = platilloRepository.findByPrecio(precioBuscado);
            } catch (NumberFormatException e) {
                resultados = platilloRepository.findByNombreContainingIgnoreCase(textoBuscado.trim());
            }
        }

        for (Platillo platillo : resultados) {
            VBox tarjeta = crearTarjetaPlatillo(platillo);
            flowPanePlatillos.getChildren().add(tarjeta);
        }
    }

    // Confirmación y pase de orden hacia cocina
    @FXML
    public void confirmarOrdenAction() {
        if (pedidoActual != null) {
            try {
                controlEnviarOrdenCocina.inicia(pedidoActual);
            } catch (Exception e) {
                System.err.println("Error al preparar la orden y avanzar el ticket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void initialize() {}
}