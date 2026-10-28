package mx.uam.ayd.proyecto.presentacion.historialPedidosEntregados;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Control con enfoque en la HU-11 (CYB-11)
 * @author JLCB
 */
@Component
public class ControlHistorialPedidos {
    // Se declara a la vista principal a la que controla
    private final VistaHistorialPedidos vista;

    @Autowired
    public ControlHistorialPedidos(VistaHistorialPedidos vista){
        this.vista = vista;
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
}
