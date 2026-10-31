package mx.uam.ayd.proyecto.negocio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.uam.ayd.proyecto.datos.ClienteRepository;
import mx.uam.ayd.proyecto.datos.DetallesPedidoRepository;
import mx.uam.ayd.proyecto.datos.PedidoRepository;
import mx.uam.ayd.proyecto.negocio.modelo.Cliente;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;
import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;

/**
 * Lógica de negocio para la gestión de Pedidos
 */
@Service
public class ServicioPedido {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private DetallesPedidoRepository detallesPedidoRepositoty;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Genera un número de orden consecutivo automático y seguro.
     * Cuenta los registros existentes y le suma 1, asegurando que pase de #1 a #2, #3, etc.
     */
    private int generarSiguienteNumeroOrden() {
        long totalPedidos = pedidoRepository.count();
        return (int) (totalPedidos + 1);
    }

    /**
    * Guarda o actualiza un pedido en la base de datos.
    */

    public Pedido guardarPedido(Pedido pedido) {
        try{
            pedido = pedidoRepository.save(pedido);
            return pedido;
        } catch (Exception e){
            System.out.println("El pedido es Nulo");
            return null;
        }
    }

    /**
     * Valida que los datos del formulario a domicilio no estén vacíos
     * y que el teléfono tenga la longitud correcta (RN-02).
     */
    public boolean validarDatosDomicilio(String nombre, String telefono, String direccion) {
        if (nombre == null || nombre.trim().isEmpty()) return false;
        if (telefono == null || !telefono.matches("\\d{10}")) return false;
        if (direccion == null || direccion.trim().isEmpty()) return false;
        return true;
    }

    /**
     * Crea un nuevo cliente y su pedido asociado para entrega a domicilio con número automático.
     */
    @Transactional
    public Pedido crearPedidoDomicilio(String nombre, String telefono, String direccion) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        cliente = clienteRepository.save(cliente);

        Pedido pedido = new Pedido();
        pedido.setTipoOrden("Domicilio");
        pedido.setEstado("Pendiente");
        pedido.setNumeroOrden(generarSiguienteNumeroOrden()); // <--- Asigna el consecutivo automático aquí
        pedido.setCliente(cliente);
        
        return pedidoRepository.save(pedido);
    }

    /**
     * Crea un nuevo cliente y su pedido asociado para recoger en sucursal con número automático.
     */
    @Transactional
    public Pedido crearPedidoRecoger(String nombre, String telefono) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        
        if (telefono != null && !telefono.trim().isEmpty()) {
            cliente.setTelefono(telefono);
        }
        cliente = clienteRepository.save(cliente);

        Pedido pedido = new Pedido();
        pedido.setTipoOrden("Recoger");
        pedido.setEstado("Pendiente");
        pedido.setNumeroOrden(generarSiguienteNumeroOrden()); // <--- Asigna el consecutivo automático aquí
        pedido.setCliente(cliente);

        return pedidoRepository.save(pedido);
    }

    /**
     * Crea un pedido para consumo local asignado a una mesa específica con número automático.
     */
    @Transactional
    public Pedido crearPedidoLocal(int numeroMesa) {
        Cliente cliente = new Cliente();
        cliente.setNombre("Mesa " + numeroMesa);
        cliente = clienteRepository.save(cliente);

        Pedido pedido = new Pedido();
        pedido.setTipoOrden("Local");
        pedido.setEstado("Pendiente");
        pedido.setNumeroOrden(generarSiguienteNumeroOrden()); // <--- Asigna el consecutivo automático aquí
        pedido.setNumMesa(numeroMesa); // Se asigna el número de la mesa correspondiente
        pedido.setCliente(cliente);

        return pedidoRepository.save(pedido);
    }


    // HU-O3 ENVIAR ORDEN COSINA 
    // RECUPERA EL PEDIDO PO ID Y LOS DETALLES
    @Transactional(readOnly = true)
    public Pedido recuperaPedido(long idPedido) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(idPedido);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            if (pedido.getDetallesPedido() != null) {
                pedido.getDetallesPedido().size(); 
            }
            return pedido;
        }
        return null;
    }

    // PROCESA EL ENVIO A COSINA Y DIVIDE COMANDAS 
    @Transactional
    public boolean procesarEnvioCocina(long idPedido, double costoEnvio) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(idPedido);
        
        if (pedidoOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el pedido con ID: " + idPedido);
        }
        
        Pedido pedido = pedidoOpt.get();
        // RN-05: VALIDA QUE LA ORDEN NO HAYA SIDO ENVIADA PREVIAMENTE
        if ("En Preparación".equals(pedido.getEstado())) {
            throw new IllegalStateException("La orden ya fue enviada a cocina y no puede ser modificada.");
        }
        
        // RN-05: VALIDA QUE SI YA ESTA CANCELADA NO SE PUEDA ENVIAR
        if ("Cancelada".equalsIgnoreCase(pedido.getEstado())) {
            throw new IllegalStateException("Esta orden ha sido cancelada y no puede enviarse a cocina.");
        }

        List<DetallesPedido> detalles = pedido.getDetallesPedido();
        
        // LISTAS PARA SEPARAR LOS PLATILLOS SEGUN EL AREA 
        List<DetallesPedido> paraPlancha = new ArrayList<>();
        List<DetallesPedido> paraRollos = new ArrayList<>();
        // RN-04: DIVIDE LA ORDEN POR AREA 
        if (detalles != null) {
            for (DetallesPedido detalle : detalles) {
                if (detalle.getPlatillo() != null) {
                    String area = detalle.getPlatillo().getTipoArea();
                    
                    if ("Plancha".equalsIgnoreCase(area)) {
                        paraPlancha.add(detalle);
                    } else if ("Rollos".equalsIgnoreCase(area)) {
                        paraRollos.add(detalle);
                    }
                }
            }
        }
        // ENVIO DE LAS LISTAS FILTRADAS 
        enviarAPlancha(paraPlancha);
        enviarARollos(paraRollos);
        //ACTUALIZA EL ESTADO
        pedido.setEstado("En Preparación");
        pedidoRepository.save(pedido);

        // NUEVO GUARDA EL COSTO DE ENVIO
        pedido.setCostoEnvio(costoEnvio);
        
        pedidoRepository.save(pedido);
        return true; 
    }
    // SIMULA EL ENVIO DE PLATILLOS A PLANCHA 
    private void enviarAPlancha(List<DetallesPedido> detalles) {
        if (detalles != null && !detalles.isEmpty()) {
            System.out.println("-> Enviando " + detalles.size() + " platillos a PLANCHA.");
        }
    }


    // SIMULA EL ENVIO DE PLATILLOS A ROLLOS 
    private void enviarARollos(List<DetallesPedido> detalles) {
        if (detalles != null && !detalles.isEmpty()) {
            System.out.println("-> Enviando " + detalles.size() + " platillos a ROLLOS.");       }
    }

    // HU-04 CANCELAR ORDEN

    @Transactional
    public boolean cancelarPedido(long idPedido, String motivoCancelacion, String idUsuario) {
        // PIDE MOTIVO OBLIGATORIO 
        if (motivoCancelacion == null || motivoCancelacion.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio.");
        }
        // RECUPERA EL PEDIDO DE LA BD
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(idPedido);
        
        if (pedidoOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el pedido con ID: " + idPedido);
        }
        
        Pedido pedido = pedidoOpt.get();
        // CAMBIA EL ESTADO A CANCELADA 
        pedido.setEstado("Cancelada");
        // GUARDA EL REGISTRO DE CANCELACIONES 
        String detalleCancelacion = motivoCancelacion.trim() + " (Cancelado por: " + idUsuario + ")";
        pedido.setMotivoCancelacion(detalleCancelacion);
        // GUARDA CAMBIOS EN LA BD
        pedidoRepository.save(pedido);

        return true; 
    }


    // HU-05
    /**
     * Recupera pedidos pendientes / en preparación para la comanda de cocina.
     */
    @Transactional(readOnly = true)
    public List<Pedido> recuperaPedidosPendientes() {
        List<Pedido> pedidosPendientes = new ArrayList<>();
        Iterable<Pedido> todosLosPedidos = pedidoRepository.findAll();
        
        for (Pedido pedido : todosLosPedidos) {
            if ("En Preparación".equalsIgnoreCase(pedido.getEstado()) || "Pendiente".equalsIgnoreCase(pedido.getEstado())) {
                if (pedido.getDetallesPedido() != null) {
                    pedido.getDetallesPedido().size();
                }
                pedidosPendientes.add(pedido);
            }
        }
        
        return pedidosPendientes;
    }

    /**
     * Recupera pedidos completados de cocina.
     */
    @Transactional(readOnly = true)
    public List<Pedido> recuperaPedidosCompletados() {
        List<Pedido> completados = new ArrayList<>();
        Iterable<Pedido> todosLosPedidos = pedidoRepository.findAll();
        
        for (Pedido pedido : todosLosPedidos) {
            if ("Completado".equalsIgnoreCase(pedido.getEstado())) {
                if (pedido.getDetallesPedido() != null) {
                    pedido.getDetallesPedido().size();
                }
                completados.add(pedido);
            }
        }
        
        return completados;
    }

    /**
     * Finaliza una orden introduciendo su número mediante el pad numérico de cocina.
     */
    @Transactional
    public boolean finalizarOrdenPorNumero(int numeroOrden) {
        List<Pedido> pendientes = recuperaPedidosPendientes();
        
        for (Pedido pedido : pendientes) {
            if (pedido.getNumeroOrden() == numeroOrden) {
                pedido.setEstado("Completado");
                pedidoRepository.save(pedido);
                return true;
            }
        }
        return false;
    }


    /**
     * Es un método encargado de cambiar el estado de un pedido como a Entregado
     * @author JLCB
     * @param pedido Es el pedido del que queremos cambiar su estado a Entregado
     * @return Regresa true si se logró guardar correctamente el cambio del estado, false en el caso contrario
     */
    public boolean marcarComoEntregado(Pedido pedido){
        if(pedido == null) return false;
        System.out.println("Se intenta modificar el pedido "+pedido);

        if(pedidoRepository.existsById(pedido.getIdPedido())){ // Verificamos que el pedido exista en el repositorio
            pedido.setEstado("Entregado");
            pedido = pedidoRepository.save(pedido);
            System.out.println("Se guardó el pedido: "+pedido);
            return pedido.getEstado() == "Entregado";
        }else return false;
    }

    /**
     * Método encargado de asignar a un pedido una lista de detalles, en la base de datos y volver a guardar el pedido
     * @author JLCB
     * @param pedido
     * @param detalles
     */
    public boolean asignarDetallesAPedido(Pedido pedido, List<DetallesPedido> detalles){
        if(pedido == null || detalles.size() == 0 || detalles == null){
            System.err.println("No fue posible enlazar el pedido con los detalles");
            return false;
        }

        List<DetallesPedido> detallesActualzados = new ArrayList<>();
        for(DetallesPedido detalle : detalles){
            detalle.setPedido(pedido);
            detalle = detallesPedidoRepositoty.save(detalle);
            detallesActualzados.add(detalle);
        }

        pedido.setDetallesPedido(detallesActualzados);
        return pedidoRepository.save(pedido) != null;
    }

    /**
     * Función que regresa a todos los pedidos cuyo estado se encuentra en "Entregado"
     * @return Regresa una lista con los pedidos con estado "Entregado"
     */
    @Transactional(readOnly = true)
    public List<Pedido> recuperaPedidosEntregados() {
        List<Pedido> pedidosEntregados = pedidoRepository.findByEstado("Entregado");
        
        return pedidosEntregados;
    }
}