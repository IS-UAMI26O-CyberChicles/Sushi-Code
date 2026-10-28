package mx.uam.ayd.proyecto.negocio.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

// Entidad JPA que representa el detalle de un platillo dentro de una orden o pedido,
// incluyendo cantidad, subtotal, notas especiales y estado de completado en cocina.
@Entity
public class DetallesPedido {

    // Identificador único del detalle del pedido
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idDetallePedido;

    // Cantidad solicitada de este platillo específico
    private int cantidad;

    // Subtotal calculado para este detalle (cantidad * precio del platillo)
    private double subtotal;

    // Cadena de texto para almacenar notas o especificaciones especiales del cliente (HU-06)
    private String notas;

    // Estado que indica si el platillo ya fue completado o preparado en cocina
    private boolean completado;

    // Relación con el platillo correspondiente del menú
    @ManyToOne
    private Platillo platillo;

    // Relación con el pedido general al que pertenece este detalle
    @ManyToOne
    private Pedido pedido;

    // Constructor vacío por defecto requerido por JPA
    public DetallesPedido() {
    }

    // Constructor exacto utilizado en DatosPrueba: (int cantidad, String notas, double subtotal, Platillo platillo, Pedido pedido)
    public DetallesPedido(int cantidad, String notas, double subtotal, Platillo platillo, Pedido pedido) {
        this.cantidad = cantidad;
        this.notas = notas;
        this.subtotal = subtotal;
        this.platillo = platillo;
        this.pedido = pedido;
    }

    // Constructor alternativo si se pasa subtotal como entero o en otro orden: (int cantidad, String notas, int subtotal, Platillo platillo, Pedido pedido)
    public DetallesPedido(int cantidad, String notas, int subtotal, Platillo platillo, Pedido pedido) {
        this.cantidad = cantidad;
        this.notas = notas;
        this.subtotal = subtotal;
        this.platillo = platillo;
        this.pedido = pedido;
    }

    // Constructor básico para cantidad, notas, platillo y pedido
    public DetallesPedido(int cantidad, String notas, Platillo platillo, Pedido pedido) {
        this.cantidad = cantidad;
        this.notas = notas;
        this.platillo = platillo;
        this.pedido = pedido;
    }

    // Constructor con id, cantidad, platillo y pedido
    public DetallesPedido(int idDetallePedido, int cantidad, Platillo platillo, Pedido pedido) {
        this.idDetallePedido = idDetallePedido;
        this.cantidad = cantidad;
        this.platillo = platillo;
        this.pedido = pedido;
    }

    // Constructor completo con subtotal y estado de completado
    public DetallesPedido(int cantidad, double subtotal, String notas, boolean completado, Platillo platillo, Pedido pedido) {
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.notas = notas;
        this.completado = completado;
        this.platillo = platillo;
        this.pedido = pedido;
    }

    // Obtiene el identificador del detalle
    public long getIdDetallePedido() {
        return idDetallePedido;
    }

    // Establece el identificador del detalle
    public void setIdDetallePedido(long idDetallePedido) {
        this.idDetallePedido = idDetallePedido;
    }

    // Obtiene la cantidad de platillos en este detalle
    public int getCantidad() {
        return cantidad;
    }

    // Establece la cantidad de platillos
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // Obtiene el subtotal del detalle
    public double getSubtotal() {
        return subtotal;
    }

    // Establece el subtotal del detalle (recibe double)
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    // Sobrecarga para aceptar int en caso de que algún servicio o prueba lo requiera
    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    // Obtiene las notas especiales asociadas al platillo
    public String getNotas() {
        return notas;
    }

    // Establece o actualiza las notas especiales del platillo
    public void setNotas(String notas) {
        this.notas = notas;
    }

    // Verifica si el platillo ya fue completado en la pantalla de cocina
    public boolean isCompletado() {
        return completado;
    }

    // Establece el estado de completado del platillo
    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    // Obtiene el platillo asociado
    public Platillo getPlatillo() {
        return platillo;
    }

    // Establece el platillo asociado
    public void setPlatillo(Platillo platillo) {
        this.platillo = platillo;
    }

    // Obtiene el pedido al que pertenece este detalle
    public Pedido getPedido() {
        return pedido;
    }

    // Establece el pedido al que pertenece este detalle
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}