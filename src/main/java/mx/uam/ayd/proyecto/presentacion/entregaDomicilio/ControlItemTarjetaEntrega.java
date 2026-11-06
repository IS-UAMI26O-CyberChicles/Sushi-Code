package mx.uam.ayd.proyecto.presentacion.entregaDomicilio;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import mx.uam.ayd.proyecto.negocio.modelo.Cliente;
import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;

@Component
@Scope("prototype")
public class ControlItemTarjetaEntrega {

    // componentes de la vista FXML
    @FXML
    private Label lblNumeroOrden;
    @FXML
    private Label lblHora;
    @FXML
    private Label lblCliente;
    @FXML
    private Label lblTelefono;
    @FXML
    private Label lblDireccion;
    @FXML
    private Label lblTotalCobrar;
    @FXML
    private Button btnMarcarEntregado;
    
    // Referencias de control y modelo
    private ControlEntregaDomicilio controlPadre;
    private Pedido pedidoActual;

    // Este metodo llena los datos de la tarjeta de entrega con la información del pedido y del cliente
    public void setDatos(Pedido pedido, ControlEntregaDomicilio controlPadre) {
        this.pedidoActual = pedido;
        this.controlPadre = controlPadre;

        // 1. Número de orden de la entidad Pedido
        lblNumeroOrden.setText("#" + pedido.getNumeroOrden());
        
        // 2. genera y muestra la hora actual en formato de 24 hrs
        String horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        lblHora.setText("🕒 " + horaActual);

        // 3. Muestra la información del cliente asociado al pedido, con manejo de posibles valores nulos
        Cliente cliente = pedido.getCliente();
        if (cliente != null) {
            lblCliente.setText(cliente.getNombre() != null ? cliente.getNombre() : "Sin Nombre");
            lblTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "Sin Teléfono");
            lblDireccion.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "Sin Dirección");
        } else {
            lblCliente.setText("Cliente no registrado");
            lblTelefono.setText("N/A");
            lblDireccion.setText("N/A");
        }

        // 4. Cálculo del Total a cobrar 
        double totalCalculado = pedido.getTotal();
        
        try {
            // si el total es 0, intentamos calcularlo a partir de los detalles del pedido
            if (totalCalculado == 0 && pedido.getDetallesPedido() != null) {
                for (DetallesPedido detalle : pedido.getDetallesPedido()) {
                    if (detalle.getPlatillo() != null) {
                        totalCalculado += detalle.getCantidad() * detalle.getPlatillo().getPrecio();
                    }
                }
            }
        } catch (Exception e) {
            // si los detalles no estan cargados en memoria (LazyInitialization), mostramos un aviso y usamos el costo de envío como total
            System.out.println("Aviso: No se pudieron calcular los detalles por LazyInitialization. Mostrando costo de envío o cero.");
            if (totalCalculado == 0) {
                totalCalculado = pedido.getCostoEnvio();
            }
        }
        
        // Sumamos el costo de envío al total que obtuvimos arriba
        totalCalculado = totalCalculado + pedido.getCostoEnvio();
       
       //5. verificamos si el pedido está pagado y actualizamos la etiqueta de total a cobrar con el estilo correspondiente
       try {
            if (pedido.esPagado()) {
                // Si el pedido está pagado, mostramos el total como "$0.00 (Pagado)" y aplicamos un estilo verde
                lblTotalCobrar.setText("$0.00 (Pagado)");
                lblTotalCobrar.setStyle("-fx-text-fill: #00FF00; -fx-font-weight: bold;"); 
            } else {
                // Si el pedido no está pagado, mostramos el total calculado y aplicamos un estilo blanco
                lblTotalCobrar.setText(String.format("$%.2f", totalCalculado));
                lblTotalCobrar.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); 
            }
        } catch (Exception e) {
            // Si los cobros no se cargaron correctamente, mostramos un aviso y aplicamos un estilo blanco al total a cobrar
            System.out.println("Aviso: No se pudieron leer los cobros del pedido #" + pedido.getNumeroOrden());
            lblTotalCobrar.setText(String.format("$%.2f", totalCalculado));
            lblTotalCobrar.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); 
        }
    }
    
    // Este metodo se llama cuando el usuario hace clic en el botón "Marcar como Entregado"
    @FXML
    private void clicMarcarEntregado() {
        if (controlPadre != null && pedidoActual != null) {
            controlPadre.marcarPedidoComoEntregado(pedidoActual);
        }
    }
}