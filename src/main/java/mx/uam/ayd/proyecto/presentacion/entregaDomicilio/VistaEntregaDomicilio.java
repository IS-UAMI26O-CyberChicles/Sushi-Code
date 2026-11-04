package mx.uam.ayd.proyecto.presentacion.entregaDomicilio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Component
public class VistaEntregaDomicilio {

    @Autowired
    private ApplicationContext context;

    @FXML
    private Label lblPendientes;

    @FXML
    private VBox vboxContenedorEntregas;

    private Stage stage;

    public void muestra(ControlEntregaDomicilio control) {
        if (stage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Ventana-movil.fxml"));
                
                loader.setControllerFactory(type -> {
                    if (type == VistaEntregaDomicilio.class) {
                        return this;
                    }
                    return context.getBean(type);
                });
                
                Parent root = loader.load();

                stage = new Stage();
                stage.setTitle("Mis Entregas - Delivery");
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                System.err.println("Error al abrir la ventana de entregas: " + e.getMessage());
                e.printStackTrace();
            }
        }
        stage.show();
        stage.toFront();
    }

    // --- ESTOS SON LOS 3 MÉTODOS QUE VS CODE ESTÁ BUSCANDO ---

    public void limpiarContenedor() {
        if (vboxContenedorEntregas != null) {
            vboxContenedorEntregas.getChildren().clear();
        }
    }

    public void agregarTarjeta(Node tarjetaNode) {
        vboxContenedorEntregas.getChildren().add(tarjetaNode);
    }

    public void actualizarContador(int totalPendientes) {
        if (lblPendientes != null) {
            lblPendientes.setText("(" + totalPendientes + " Pendientes)");
        }
    }
}