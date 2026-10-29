package mx.uam.ayd.proyecto.presentacion.enviarOrdenCocina;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import mx.uam.ayd.proyecto.negocio.ServicioOrden;
import mx.uam.ayd.proyecto.negocio.ServicioPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Cobro;
import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;
import mx.uam.ayd.proyecto.presentacion.armadoOrden.VistaArmadoOrden;
import mx.uam.ayd.proyecto.presentacion.cancelarOrden.ControlCancelarOrden;
import mx.uam.ayd.proyecto.presentacion.principal.ControlPrincipal;
import mx.uam.ayd.proyecto.presentacion.visualizarOrden.ControlVisualizarOrden;

@Component  // EVITA HACER EL NEW
public class ControlEnviarOrdenCocina {

    // CAPA DE NEGOCIO Y SERVICIOS INYECTADOS

    @Autowired // AUTOCABLEADO
    private ServicioPedido servicioPedido;

    @Autowired
    private ServicioOrden servicioOrden;

    @Autowired
    @Lazy // CREA EL CONTROLADOR HASTA QUE ES NECESARIO
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

    // COMPONENTES DE LA INTERFAZ DE USUARIO (FXML) 
    
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
    @FXML private TableColumn<DetallesPedido, Integer> colSubtotal;
    @FXML private Button btnEnviarCocina;

    // COMPONENTES NUEVOS PARA EL ENVÍO A DOMICILIO Y PAGO

    @FXML private TitledPane paneDetallesEnvio;
    @FXML private VBox boxCostoEnvio;
    @FXML private TextField txtCostoEnvio;
    @FXML private Label lblDireccion;
    @FXML private Label lblTelefono;
    @FXML private RadioButton rbPendiente;
    @FXML private RadioButton rbTarjeta;
    @FXML private RadioButton rbEfectivo;

    // ATRIBUTOS DE ESTADO
    private double totalBase = 0.0;
    private long idPedidoActual;
    private Pedido pedidoActual;
    //para asegurar selección única
    private ToggleGroup grupoPago;

    // MÉTODO DE INICIALIZACIÓN DE LA INTERFAZ

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarListenerEnvio();
        configurarGrupoPago();
    }

    private void configurarGrupoPago() {
        if (rbPendiente != null && rbTarjeta != null && rbEfectivo != null) {
            grupoPago = new ToggleGroup();
            rbPendiente.setToggleGroup(grupoPago);
            rbTarjeta.setToggleGroup(grupoPago);
            rbEfectivo.setToggleGroup(grupoPago);
        }
    }

    // EL METODO DICE COMO LEER LOS DATOS DE LA ENTIDAD DETALLESPEDIDO
    // DE DETALLESPEDIDO A PLATILLO
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
        if (colSubtotal != null) {
            colSubtotal.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getSubtotal()).asObject());
        }
    }

    // FILTRA CARACTERES (SOLO NÚMEROS Y PUNTO DECIMAL) Y SUMA AL INSTANTE
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

    // REALIZA LA SUMA MATEMÁTICA Y ACTUALIZA LA ETIQUETA DEL TOTAL
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
    
    // INICIA Y DESPLIEGA LA VENTANA PASANDO EL OBJETO PEDIDO COMPLETO
    public void inicia(Pedido pedido) {
        this.pedidoActual = pedido;
        if (pedido != null) {
            this.idPedidoActual = pedido.getIdPedido();
        }
        ventana.muestra();
        poblarDatosPedido();
    }

    // INICIA Y DESPLIEGA LA VENTANA DE ENVÍO A COCINA CON EL ID DEL PEDIDO
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

    // CARGA LA INTERFAZ CON LOS DATOS DEL PEDIDO Y DETALLES
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
            // CALCULA EL IVA
            double total = servicioOrden.calcularSubtotalTotal(pedidoActual.getIdPedido());
            this.totalBase = total;
            double subtotal = total / 1.16;
            double iva = total - subtotal;
            
            if (lblSubtotal != null) lblSubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
            if (lblIva != null) lblIva.setText(String.format("IVA: $%.2f", iva));
            if (lblTotal != null) lblTotal.setText(String.format("TOTAL: $%.2f", total));

            //LÓGICA PARA OCULTAR O MOSTRAR SECCIONES SEGÚN TIPO DE ORDEN 
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
            
            // SI ES A DOMICILIO Y HAY UN CLIENTE ASIGNADO, INYECTA SU DIRECCIÓN Y TELÉFONO
            if (esAdomicilio && pedidoActual.getCliente() != null) {
                if (lblDireccion != null) lblDireccion.setText(pedidoActual.getCliente().getDireccion());
                if (lblTelefono != null) lblTelefono.setText(pedidoActual.getCliente().getTelefono());
            }

            // SE INYECTA LA LISTA DE PLATILLOS ALA TABLA
            if (tablaDetalle != null && detalles != null) {
                tablaDetalle.setItems(FXCollections.observableArrayList(detalles));
                tablaDetalle.refresh();
            }
        }
    }
    // MANEJA EL EVENTO DE ENVÍO A COCINA, VALIDANDO QUE HAYA PLATILLOS Y QUE EL COSTO DE ENVÍO SEA VÁLIDO
    @FXML
    void clickBotonEnviar(ActionEvent event) {
        // VALIDACIÓN: evita enviar una orden vacía
        if (tablaDetalle == null || tablaDetalle.getItems().isEmpty()) {
            mostrarMensajeError("Orden Vacía", "No se puede enviar una orden sin platillos a la cocina.");
            return;
        }

        // VALIDACIÓN HU-15: Costo de envío obligatorio para pedidos a domicilio 
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

            // REGISTRA EL COBRO SEGÚN EL MÉTODO DE PAGO SELECCIONADO
            if (pedidoActual != null) {
                pedidoActual.getCobros().clear(); // Limpia cobros anteriores para evitar duplicados

                if (rbTarjeta != null && rbTarjeta.isSelected()) {
                    Cobro cobro = new Cobro();
                    cobro.setPagoTotal((int) Math.round(totalBase + costo));
                    cobro.setMetodoPago("Tarjeta");
                    cobro.setFecha(new Date(System.currentTimeMillis()));
                    cobro.setHora(new Time(System.currentTimeMillis()));
                    cobro.setPedido(pedidoActual);

                    pedidoActual.agregarCobro(cobro);
                } else if (rbEfectivo != null && rbEfectivo.isSelected()) {
                    Cobro cobro = new Cobro();
                    cobro.setPagoTotal((int) Math.round(totalBase + costo));
                    cobro.setMetodoPago("Efectivo");
                    cobro.setFecha(new Date(System.currentTimeMillis()));
                    cobro.setHora(new Time(System.currentTimeMillis()));
                    cobro.setPedido(pedidoActual);

                    pedidoActual.agregarCobro(cobro);
                }
                // Si rbPendiente está seleccionado, la lista de cobros queda vacía y esPagado() devolverá false
            }

            boolean exito = servicioPedido.procesarEnvioCocina(idPedidoActual , costo);
            
            if (exito) {
                if (lblEstado != null) {
                    lblEstado.setText("En Preparación");
                }
                
                // ABRE Y ACTUALIZA LA VISTA DE COCINA 
                if (controlVisualizarOrden != null) {
                    controlVisualizarOrden.inicia();
                }

                mostrarMensajeExito();

                if (btnEnviarCocina != null && btnEnviarCocina.getScene() != null) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnEnviarCocina.getScene().getWindow();
                    stage.close();
                }
            }
            // SI EL PEDIDO NO EXISTE O YA ESTA EN PREPARACION
        } catch (IllegalArgumentException e) {
            mostrarMensajeError("No se encontró el pedido", e.getMessage());
        } catch (IllegalStateException e) {
            mostrarMensajeError("La orden ya está en preparación y no puede ser modificada", e.getMessage());
        }
    }

    // VENTANAS DE EXITO Y ERROR PARA INFORMAR AL USUARIO SOBRE EL ESTADO DE LA OPERACIÓN
    private void mostrarMensajeExito() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText("¡La orden ha sido enviada exitosamente a la cocina!");
        alert.showAndWait();
    }

    private void mostrarMensajeError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

     //LIMPIA LA INERFAZ , CAMBIA ESTADO Y LIMPIA TABLA     
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

    // ABRE LA NETANA DE CANCELAR
    @FXML
    public void clickBotonCancelarOrden(ActionEvent event) {
        
        controlPrincipal.iniciaVentanaCancelarOrden();
        controlCancelarOrden.inicia(idPedidoActual);
    }

    // REGRESA A LA VENTANA DE MENÚ / ARMADO DE ORDEN RETENIENDO LA INFORMACIÓN
    @FXML
    public void clickBotonVolver(ActionEvent event) {

        if (vistaArmadoOrden != null && pedidoActual != null) {
            vistaArmadoOrden.muestra(pedidoActual);
        }
        cerrar();
    }

    // CIERRA LA VENTANA DE ENVIO A COCINA
public void cerrar() {
    if (btnCancelarOrden != null && btnCancelarOrden.getScene() != null) {
        javafx.stage.Stage stage = (javafx.stage.Stage) btnCancelarOrden.getScene().getWindow();
        stage.close();
    } else if (btnVolver != null && btnVolver.getScene() != null) {
        javafx.stage.Stage stage = (javafx.stage.Stage) btnVolver.getScene().getWindow();
        stage.close();
    }
}
    // MÉTODO QUE SE INVOCA AL CANCELAR PARA CERRAR LA VENTANA DEL TICKET ANTERIOR
    public void cerrarTodoPorCancelacion() {
        cerrar();

        if (vistaArmadoOrden != null) {
            vistaArmadoOrden.cierra(); 
        }
    }
}