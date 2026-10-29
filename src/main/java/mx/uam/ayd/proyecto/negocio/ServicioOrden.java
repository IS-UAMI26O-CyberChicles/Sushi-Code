package mx.uam.ayd.proyecto.negocio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.uam.ayd.proyecto.datos.DetallesPedidoRepository;
import mx.uam.ayd.proyecto.datos.PedidoRepository;
import mx.uam.ayd.proyecto.datos.PlatilloRepository;
import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;
import mx.uam.ayd.proyecto.negocio.modelo.Platillo;

// Servicio encargado de la lógica de negocio para el armado de la orden (HU-02),
// incluyendo el cálculo de subtotales y la aplicación de promociones (2x$160 en Rollos Especiales).
@Service
public class ServicioOrden {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallesPedidoRepository detallesPedidoRepository;
    
    @Autowired
    private PlatilloRepository platilloRepository;

    // Procesa la adición de un nuevo platillo a la orden actual y lo guarda en H2,
    // calculando su subtotal inicial mediante las reglas de negocio correspondientes.
    public DetallesPedido procesarNuevoPlatillo(long idPlatillo, long idPedido) {
        
        // 1. Buscamos el Platillo y el Pedido en la base de datos
        Platillo platillo = platilloRepository.findById(idPlatillo)
                .orElseThrow(() -> new IllegalArgumentException("Platillo no encontrado"));
                
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // 2. Armamos el nuevo renglón del ticket (DetallesPedido)
        DetallesPedido nuevoDetalle = new DetallesPedido();
        nuevoDetalle.setPlatillo(platillo);
        nuevoDetalle.setPedido(pedido);
        nuevoDetalle.setCantidad(1); // Por defecto, al dar clic se agrega 1
        
        // Calculamos el subtotal considerando promociones (ej. Rollos Especiales)
        nuevoDetalle.setSubtotal(calcularSubtotalParaDetalle(platillo, 1));
        nuevoDetalle.setNotas("");

        // 3. El repositorio hace el guardado real en la BD H2 usando Hibernate
        return detallesPedidoRepository.save(nuevoDetalle);
    }

    // Modifica la cantidad de un platillo que ya está en la orden (sumar o restar),
    // recalculando automáticamente el subtotal aplicando tarifas de promoción si aplican.
    public DetallesPedido procesarCambioCantidad(long idDetalle, int operacion) {
        DetallesPedido detalle = detallesPedidoRepository.findById(idDetalle)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado"));
        
        int nuevaCantidad = detalle.getCantidad() + operacion;
        
        if (nuevaCantidad <= 0) {
            // Si la cantidad llega a 0, eliminamos el renglón del ticket
            detallesPedidoRepository.delete(detalle);
            return null; 
        } else {
            // Actualizamos la cantidad y recalculamos el subtotal conforme a la promoción
            detalle.setCantidad(nuevaCantidad);
            detalle.setSubtotal(calcularSubtotalParaDetalle(detalle.getPlatillo(), nuevaCantidad));
            return detallesPedidoRepository.save(detalle);
        }
    }

    // Método auxiliar privado que aplica la regla de negocio de Rollos Especiales (2 x $160)
    // y el cálculo estándar para el resto del menú.
    private double calcularSubtotalParaDetalle(Platillo platillo, int cantidad) {
        double precioUnitario = platillo.getPrecio();
        
        // Verifica si el platillo pertenece a la categoría de rollos especiales
        if ("Rollos especiales".equalsIgnoreCase(platillo.getTipoArea())) {
            int pares = cantidad / 2;       // Cantidad de parejas (ej. 3 / 2 = 1 par)
            int individuales = cantidad % 2; // Elemento sobrante si la cantidad es impar (ej. 3 % 2 = 1)
            
            // Cada par cuesta $160 y los adicionales se cobran al precio unitario normal ($103)
            return (pares * 160.0) + (individuales * precioUnitario);
        } else {
            // Cálculo estándar para cualquier otra categoría (Rollos, Plancha, Bebidas, Postres)
            return cantidad * precioUnitario;
        }
    }

    // Calcula el subtotal sumando el costo de todos los detalles de la orden.
    public double calcularSubtotalTotal(long idPedido) {
        List<DetallesPedido> detalles = obtenerDetallesDePedido(idPedido);
        double total = 0.0;
        for (DetallesPedido detalle : detalles) {
            total += detalle.getSubtotal();
        }
        return total;
    }

    // Obtiene todos los renglones asociados a un ticket.
    public List<DetallesPedido> obtenerDetallesDePedido(long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        return detallesPedidoRepository.findByPedido(pedido);
    }
    
    // Agrega una nota especial a un platillo en específico (ej. "Sin cebolla").
    public DetallesPedido agregarNota(long idDetalle, String nota) {
        DetallesPedido detalle = detallesPedidoRepository.findById(idDetalle)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado"));
                
        detalle.setNotas(nota);
        return detallesPedidoRepository.save(detalle);
    }

    // Guarda los cambios realizados en un detalle de pedido específico (Ej. cambiar su estado a completado).
    public DetallesPedido actualizarDetalle(DetallesPedido detalle) {
        return detallesPedidoRepository.save(detalle);
    }
}