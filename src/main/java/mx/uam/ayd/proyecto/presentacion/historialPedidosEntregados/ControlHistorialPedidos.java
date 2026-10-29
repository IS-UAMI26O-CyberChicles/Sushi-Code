package mx.uam.ayd.proyecto.presentacion.historialPedidosEntregados;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
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

    @Autowired
    public ControlHistorialPedidos(VistaHistorialPedidos vista, ControlRegistroPedido controlRegistroPedido){
        this.vista = vista;
        this.controlRegistroPedido = controlRegistroPedido;
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
        vista.muestra();
    }

    /**
     * Un método encargado de coordinar el traslado al control RegistroPedido
     */
    public void irRegistroPedido(){
        controlRegistroPedido.inicia();
        vista.cierra();
    }
}
