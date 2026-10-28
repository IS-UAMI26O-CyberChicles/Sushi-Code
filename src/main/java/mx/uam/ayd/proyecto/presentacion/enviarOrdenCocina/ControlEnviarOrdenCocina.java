package mx.uam.ayd.proyecto.presentacion.enviarOrdenCocina;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import mx.uam.ayd.proyecto.negocio.ServicioOrden;
import mx.uam.ayd.proyecto.negocio.ServicioPedido;
import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;
import mx.uam.ayd.proyecto.presentacion.armadoOrden.VistaArmadoOrden;
import mx.uam.ayd.proyecto.presentacion.cancelarOrden.ControlCancelarOrden;
import mx.uam.ayd.proyecto.presentacion.principal.ControlPrincipal;
import mx.uam.ayd.proyecto.presentacion.visualizarOrden.ControlVisualizarOrden;

// Componente controlador para la gestión de envío de órdenes a cocina y detalles de envío
@Component 
public class ControlEnviarOrdenCocina {

    // Capa de negocio y servicios inyectados mediante Spring
    @Autowired 
    private ServicioPedido servicioPedido;

    @Autowired
    private ServicioOrden servicioOrden;

    @Autowired
    @Lazy 
    private ControlPrincipal controlPrincipal; 

    @Autowired
    private VentanaEnviarOrdenCocina ventana;

    @Autowired
    @Lazy
    private ControlVisualizarOrden controlVisualizarOrden;

    @Autowired
    @Lazy
    private ControlCancelarOrden controlCancelarOrden;

    @Autowired
    @Lazy
    private VistaArmadoOrden vistaArmadoOrden;

    // Componentes de la interfaz de usuario (FXML) 
    @FXML private Button btnCancelarOrden;
    @FXML private Button btnVolver;
    @FXML private Label lblCliente;
    @FXML private Label lblMesa;
    @FXML private Label lblEstado;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    @FXML private TableView<DetallesPedido> tablaDetalle;
    @FXML private TableColumn<DetallesPedido, String> colCategoria;
    @FXML private TableColumn<DetallesPedido, String> colProducto;
    @FXML private TableColumn<DetallesPedido, Integer> colCantidad;
    @FXML private TableColumn<DetallesPedido, Integer> colPrecio;
    @FXML private TableColumn<DetallesPedido, Double> colSubtotal; // Actualizado a Double para coincidir con el modelo
    @FXML private Button btnEnviarCocina;

    // Componentes para el envío a domicilio
    @FXML private TitledPane paneDetallesEnvio;
    @FXML private VBox boxCostoEnvio;
    @FXML private TextField txtCostoEnvio;
    @FXML private Label lblDireccion;
    @FXML private Label lblTelefono;

    // Atributos de estado del controlador
    private double totalBase = 0.0;
    private long idPedidoActual;
    private Pedido pedidoActual;

    // Método de inicialización de la interfaz gráfica
    @FXML
    public void initialize() {
        configurarColumnas();
        configurarListenerEnvio(); 
    }

    // Configura la forma en que se leen los datos de la entidad DetallesPedido hacia las columnas de la tabla
    private void configurarColumnas() {
        if (colCategoria != null) {
            colCategoria.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPlatillo() != null ? 
                    cellData.getValue().getPlatillo().getTipoArea() : "N/A"));
        }
        if (colProducto != null) {
            colProducto.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPlatillo() != null ? 
                    cellData.getValue().getPlatillo().getNombre() : "Sin Nombre"));
        }
        if (colCantidad != null) {
            colCantidad.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getCantidad()).asObject());
        }
        if (colPrecio != null) {
            colPrecio.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getPlatillo() != null ? 
                    cellData.getValue().getPlatillo().getPrecio() : 0).asObject());
        }
        // Utiliza SimpleDoubleProperty para el manejo correcto del subtotal decimal
        if (colSubtotal != null) {
            colSubtotal.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getSubtotal()).asObject());
        }
    }

    // Filtra caracteres (solo números y punto decimal) y actualiza el total al instante
    private void configurarListenerEnvio() {
        if (txtCostoEnvio != null) {
            txtCostoEnvio.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*(\\.\\d*)?")) {
                    txtCostoEnvio.setText(oldValue);
                } else {
                    actualizarTotalConEnvio();
                }
            });
        }
    }

    // Realiza la suma matemática del costo de envío y actualiza la etiqueta del total
    private void actualizarTotalConEnvio() {
        double costoEnvio = 0.0;
        try {
            if (txtCostoEnvio.getText() != null && !txtCostoEnvio.getText().isEmpty()) {
                costoEnvio = Double.parseDouble(txtCostoEnvio.getText());
            }
        } catch (NumberFormatException e) {
            costoEnvio = 0.0;
        }
        double nuevoTotal = totalBase + costoEnvio;
        if (lblTotal != null) {
            lblTotal.setText(String.format("TOTAL: $%.2f", nuevoTotal));
        }
    }
    
    // Inicia y despliega la ventana pasando el objeto pedido completo
    public void inicia(Pedido pedido) {
        this.pedidoActual = pedido;
        if (pedido != null) {
            this.idPedidoActual = pedido.getIdPedido();
        }
        ventana.muestra();
        poblarDatosPedido();
    }

    // Inicia y despliega la ventana de envío a cocina utilizando el ID del pedido
    public void inicia(long idPedido) {
        this.idPedidoActual = idPedido;
        ventana.muestra();
        try {
            this.pedidoActual = servicioPedido.recuperaPedido(idPedidoActual);
            poblarDatosPedido();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Carga la interfaz con los datos generales del pedido, cálculo de IVA y detalles
    private void poblarDatosPedido() {
        if (pedidoActual != null) {
            List<DetallesPedido> detalles = servicioOrden.obtenerDetallesDePedido(pedidoActual.getIdPedido());
            if (lblCliente != null) {
                lblCliente.setText(pedidoActual.getCliente() != null ? pedidoActual.getCliente().getNombre() : "Cliente General");
            }
            if (lblMesa != null) {
                lblMesa.setText(String.valueOf(pedidoActual.getNumeroOrden()));
            }
            if (lblEstado != null) {
                lblEstado.setText(pedidoActual.getEstado() != null ? pedidoActual.getEstado() : "Pendiente de envío");
            }
            
            // Cálculo de subtotales e IVA
            double total = servicioOrden.calcularSubtotalTotal(pedidoActual.getIdPedido());
            this.totalBase = total;
            double subtotal = total / 1.16;
            double iva = total - subtotal;
            
            if (lblSubtotal != null) lblSubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
            if (lblIva != null) lblIva.setText(String.format("IVA: $%.2f", iva));
            if (lblTotal != null) lblTotal.setText(String.format("TOTAL: $%.2f", total));

            // Lógica para ocultar o mostrar secciones según el tipo de orden (Domicilio / Local)
            boolean esAdomicilio = pedidoActual.getTipoOrden() != null && 
                                   pedidoActual.getTipoOrden().equalsIgnoreCase("Domicilio");
            
            if (paneDetallesEnvio != null) {
                paneDetallesEnvio.setVisible(esAdomicilio);
                paneDetallesEnvio.setManaged(esAdomicilio);
            }
            if (boxCostoEnvio != null) {
                boxCostoEnvio.setVisible(esAdomicilio);
                boxCostoEnvio.setManaged(esAdomicilio);
            }
            
            // Si es a domicilio y cuenta con cliente asignado, inyecta su dirección y teléfono
            if (esAdomicilio && pedidoActual.getCliente() != null) {
                if (lblDireccion != null) lblDireccion.setText(pedidoActual.getCliente().getDireccion());
                if (lblTelefono != null) lblTelefono.setText(pedidoActual.getCliente().getTelefono());
            }

            // Inyecta la lista de platillos a la tabla de la vista
            if (tablaDetalle != null && detalles != null) {
                tablaDetalle.setItems(FXCollections.observableArrayList(detalles));
                tablaDetalle.refresh();
            }
        }
    }

    // Maneja el evento de envío a cocina, validando que haya platillos y costo de envío requerido
    @FXML
    void clickBotonEnviar(ActionEvent event) {
        // Validación: Evita enviar una orden vacía
        if (tablaDetalle == null || tablaDetalle.getItems().isEmpty()) {
            mostrarMensajeError("Orden Vacía", "No se puede enviar una orden sin platillos a la cocina.");
            return;
        }

        // Validación HU-15: Costo de envío obligatorio para pedidos a domicilio 
        boolean esAdomicilio = pedidoActual != null && 
                               pedidoActual.getTipoOrden() != null && 
                               pedidoActual.getTipoOrden().equalsIgnoreCase("Domicilio");

        if (esAdomicilio) {
            String textoCosto = (txtCostoEnvio != null && txtCostoEnvio.getText() != null) 
                                ? txtCostoEnvio.getText().trim() : "";
            
            if (textoCosto.isEmpty()) {
                mostrarMensajeError("Costo de Envío Requerido", 
                    "Esta orden es a domicilio. Por favor, ingresa el costo de envío antes de enviar a cocina.");
                return;
            }
        }
        
        try {
            double costo = 0.0;
            if (txtCostoEnvio != null && txtCostoEnvio.getText() != null && !txtCostoEnvio.getText().isEmpty()) {
                costo = Double.parseDouble(txtCostoEnvio.getText());
            }

            boolean exito = servicioPedido.procesarEnvioCocina(idPedidoActual, costo);
            
            if (exito) {
                if (lblEstado != null) {
                    lblEstado.setText("En Preparación");
                }
                
                // Abre y actualiza la vista de cocina tras el envío exitoso
                if (controlVisualizarOrden != null) {
                    controlVisualizarOrden.inicia();
                }

                mostrarMensajeExito();

                if (btnEnviarCocina != null && btnEnviarCocina.getScene() != null) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnEnviarCocina.getScene().getWindow();
                    stage.close();
                }
            }
        } catch (IllegalArgumentException e) {
            mostrarMensajeError("No se encontró el pedido", e.getMessage());
        } catch (IllegalStateException e) {
            mostrarMensajeError("La orden ya está en preparación y no puede ser modificada", e.getMessage());
        }
    }

    // Despliega ventanas de éxito para informar al usuario sobre la operación
    private void mostrarMensajeExito() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText("¡La orden ha sido enviada exitosamente a la cocina!");
        alert.showAndWait();
    }

    // Despliega ventanas de error para notificar incidencias
    private void mostrarMensajeError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Limpia la interfaz, cambia el estado y vacía la tabla tras una cancelación
    public void limpiarVistaDespuesDeCancelar() {
        if (lblEstado != null) {
            lblEstado.setText("Cancelada");
        }
        
        if (tablaDetalle != null) {
            tablaDetalle.getItems().clear();
            tablaDetalle.refresh();
        }
        
        if (btnEnviarCocina != null) {
            btnEnviarCocina.setDisable(true);
        }
        if (btnCancelarOrden != null) {
            btnCancelarOrden.setDisable(true);
        }

        if (vistaArmadoOrden != null) {
            vistaArmadoOrden.cierra();    
        }
    }

    // Abre la ventana de cancelación de la orden actual
    @FXML
    public void clickBotonCancelarOrden(ActionEvent event) {
        controlPrincipal.iniciaVentanaCancelarOrden();
        controlCancelarOrden.inicia(idPedidoActual);
    }

    // Regresa a la ventana de armado de orden reteniendo la información actual
    @FXML
    public void clickBotonVolver(ActionEvent event) {
        if (vistaArmadoOrden != null && pedidoActual != null) {
            vistaArmadoOrden.muestra(pedidoActual);
        }
        cerrar();
    }

    // Cierra la ventana actual de envío a cocina
    public void cerrar() {
        if (btnCancelarOrden != null && btnCancelarOrden.getScene() != null) {
            javafx.stage.Stage stage = (javafx.stage.Stage) btnCancelarOrden.getScene().getWindow();
            stage.close();
        } else if (btnVolver != null && btnVolver.getScene() != null) {
            javafx.stage.Stage stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
            stage.close();
        }
    }

    // Método que se invoca al cancelar para cerrar por completo las ventanas relacionadas
    public void cerrarTodoPorCancelacion() {
        cerrar();

        if (vistaArmadoOrden != null) {
            vistaArmadoOrden.cierra(); 
        }
    }
}