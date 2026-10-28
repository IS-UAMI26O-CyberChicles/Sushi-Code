package mx.uam.ayd.proyecto.negocio.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Entidad de negocio Platillo
 */
@Entity
public class Platillo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idPlatillo;

    private String nombre;
    private int precio;
    private String tipoArea;
    private String descripcion;

    // Constructores
    /**
     * Constructor cuyo propósito es ser generado y posteriormente settear sus atributos
     */
    public Platillo(){}
    /**
     * Constructor cuyo propósito es facilitar su generación para los datos de prueba
     */
    public Platillo(String nombre, int precio, String tipoArea, String descripcion){
        this.nombre = nombre;
        this.precio = precio;
        this.tipoArea = tipoArea;
        this.descripcion = descripcion;
    }

    // --- GETTERS Y SETTERS ---
    public long getIdPlatillo() {
        return idPlatillo;
    }

    public void setIdPlatillo(long idPlatillo) {
        this.idPlatillo = idPlatillo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public String getTipoArea() {
        return tipoArea;
    }

    public void setTipoArea(String tipoArea) {
        this.tipoArea = tipoArea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}