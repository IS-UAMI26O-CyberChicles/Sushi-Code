package mx.uam.ayd.proyecto.presentacion.historialPedidosEntregados;

import java.io.IOException;

import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Vista encargada de la comunicación con la ventana principal de la HU-11 (CYB-11)
 * @author JLCB
 */
@Component
public class VistaHistorialPedidos {
    private Stage stage;
    private ControlHistorialPedidos control; // Control encargado de la vista
    private boolean initialized = false;

    public VistaHistorialPedidos(){}
    
    /**
	 * Initialize UI components on the JavaFX application thread
	 */
    private void initializeUI() {
		if (initialized) {
			return;
		}
		
		// Create UI only if we're on JavaFX thread
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(this::initializeUI);
			return;
		}
		
		try {
			stage = new Stage();
			stage.setTitle("Historial de pedidos");
			
			// Load FXML
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ventana-historial-pedidos.fxml"));
			loader.setController(this);
			Scene scene = new Scene(loader.load(), 1024, 768);
			
			stage.setScene(scene);
			stage.setMaximized(true);
            stage.toFront();
			
			initialized = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    /**
	 * Establece el controlador asociado a esta ventana
	 * 
	 * @param control El controlador asociado
	 */
	public void setControlHistorialPedidos(ControlHistorialPedidos control) {
		this.control = control;
	}

	/**
	 * Método con el que se da apertura a mostar la pantalla
	 * @author JLCB
	*/
    public void muestra(){
        if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> this.muestra());
			return;
		}
		initializeUI();
		
		// Métodos encargados de que los datos recopilados sean mostrados antes de mostrarse
		// ...

        stage.show();
    }

	/**
	 * La vista cierra la pantalla
	 */
	public void cierra(){
		stage.close();
	}

	// On Action's
	/**
	 * La acción que se tomará al presionar el botón "Volver a Menú Principal"
	 */
	@FXML
	public void onActionVolver(){
		System.out.println("Se presionó botón Volver a Menú Principal");
		control.irRegistroPedido();
	}
}
