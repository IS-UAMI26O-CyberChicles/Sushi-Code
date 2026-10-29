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

// Clase encargada de poblar la base de datos H2 en memoria con datos de prueba
// extraídos del menú de Ryho Sushi al arrancar la aplicación.
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
        // Se declaran las variables principales para vinculación con las órdenes de prueba
        Platillo p1 = null;
        Platillo p2 = null;
        Platillo p3 = null;
        Platillo p4 = null;
        Platillo p5 = null;
        
        // Carga de platillos de prueba ordenados por categorías del menú
        if (platilloRepository.count() == 0) {
            System.out.println("Iniciando carga de platillos de prueba de Ryho Sushi en H2...");

            // --- CATEGORÍA: ROLLOS ESPECIALES ("Rollos especiales") ---
            p1 = new Platillo("Evi Roll", 103, "Rollos especiales", "Camarón empanizado y aguacate, queso crema y tampico encima.");
            platilloRepository.save(p1);

            platilloRepository.save(new Platillo("Niku Roll", 103, "Rollos especiales", "Carne asada, queso manchego y aguacate. Finas laminas de filete de res, encima empanizado, encima chipotle."));
            platilloRepository.save(new Platillo("Pollito Roll", 103, "Rollos especiales", "Pollo empanizado, aguacate, queso crema. Rollo envuelto en alga nori, encima queso manchego gratinado y salsa chipotle."));
            platilloRepository.save(new Platillo("Okinawa Roll", 103, "Rollos especiales", "Plátano capeado, queso crema, salmón y cangrejo. Queso manchego y aguacate, encima tampico y chipotle. Agridulce."));
            platilloRepository.save(new Platillo("Kaiu Roll", 103, "Rollos especiales", "Cebolla, aguacate y camarón. Cebolla asada con salsa chipotle."));
            platilloRepository.save(new Platillo("Avocado Roll", 103, "Rollos especiales", "Camarón empanizado. Aguacate salsa kushiage tampico."));
            platilloRepository.save(new Platillo("Voy Roll", 103, "Rollos especiales", "Rollo capeado aderezado con tampico y chipotle. Salmón, aguacate y cangrejo."));
            platilloRepository.save(new Platillo("Masago Roll", 103, "Rollos especiales", "Crema queso y masago. Salmón, aguacate y cangrejo."));
            platilloRepository.save(new Platillo("Kani Roll", 103, "Rollos especiales", "Queso crema y surimi, rollo capeado encima aderezo tampico y toque picante. Salmón, queso crema y aguacate."));
            platilloRepository.save(new Platillo("Kakiage Roll", 103, "Rollos especiales", "Queso crema y kakiage. Sumiri, aguacate y pepino."));
            platilloRepository.save(new Platillo("Sake Roll", 103, "Rollos especiales", "Queso crema y pepino. Rollo cubierto con finos de salmón fresco."));
            platilloRepository.save(new Platillo("Patriarca Roll", 103, "Rollos especiales", "Queso crema y pepino. Salmón semi cocido y tampico."));
            platilloRepository.save(new Platillo("Pintado Roll", 103, "Rollos especiales", "Camarón empanizado y aguacate. Queso crema servido con tampico."));

            // --- CATEGORÍA: ROLLOS TRADICIONALES Y BÁSICOS ("Rollos") ---
            p2 = new Platillo("California Roll", 83, "Rollos", "Camarón, aguacate y pepino.");
            platilloRepository.save(p2);

            platilloRepository.save(new Platillo("Filadelfia Roll", 83, "Rollos", "Queso, pepino y salmón fresco."));
            platilloRepository.save(new Platillo("Vegetariano Roll", 83, "Rollos", "Queso crema, aguacate, pepino y zanahoria."));
            platilloRepository.save(new Platillo("Tampico Roll", 83, "Rollos", "Queso crema, aguacate y cangrejo. Tampico y ajonjolí."));
            platilloRepository.save(new Platillo("Chizu Roll", 83, "Rollos", "Cangrejo, pepino y aguacate. Queso crema y ajonjolí."));
            platilloRepository.save(new Platillo("Tango Roll", 93, "Rollos", "Camarón empanizado, queso crema, mango y salsa de tamarindo."));
            platilloRepository.save(new Platillo("Puratan Roll", 93, "Rollos", "Camarón empanizado y aguacate. Queso crema, plátano frito y salsa de chipotle."));
            platilloRepository.save(new Platillo("Kapa Roll", 93, "Rollos", "Camarón empanizado y pepino, mango y aguacate, encima salsa dulce y chipotle, queso crema."));
            platilloRepository.save(new Platillo("Hawai Roll", 93, "Rollos", "Salmón y aguacate. Queso crema, piña y salsa de anguila."));
            platilloRepository.save(new Platillo("Confrutal Roll", 93, "Rollos", "Aguacate, pepino. Mango, fresa, kiwi, queso philadelphia, cubierto en salsa de tamarindo."));
            platilloRepository.save(new Platillo("Arigato Roll", 93, "Rollos", "Cangrejo empanizado y aguacate. Queso crema y salsa de tamarindo."));
            platilloRepository.save(new Platillo("Manchego Roll", 93, "Rollos", "Cangrejo empanizado y aguacate. Queso manchego gratinado y chipotle."));
            platilloRepository.save(new Platillo("Nanu Roll", 93, "Rollos", "Pepino, aguacate y salmón. Mango y salsa de tamarindo."));

            // --- CATEGORÍA: PLANCHA ("Plancha") ---
            p3 = new Platillo("Yakimeshi Mixto", 72, "Plancha", "Arroz frito con verdura, pollo, carne y camarón.");
            platilloRepository.save(p3);

            platilloRepository.save(new Platillo("Yakimeshi de Verduras", 60, "Plancha", "Arroz frito tradicional con verduras."));
            platilloRepository.save(new Platillo("Yakimeshi Mixto con Ajo", 78, "Plancha", "Arroz frito mixto con un toque de ajo."));
            platilloRepository.save(new Platillo("Yakimeshi Mixto con Tampico", 83, "Plancha", "Arroz frito mixto cubierto con tampico."));
            platilloRepository.save(new Platillo("Teppan de Verduras", 90, "Plancha", "Variedad de verduras asadas a la plancha."));
            platilloRepository.save(new Platillo("Teppan de Pollo", 100, "Plancha", "Pollo asado a la plancha con verduras."));
            platilloRepository.save(new Platillo("Teppan de Filete", 100, "Plancha", "Filete de res asado a la plancha con verduras."));
            platilloRepository.save(new Platillo("Teppan de Camarón", 140, "Plancha", "Camarón asado a la plancha con verduras."));
            platilloRepository.save(new Platillo("Cielo, Mar y Tierra", 140, "Plancha", "Combinación a la plancha de pollo, filete y camarón."));

            // --- CATEGORÍA: BEBIDAS ("Bebidas") ---
            p4 = new Platillo("Limonada Mineral", 45, "Bebidas", "Limonada fresca con agua mineral.");
            platilloRepository.save(p4);

            platilloRepository.save(new Platillo("Agua de Piñacoco", 30, "Bebidas", "Agua fresca de piña con coco."));
            platilloRepository.save(new Platillo("Agua de Mango", 30, "Bebidas", "Agua fresca de mango."));
            platilloRepository.save(new Platillo("Agua Fresca", 30, "Bebidas", "Agua fresca del día."));
            platilloRepository.save(new Platillo("Coca-Cola", 27, "Bebidas", "Refresco de cola de 355ml."));
            platilloRepository.save(new Platillo("Sprite", 27, "Bebidas", "Refresco de lima-limón de 355ml."));
            platilloRepository.save(new Platillo("Sidral", 27, "Bebidas", "Refresco sabor manzana de 355ml."));
            platilloRepository.save(new Platillo("Sangría", 27, "Bebidas", "Bebida sabor sangría."));
            platilloRepository.save(new Platillo("Calpis Natural", 40, "Bebidas", "Bebida tradicional japonesa de leche cultivada."));
            platilloRepository.save(new Platillo("Calpis de Mango", 45, "Bebidas", "Calpis sabor mango."));
            platilloRepository.save(new Platillo("Calpis de Fresa", 45, "Bebidas", "Calpis sabor fresa."));
            platilloRepository.save(new Platillo("Calpis de Piña", 45, "Bebidas", "Calpis sabor piña."));

            // --- CATEGORÍA: POSTRES ("Postres") ---
            p5 = new Platillo("Pastel de Matcha", 90, "Postres", "Delicioso pastel esponjoso de té verde japonés.");
            platilloRepository.save(p5);

            platilloRepository.save(new Platillo("Helado Tempura", 85, "Postres", "Bola de helado empanizada y frita."));
            platilloRepository.save(new Platillo("Banana Tempura", 75, "Postres", "Plátano frito en pasta crujiente con jarabe."));
            platilloRepository.save(new Platillo("Mochi de Fresa", 65, "Postres", "Pastelito de arroz japonés relleno de helado."));
            platilloRepository.save(new Platillo("Cheesecake Oriental", 95, "Postres", "Pay de queso estilo japonés con toque de vainilla."));

            System.out.println("¡Carga de platillos del menú completada con éxito!");
        }

        // Fabricación artificial de pedidos activos
        if(pedidoRepository.count() == 0){
            if(platilloRepository.count() == 0){
                p1 = new Platillo("Evi Roll", 103, "Rollos esp.", "Camarón empanizado y aguacate, queso crema y tampico encima.");
                platilloRepository.save(p1);
                p2 = new Platillo("California Roll", 83, "Rollos", "Camarón, aguacate y pepino.");
                platilloRepository.save(p2);
                p3 = new Platillo("Yakimeshi Mixto", 72, "Plancha", "Arroz frito con verdura, pollo, carne y camarón.");
                platilloRepository.save(p3);
                p4 = new Platillo("Limonada Mineral", 45, "Bebidas", "Limonada fresca con agua mineral.");
                platilloRepository.save(p4);
                p5 = new Platillo("Pastel de Matcha", 90, "Postres", "Delicioso pastel esponjoso de té verde japonés.");
                platilloRepository.save(p5);
            }

            // Creamos y guardamos clientes
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
            
            // Asignamos clientes a los pedidos
            pedido1.setCliente(c1);
            pedido2.setCliente(c2);
            pedido3.setCliente(c1);
            pedido4.setCliente(c2);
            pedido5.setCliente(c1);
            pedido6.setCliente(c2);

            // Guardamos el pedido en el repositorio
            pedido1 = pedidoRepository.save(pedido1);
            pedido2 = pedidoRepository.save(pedido2);
            pedido3 = pedidoRepository.save(pedido3);
            pedido4 = pedidoRepository.save(pedido4);
            pedido5 = pedidoRepository.save(pedido5);
            pedido6 = pedidoRepository.save(pedido6);

            // Creamos los detalles y los asignamos a los pedidos
            DetallesPedido det1 = new DetallesPedido(2, "Sin ajonjolí", 206, p1, pedido1);
            det1 = detallesPedidoRepository.save(det1);
            DetallesPedido det2 = new DetallesPedido(2, "Extra salsa agridulce", 144, p3, pedido1);
            det2 = detallesPedidoRepository.save(det2);
            DetallesPedido det3 = new DetallesPedido(2, null, 166, p2, pedido1);
            det3 = detallesPedidoRepository.save(det3);
            List<DetallesPedido> detalles = new ArrayList<>();
            detalles.add(det1);
            detalles.add(det2);
            detalles.add(det3);
            pedido1.setDetallesPedido(detalles);

            DetallesPedido det4 = new DetallesPedido(1, null, 83, p2, pedido2);
            det4 = detallesPedidoRepository.save(det4);
            DetallesPedido det5 = new DetallesPedido(1, null, 72, p3, pedido2);
            det5 = detallesPedidoRepository.save(det5);
            List<DetallesPedido> detalles2 = new ArrayList<>();
            detalles2.add(det4);
            detalles2.add(det5);
            pedido2.setDetallesPedido(detalles2);

            DetallesPedido det6 = new DetallesPedido(3, null, 135, p4, pedido3);
            det6 = detallesPedidoRepository.save(det6);
            List<DetallesPedido> detalles3 = new ArrayList<>();
            detalles3.add(det6);
            pedido3.setDetallesPedido(detalles3);

            DetallesPedido det7 = new DetallesPedido(2, null, 206, p1, pedido4);
            det7 = detallesPedidoRepository.save(det7);
            DetallesPedido det8 = new DetallesPedido(1, null, 45, p4, pedido4);
            det8 = detallesPedidoRepository.save(det8);
            List<DetallesPedido> detalles4 = new ArrayList<>();
            detalles4.add(det7);
            detalles4.add(det8);
            pedido4.setDetallesPedido(detalles4);

            DetallesPedido det9 = new DetallesPedido(4, null, 332, p2, pedido5);
            det9 = detallesPedidoRepository.save(det9);
            List<DetallesPedido> detalles5 = new ArrayList<>();
            detalles5.add(det9);
            pedido5.setDetallesPedido(detalles5);

            DetallesPedido det10 = new DetallesPedido(1, null, 72, p3, pedido6);
            det10 = detallesPedidoRepository.save(det10);
            DetallesPedido det11 = new DetallesPedido(1, null, 83, p2, pedido6);
            det11 = detallesPedidoRepository.save(det11);
            List<DetallesPedido> detalles6 = new ArrayList<>();
            detalles6.add(det10);
            detalles6.add(det11);
            pedido6.setDetallesPedido(detalles6);

            // Actualizamos los pedidos con sus detalles
            pedido1 = pedidoRepository.save(pedido1);
            pedido2 = pedidoRepository.save(pedido2);
            pedido3 = pedidoRepository.save(pedido3);
            pedido4 = pedidoRepository.save(pedido4);
            pedido5 = pedidoRepository.save(pedido5);
            pedido6 = pedidoRepository.save(pedido6);

            System.out.println("Se guardaron los clientes, pedidos y detalles pedidos correctamente.");
        }

        // Carga de mesas de prueba
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