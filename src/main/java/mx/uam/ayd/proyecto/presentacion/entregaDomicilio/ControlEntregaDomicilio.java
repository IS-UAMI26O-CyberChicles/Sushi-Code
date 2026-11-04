package mx.uam.ayd.proyecto.presentacion.entregaDomicilio;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node; 
import mx.uam.ayd.proyecto.negocio.ServicioPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;

@Component
public class ControlEntregaDomicilio {

    @Autowired
    private ServicioPedido servicioPedido;

    @Autowired
    private VistaEntregaDomicilio vista;

    @Autowired
    private ApplicationContext context;

    public void inicia() {
        // 1. Primero abrimos la vista para que inicialice sus componentes FXML
        vista.muestra(this);

        // 2. Limpiamos tarjetas anteriores
        vista.limpiarContenedor();

        // 3. Obtenemos pedidos a entregar
        List<Pedido> pedidos = servicioPedido.obtenerPedidosAEntregar();
        vista.actualizarContador(pedidos.size());

        // 4. Cargamos cada tarjeta con la ruta EXACTA de tu FXML
        for (Pedido pedido : pedidos) {
            try {
                System.out.println("--- Intentando crear tarjeta para orden: " + pedido.getNumeroOrden());
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Item-Tarjeta-Entrega.fxml"));
                loader.setControllerFactory(context::getBean);
                Node tarjetaNode = loader.load();

                ControlItemTarjetaEntrega itemControl = loader.getController();
                itemControl.setDatos(pedido, this); 

                vista.agregarTarjeta(tarjetaNode);
                
                System.out.println("--- Tarjeta agregada al contenedor visual con éxito");
            } catch (Exception e) {
                System.err.println("Error al cargar tarjeta de entrega: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void marcarPedidoComoEntregado(Pedido pedido) {
        try {
            pedido.setEstado("Entregado");
            servicioPedido.guardarPedido(pedido);
            inicia();
        } catch (Exception e) {
            System.err.println("Error al marcar pedido como entregado: " + e.getMessage());
        }
     
}
}