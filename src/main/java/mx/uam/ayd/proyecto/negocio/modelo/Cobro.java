package mx.uam.ayd.proyecto.negocio.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.sql.Date;
import java.sql.Time;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

/**
 * Entidad de negocio Cobro
 */
@Entity
public class Cobro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idCobro;

    private int pagoTotal;
    private String metodoPago;
    private Time hora;
    private Date fecha;
    private int cambio;


    // --- RELACIONES DEL MODELO DE DOMINIO ---
    // Muchos Cobros pertenecen a 1 Pedido
    @ManyToOne(targetEntity = Pedido.class, fetch = FetchType.LAZY)
    private Pedido pedido;


    // --- GETTERS Y SETTERS ---
    public long getIdCobro() {
        return idCobro;
    }

    public void setIdCobro(long idCobro) {
        this.idCobro = idCobro;
    }

    public int getPagoTotal() {
        return pagoTotal;
    }

    public void setPagoTotal(int pagoTotal) {
        this.pagoTotal = pagoTotal;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Time getHora() {
        return hora;
    }

    public void setHora(Time hora) {
        this.hora = hora;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getCambio() {
        return cambio;
    }

    public void setCambio(int cambio) {
        this.cambio = cambio;
    }

    

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

}
