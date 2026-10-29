package mx.uam.ayd.proyecto.negocio.modelo;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * Entidad de negocio Pedido
 * 
 * @author CodeHunters 
 *
 */
@Entity
public class Pedido {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long idPedido;

	private String tipoOrden; // Domicilio, Local, Recoger
	private int numeroOrden;
	private char vaPara;
	private String servicios;
	private String estado;
	private String motivoCancelacion;
	private int total;

    // NUEVO ATRIBUTO PARA EL COSTO DE ENVIO
    private double costoEnvio;
    private int numMesa;


    // --- RELACIONES DEL MODELO DE DOMINIO ---

   // 1 Pedido CONTIENE muchos DetallesPedido 
    @OneToMany(targetEntity = DetallesPedido.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_pedido")
    private List<DetallesPedido> detallesPedido = new ArrayList<>();

    // 1 Pedido TIENE muchos Cobros
    @OneToMany(targetEntity = Cobro.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_pedido")
    private final List<Cobro> cobros = new ArrayList<>();

    // Muchos pedidos pueden ser de 1 cliente
    @ManyToOne(targetEntity = Cliente.class, fetch = FetchType.EAGER)
    private Cliente cliente;

    // Constructores
    /**
     * Constructor cuyo propósito es ser generado y posteriormente settear sus atributos
     */
    public Pedido() {}
    /**
     * Constructor cuyo propósito es facilitar su generación para los datos de prueba
     */
    public Pedido(String tipoOrden, int numeroOrden, char vaPara, String servicios, String estado, String motivoCancelacion, int total) {
        this.tipoOrden = tipoOrden;
        this.numeroOrden = numeroOrden;
        this.vaPara = vaPara;
        this.servicios = servicios;
        this.estado = estado;
        this.motivoCancelacion = motivoCancelacion;
        this.total = total;
    }


    // --- GETTERS Y SETTERS ---
	 // ID PEDIDO
    public long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(long idPedido) {
        this.idPedido = idPedido;
    }

	// TIPO ORDEN

    public String getTipoOrden() {
        return tipoOrden;
    }

    public void setTipoOrden(String tipoOrden) {
        this.tipoOrden = tipoOrden;
    }

	// NUMERO ORDEN
    public int getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(int numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

	// VA PARA
    public char getVaPara() {
        return vaPara;
    }

    public void setVaPara(char vaPara) {
        this.vaPara = vaPara;
    }

	// SERVICIOS
    public String getServicios() {
        return servicios;
    }

    public void setServicios(String servicios) {
        this.servicios = servicios;
    }

	// ESTADO
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

	// MOTIVO CANCELACION
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

	// TOTAL
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
    // --- NUEVO: COSTO DE ENVÍO ---
    public double getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(double costoEnvio) {
        this.costoEnvio = costoEnvio;
    }
    // Número de mesa
    public int getNumMesa(){
        return this.numMesa;
    }
    public void setNumMesa(int num){
        this.numMesa = num;
    }

    // --- RELACIONES DEL MODELO DE DOMINIO GETTERS Y SETTERS ---
    public Cliente getCliente() { 
        return cliente; 
    }
    public void setCliente(Cliente cliente) {
        this.cliente = cliente; 
        }

    public List<DetallesPedido> getDetallesPedido() { 
        return detallesPedido; 
    }
    public List<Cobro> getCobros() {
        return cobros; 
        }

    // MÉTODO EQUALS COMPARA SI DOS OBJETOS SON IGUALES BASÁNDOSE EN EL ID DEL PEDIDO 
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() == obj.getClass()) {
            Pedido other = (Pedido) obj;
            return this.numeroOrden == other.numeroOrden;
        }
        return false;
    }
// METODO HASHCODE SIMPLIFICA EL CÓDIGO PARA OBTENER UN CÓDIGO HASH BASADO EN EL ID DEL PEDIDO

    @Override
    public int hashCode() {
        return (int) (31 * numeroOrden);
    }
// TOSTRING SOBREESCRIBE EL MÉTODO PARA REPRESENTAR EL OBJETO PEDIDO COMO UNA CADENA DE TEXTO

    @Override
    public String toString() {
        return "Pedido [idPedido=" + idPedido + ", numeroOrden=" + numeroOrden + ", estado=" + estado + ", total=" + total + "]";
    }

    public void setDetallesPedido(List<DetallesPedido> detalles){
        this.detallesPedido = detalles;
    }


    // Agrega un cobro a la lista de cobros del pedido
    public void agregarCobro(Cobro cobro) {
        if (cobro != null) {
            this.cobros.add(cobro);
        }
    }

    // Retorna true si el pedido ya tiene al menos un cobro registrado
    public boolean esPagado() {
        if (this.cobros != null && this.cobros.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
	
}
