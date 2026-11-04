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

    private ControlEntregaDomicilio controlPadre;
    private Pedido pedidoActual;

    public void setDatos(Pedido pedido, ControlEntregaDomicilio controlPadre) {
        this.pedidoActual = pedido;
        this.controlPadre = controlPadre;

        // 1. Número de orden de la entidad Pedido
        lblNumeroOrden.setText("#" + pedido.getNumeroOrden());
        
        // 2. Hora (Se genera al vuelo usando la hora actual del sistema)
        String horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        lblHora.setText("🕒 " + horaActual);

        // 3. Extracción segura de la relación @ManyToOne con Cliente
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

        // 4. Cálculo del Total a cobrar (Manejo seguro del Lazy Loading de Hibernate)
        double totalCalculado = pedido.getTotal();
        
        try {
            // Intentamos calcular el total si viene en 0
            if (totalCalculado == 0 && pedido.getDetallesPedido() != null) {
                for (DetallesPedido detalle : pedido.getDetallesPedido()) {
                    if (detalle.getPlatillo() != null) {
                        totalCalculado += detalle.getCantidad() * detalle.getPlatillo().getPrecio();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Aviso: No se pudieron calcular los detalles por LazyInitialization. Mostrando costo de envío o cero.");
            if (totalCalculado == 0) {
                totalCalculado = pedido.getCostoEnvio();
            }
        }
        
        // Sumamos el costo de envío al total que obtuvimos arriba
        totalCalculado = totalCalculado + pedido.getCostoEnvio();

       try {
            if (pedido.esPagado()) {
                lblTotalCobrar.setText("$0.00 (Pagado)");
                lblTotalCobrar.setStyle("-fx-text-fill: #00FF00; -fx-font-weight: bold;"); 
            } else {
                lblTotalCobrar.setText(String.format("$%.2f", totalCalculado));
                lblTotalCobrar.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); 
            }
        } catch (Exception e) {
            // Si Hibernate bloquea la lectura de los cobros, atrapamos el error y mostramos el total normal
            System.out.println("Aviso: No se pudieron leer los cobros del pedido #" + pedido.getNumeroOrden());
            lblTotalCobrar.setText(String.format("$%.2f", totalCalculado));
            lblTotalCobrar.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); 
        }
    }
    
    @FXML
    private void clicMarcarEntregado() {
        if (controlPadre != null && pedidoActual != null) {
            controlPadre.marcarPedidoComoEntregado(pedidoActual);
        }
    }
}