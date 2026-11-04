package mx.uam.ayd.proyecto.datos;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

import mx.uam.ayd.proyecto.negocio.modelo.Pedido;

public interface PedidoRepository extends CrudRepository<Pedido, Long> {
     
    /**
     * BUSCA UN PEDIDO POR SU NÚMERO DE ORDEN.
     * 
     * @param numeroOrden NUMERO DE ORDEN DEL PEDIDO A BUSCAR.
     * @return EL PEDIDO QUE COINCIDE CON EL NÚMERO DE ORDEN, O NULL SI NO SE ENCUENTRA.
     */
    public Pedido findByNumeroOrden(int numeroOrden);

     // (HU-13): Busca pedidos por tipo de orden ("Domicilio") y estado ("Terminado")
    public List<Pedido> findByTipoOrdenAndEstado(String tipoOrden, String estado);

}
