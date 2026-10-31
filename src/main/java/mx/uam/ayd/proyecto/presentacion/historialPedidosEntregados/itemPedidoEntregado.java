package mx.uam.ayd.proyecto.presentacion.historialPedidosEntregados;

import java.util.List;

import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;

@Component
public class itemPedidoEntregado {
    // Controlador
    ControlHistorialPedidos control;

    // Elementos FXML
    // Contenedores
    @FXML private ToggleButton item; // Es el contenedor completo del item
    @FXML private VBox detallesPlatillos;
    @FXML private VBox contenedorPlatillos;
    // Textos
    @FXML private Label numPedido;
    @FXML private Label tipoPedido;
    @FXML private Label precio;
    @FXML private Label brevePlatillos;
    @FXML private Label destino;
    @FXML private Label cantArticulos;

    // Constructor necesario
    public itemPedidoEntregado(){}

    /**
     * Inicialización de la tarjeta
     */
    @FXML
    public void initialize() {
    }

    /**
     * Se da el control que controlará a esta tarjeta
     * @param control Es específicamente el control que tendrá
     */
    public void setControlHistorialPedidos(ControlHistorialPedidos control){
        this.control = control;
    }

    public void configurarDatos(Pedido pedido, ToggleGroup grupo){
        System.out.println("Se configuran datos");

        // Preparamos los elementos para ser modificados
        item.setToggleGroup(grupo);
        this.destino.getStyleClass().clear();


        // Conseguimos los datos que necesitamos
        Long num = pedido.getIdPedido();
        String tipo = pedido.getTipoOrden();
        int precio = pedido.getTotal();
        List<DetallesPedido> detalles = pedido.getDetallesPedido();
        String destino;

        if(pedido.getNumMesa() == 0){
            destino = "Cliente: "+pedido.getCliente().getNombre();
            this.destino.getStyleClass().add("texto-gris");
        }else{
            destino = "Mesa "+pedido.getNumMesa().toString();
            this.destino.getStyleClass().add("texto-rosa");
        }
        switch(tipo){
            case "Domicilio":
                break;
        }

        // Ingresamos los datos
        ingresarDatos(num, tipo, precio, detalles, destino);

        deshabilitar(detallesPlatillos);
        habilitar(brevePlatillos);
    }

    private void ingresarDatos(Long num, String tipo, int precio, List<DetallesPedido> detalles, String destino){
        numPedido.setText("#"+num);
        tipoPedido.setText(tipo);
        this.precio.setText("$"+Integer.toString(precio));
        this.destino.setText(destino);

        String breve="";
        int contadorArticulos = 0;
        contenedorPlatillos.getChildren().clear();
        Label txt = new Label();
        txt.getStyleClass().add("texto-gris");
        Region reg = new Region();
        HBox.setHgrow(reg, Priority.ALWAYS);
        Label estadoPlatillo = new Label();
        estadoPlatillo.getStyleClass().add("texto-gris");
        for(DetallesPedido detalle : detalles){
            contadorArticulos += detalle.getCantidad();

            // Mostramos los artículos breves
            if(!breve.equals(""))
                breve = breve + ", ";
            breve = breve + detalle.getPlatillo().getNombre();
        
            // Mostramos los detalles de artículos
            HBox fila = new HBox(); // Creamos el HBox que contendrá al platillo
            fila.setAlignment(Pos.CENTER_LEFT); // Aseguramos que esté centrado verticalmente y hacía la izquierda

            txt.setText(detalle.getCantidad()+" "+detalle.getPlatillo().getNombre());
            estadoPlatillo.setText("entregado");

            fila.getChildren().addAll(txt, reg, estadoPlatillo);
            contenedorPlatillos.getChildren().add(fila);
        }
        brevePlatillos.setText(breve);
        cantArticulos.setText(Integer.toString(contadorArticulos));
    }

    private void deshabilitar(Node objeto){
        objeto.setVisible(false);
        objeto.setDisable(true);
        objeto.setManaged(false);
    }
    private void habilitar(Node objeto){
        objeto.setVisible(true);
        objeto.setDisable(false);
        objeto.setManaged(true);
    }
}
