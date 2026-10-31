package mx.uam.ayd.proyecto.presentacion.historialPedidosEntregados;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;

/**
 * Vista encargada de la comunicación con la ventana principal de la HU-11 (CYB-11)
 * @author JLCB
 */
@Component
public class VistaHistorialPedidos {
    private Stage stage;
    private ControlHistorialPedidos control; // Control encargado de la vista
    private boolean initialized = false;

	// Contenedores FXML
	@FXML private VBox contenedorPedidos;

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
    public void muestra(List<Pedido> pedidosCompletados){
        if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> this.muestra(pedidosCompletados));
			return;
		}
		initializeUI();
		
		// Métodos encargados de que los datos recopilados sean mostrados antes de mostrarse
		mostrarEntregados(pedidosCompletados);


        stage.show();
    }

	private void mostrarEntregados(List<Pedido> pedidosEntregados){
		contenedorPedidos.getChildren().clear();

		ToggleGroup grupo = new ToggleGroup(); // Grupo al que pertenecerá los toggle creados
		for(Pedido pedido : pedidosEntregados){
			try{
			// Carga a la plantilla de la tarjeta de los pedidos
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/items-historial-pedidos/plantilla-pedido-entregado.fxml"));
			itemPedidoEntregado tarjeta = new itemPedidoEntregado(); // Se asigna el controlador hecho a la tarjeta
			tarjeta.setControlHistorialPedidos(control); // Se da que este controlador tiene el control sobre las tarjetas, para apoyar la comunicación con los servicios
			loader.setController(tarjeta);
			Node tarjetaVisual = loader.load(); // Se inicializa la tarjeta
			tarjeta.configurarDatos(pedido, grupo); // Mediante el controlador se llama al método para ingresar la información
			contenedorPedidos.getChildren().add(tarjetaVisual); // Se agrega la tarjeta al contenedor
			}catch(Exception e){
                e.printStackTrace();
            }
			
		}
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
