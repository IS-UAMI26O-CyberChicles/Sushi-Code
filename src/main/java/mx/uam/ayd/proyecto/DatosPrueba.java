package mx.uam.ayd.proyecto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import mx.uam.ayd.proyecto.datos.ClienteRepository;
import mx.uam.ayd.proyecto.datos.DetallesPedidoRepository;
import mx.uam.ayd.proyecto.datos.EmpleadoRepository;
import mx.uam.ayd.proyecto.datos.MesaRepository;
import mx.uam.ayd.proyecto.datos.PedidoRepository;
import mx.uam.ayd.proyecto.datos.PlatilloRepository;
import mx.uam.ayd.proyecto.negocio.modelo.Cliente;
import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Empleado;
import mx.uam.ayd.proyecto.negocio.modelo.Mesa;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;
import mx.uam.ayd.proyecto.negocio.modelo.Platillo;

/**
 * Clase encargada de poblar la base de datos H2 en memoria con datos de prueba
 * cada vez que arranca la aplicación.
 */
@Component
public class DatosPrueba implements CommandLineRunner {

    @Autowired
    private PlatilloRepository platilloRepository;
    @Autowired 
    private PedidoRepository pedidoRepository;
    @Autowired 
    private DetallesPedidoRepository detallesPedidoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private MesaRepository mesaRepository;
    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Override
    public void run(String... args) throws Exception {
        Platillo p1 = null;
        Platillo p2 = null;
        Platillo p3 = null;
        Platillo p4 = null;
        Platillo p5 = null;
        
        // Carga de platillos de prueba
        if (platilloRepository.count() == 0) {
            System.out.println("Iniciando carga de platillos de prueba en H2...");

            p1 = new Platillo();
            p1.setNombre("Rollo Mar y Tierra");
            p1.setPrecio(120);
            p1.setTipoArea("Rollos");
            p1.setDescripcion("Rollo empanizado con camarón, res y aguacate.");
            platilloRepository.save(p1);

            p2 = new Platillo();
            p2.setNombre("Nigiri de Salmón");
            p2.setPrecio(85);
            p2.setTipoArea("Rollos");
            p2.setDescripcion("Dos piezas tradicionales de salmón fresco.");
            platilloRepository.save(p2);

            p3 = new Platillo();
            p3.setNombre("Yakimeshi Mixto");
            p3.setPrecio(150);
            p3.setTipoArea("Plancha");
            p3.setDescripcion("Arroz frito con pollo, res y vegetales.");
            platilloRepository.save(p3);
            
            p4 = new Platillo();
            p4.setNombre("Limonada Mineral");
            p4.setPrecio(45);
            p4.setTipoArea("Bebidas");
            p4.setDescripcion("Limonada fresca con agua mineral.");
            platilloRepository.save(p4);

            p5 = new Platillo();
            p5.setNombre("Pastel de Matcha");
            p5.setPrecio(90);
            p5.setTipoArea("Postres");
            p5.setDescripcion("Delicioso pastel esponjoso de té verde japonés.");
            platilloRepository.save(p5);

            System.out.println("¡Carga de platillos completada!");
        }

        // Carga de empleados de prueba (Tu rama CYB-3)
        if (empleadoRepository.count() == 0) {
            System.out.println("Iniciando carga de empleados de prueba en H2...");

            empleadoRepository.save(new Empleado("Daiki Nakamura", "daiki.n", "ClaveSegura1!", "Cocinero"));
            empleadoRepository.save(new Empleado("Aoi Tanaka", "aoi.tanaka", "ClaveSegura1!", "Administrador"));
            empleadoRepository.save(new Empleado("Ren Takahashi", "ren.t", "ClaveSegura1!", "Recepcionista"));
            empleadoRepository.save(new Empleado("Haruto Sato", "haruto.s", "ClaveSegura1!", "Repartidor"));

            System.out.println("¡Carga de empleados completada!");
        }

        // Fabricación artificial de pedidos activos (Rama master)
        if(pedidoRepository.count() == 0){
            if(platilloRepository.count() == 0){
                p1 = new Platillo("Rollos de Mar y Tierra", 120, "Rollos", "Rollo empanizado con camarón, res y aguacate.");
                platilloRepository.save(p1);
                p2 = new Platillo("Nigiri de Salmón", 85, "Rollos", "Dos piezas tradicionales de salmón fresco.");
                platilloRepository.save(p2);
                p3 = new Platillo("Yakimeshi Mixto", 150, "Plancha", "Arroz frito con pollo, res y vegetales.");
                platilloRepository.save(p3);
                p4 = new Platillo("Limonada Mineral", 45, "Bebidas", "Limonada fresca con agua mineral.");
                platilloRepository.save(p4);
            }

            Cliente c1 = new Cliente("Alberto", "124567891", "Dirección 1");
            Cliente c2 = new Cliente("Julio", "0321654987", "Dirección 2");
            clienteRepository.save(c1);
            clienteRepository.save(c2);

            // Creamos pedidos
            Pedido pedido1 = new Pedido("Domicilio", 12345, 'a', "Servicios", "Completado", null, 100);
            Pedido pedido2 = new Pedido("Recoger", 12346, 'b', "Cocina", "Completado", null, 250);
            Pedido pedido3 = new Pedido("Recoger", 12347, 'a', "Servicios", "Completado", null, 150);
            Pedido pedido4 = new Pedido("Domicilio", 12348, 'c', "Servicios", "Completado", null, 300);
            Pedido pedido5 = new Pedido("Domicilio", 12349, 'a', "Cocina", "En Proceso", null, 400);
            Pedido pedido6 = new Pedido("Recoger", 12350, 'b', "Servicios", "Cancelado", null, 200);

            
            pedido1.setCliente(c1);
            pedido2.setCliente(c2);
            pedido3.setCliente(c1);
            pedido4.setCliente(c2);
            pedido5.setCliente(c1);
            pedido6.setCliente(c2);

            pedido1 = pedidoRepository.save(pedido1);
            pedido2 = pedidoRepository.save(pedido2);
            pedido3 = pedidoRepository.save(pedido3);
            pedido4 = pedidoRepository.save(pedido4);
            pedido5 = pedidoRepository.save(pedido5);
            pedido6 = pedidoRepository.save(pedido6);

            DetallesPedido det1 = new DetallesPedido(2, "Nota 1. Prueba de impresión", 100, p1, pedido1);
            det1 = detallesPedidoRepository.save(det1);
            DetallesPedido det2 = new DetallesPedido(2, "nota", 100, p3, pedido1);
            det2 = detallesPedidoRepository.save(det2);
            DetallesPedido det3 = new DetallesPedido(2, null, 100, p2, pedido1);
            det3 = detallesPedidoRepository.save(det3);
            List<DetallesPedido> detalles = new ArrayList<>();
            detalles.add(det1);
            detalles.add(det2);
            detalles.add(det3);
            pedido1.setDetallesPedido(detalles);

            DetallesPedido det4 = new DetallesPedido(1, null, 150, p2, pedido2);
            det4 = detallesPedidoRepository.save(det4);
            DetallesPedido det5 = new DetallesPedido(1, null, 100, p3, pedido2);
            det5 = detallesPedidoRepository.save(det5);
            List<DetallesPedido> detalles2 = new ArrayList<>();
            detalles2.add(det4);
            detalles2.add(det5);
            pedido2.setDetallesPedido(detalles2);

            DetallesPedido det6 = new DetallesPedido(3, null, 50, p4, pedido3);
            det6 = detallesPedidoRepository.save(det6);
            List<DetallesPedido> detalles3 = new ArrayList<>();
            detalles3.add(det6);
            pedido3.setDetallesPedido(detalles3);

            DetallesPedido det7 = new DetallesPedido(2, null, 100, p1, pedido4);
            det7 = detallesPedidoRepository.save(det7);
            DetallesPedido det8 = new DetallesPedido(1, null, 200, p4, pedido4);
            det8 = detallesPedidoRepository.save(det8);
            List<DetallesPedido> detalles4 = new ArrayList<>();
            detalles4.add(det7);
            detalles4.add(det8);
            pedido4.setDetallesPedido(detalles4);

            DetallesPedido det9 = new DetallesPedido(4, null, 100, p2, pedido5);
            det9 = detallesPedidoRepository.save(det9);
            List<DetallesPedido> detalles5 = new ArrayList<>();
            detalles5.add(det9);
            pedido5.setDetallesPedido(detalles5);

            DetallesPedido det10 = new DetallesPedido(1, null, 100, p3, pedido6);
            det10 = detallesPedidoRepository.save(det10);
            DetallesPedido det11 = new DetallesPedido(1, null, 100, p2, pedido6);
            det11 = detallesPedidoRepository.save(det11);
            List<DetallesPedido> detalles6 = new ArrayList<>();
            detalles6.add(det10);
            detalles6.add(det11);
            pedido6.setDetallesPedido(detalles6);

            pedido1 = pedidoRepository.save(pedido1);
            pedido2 = pedidoRepository.save(pedido2);
            pedido3 = pedidoRepository.save(pedido3);
            pedido4 = pedidoRepository.save(pedido4);
            pedido5 = pedidoRepository.save(pedido5);
            pedido6 = pedidoRepository.save(pedido6);

            System.out.println("Se guardaron los clientes, pedidos y detalles pedidos");
        }

        // Carga de mesas de prueba (HU-05)
        if (mesaRepository.count() == 0) {
            System.out.println("Iniciando carga de mesas de prueba en H2...");

            mesaRepository.save(new Mesa(1, 4, "LIBRE"));
            mesaRepository.save(new Mesa(2, 4, "LIBRE"));
            mesaRepository.save(new Mesa(3, 6, "LIBRE"));
            mesaRepository.save(new Mesa(4, 4, "LIBRE"));
            mesaRepository.save(new Mesa(5, 4, "LIBRE"));

            System.out.println("¡Carga de mesas completada!");
        }

        // Carga de empleados de prueba
        if (empleadoRepository.count() == 0) {
            System.out.println("Iniciando carga de empleados de prueba en H2...");

            empleadoRepository.save(new Empleado("Daiki Nakamura", "daiki.n", "ClaveSegura1!", "Cocinero"));
            empleadoRepository.save(new Empleado("Aoi Tanaka", "aoi.tanaka", "ClaveSegura1!", "Administrador"));
            empleadoRepository.save(new Empleado("Ren Takahashi", "ren.t", "ClaveSegura1!", "Recepcionista"));
            empleadoRepository.save(new Empleado("Haruto Sato", "haruto.s", "ClaveSegura1!", "Repartidor"));

            System.out.println("¡Carga de empleados completada!");
        }

    }
}