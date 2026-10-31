package mx.uam.ayd.proyecto.presentacion.historialPedidosEntregados;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import mx.uam.ayd.proyecto.negocio.ServicioPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;
import mx.uam.ayd.proyecto.presentacion.registrarPedido.ControlRegistroPedido;

/**
 * Control con enfoque en la HU-11 (CYB-11)
 * @author JLCB
 */
@Component
public class ControlHistorialPedidos {
    // Se declara a la vista principal a la que controla
    private final VistaHistorialPedidos vista;
    // Se declara los controles a los que se puede comunicar
    private final ControlRegistroPedido controlRegistroPedido;
    // Se declaran los servicios a los que se comunicará
    private final ServicioPedido servicioPedido;

    @Autowired
    public ControlHistorialPedidos(VistaHistorialPedidos vista, ControlRegistroPedido controlRegistroPedido, ServicioPedido servicioPedido){
        this.vista = vista;
        this.controlRegistroPedido = controlRegistroPedido;
        this.servicioPedido = servicioPedido;
    }

    /**
	 * Método que se ejecuta después de la construcción del bean
	 * y realiza la conexión bidireccional entre el control y la ventana
	 */
    @PostConstruct
    public void init() {
        vista.setControlHistorialPedidos(this);
    }

    /**
     * Método con el que se dará inicio a la HU-11, y por lo tanto, a sus vistas y controladores que sean necesarios
     * @author JLCB
    */
    public void inicia(){
        List<Pedido> pedidosEntregados = servicioPedido.recuperaPedidosEntregados();
        vista.muestra(pedidosEntregados);
    }

    /**
     * Un método encargado de coordinar el traslado al control RegistroPedido
     */
    public void irRegistroPedido(){
        controlRegistroPedido.inicia();
        vista.cierra();
    }
}
