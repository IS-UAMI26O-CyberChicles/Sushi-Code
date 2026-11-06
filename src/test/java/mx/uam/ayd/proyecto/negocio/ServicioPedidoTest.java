package mx.uam.ayd.proyecto.negocio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import mx.uam.ayd.proyecto.negocio.modelo.DetallesPedido;
import mx.uam.ayd.proyecto.negocio.modelo.Pedido;
import mx.uam.ayd.proyecto.datos.DetallesPedidoRepository;
import mx.uam.ayd.proyecto.datos.PedidoRepository;
/**
 * Pruebas unitarias para validar las reglas de negocio de ServicioPedido.
 */
class ServicioPedidoTest {

    @InjectMocks
    private ServicioPedido servicioPedido;

    // repositorio falso con @Mock
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private DetallesPedidoRepository detallesPedidoRepository;

    @BeforeEach
    void setUp() {
        // Inicializamos Mockito para que reconozca el repositorio
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testValidarDatosDomicilio_Exitoso() {
        // Escenario 1: Pedido a domicilio exitoso (Todos los datos correctos)
        boolean resultado = servicioPedido.validarDatosDomicilio("Hiroshi Tanaka", "5512345678", "Calle 123");
        assertTrue(resultado, "El sistema debería aceptar los datos válidos.");
    }

    @Test
    void testValidarDatosDomicilio_FaltaDireccion() {
        // Escenario 2: Bloqueo por falta de dirección (RN-02)
        boolean resultado = servicioPedido.validarDatosDomicilio("Hiroshi Tanaka", "5512345678", "");
        assertFalse(resultado, "El sistema debería rechazar el pedido si falta la dirección.");
    }

    @Test
    void testValidarDatosDomicilio_TelefonoInvalido() {
        // Escenario 2 variante: Bloqueo porque el teléfono no tiene 10 dígitos (RN-02)
        boolean resultado = servicioPedido.validarDatosDomicilio("Hiroshi Tanaka", "12345", "Calle 123");
        assertFalse(resultado, "El sistema debería rechazar el pedido si el teléfono no tiene 10 dígitos.");
        
        // Bloqueo porque el teléfono tiene letras
        boolean resultadoLetras = servicioPedido.validarDatosDomicilio("Hiroshi Tanaka", "55ABC45678", "Calle 123");
        assertFalse(resultadoLetras, "El sistema debería rechazar el pedido si el teléfono contiene letras.");
    }

    
    // PRUEBAS HU-15/HU-03: Envio a Cocina)
    // BLOQUE EL ENVIO SI LA ORDEN FUE CANCELADA
    @Test
    void testProcesarEnvioCocina_FallaCuandoPedidoEstaCancelado() {
        long idPedido = 1L;
        
        Pedido pedidoCancelado = new Pedido();
        pedidoCancelado.setIdPedido(idPedido);
        pedidoCancelado.setEstado("Cancelada");
        pedidoCancelado.setTipoOrden("Local");

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoCancelado));

        Exception excepcion = assertThrows(IllegalStateException.class, () -> {
            servicioPedido.procesarEnvioCocina(idPedido, 0.0);
        });

        assertEquals("Esta orden ha sido cancelada y no puede enviarse a cocina.", excepcion.getMessage());
    }

    //Valida el flujo exitoso (camino feliz) de envío de un pedido local a cocina.
    @Test
    void testProcesarEnvioCocina_ExitoYCambioDeEstado() {
    long idPedido = 2L;
    
    // Configuración completa del pedido válido
    Pedido pedidoValido = new Pedido();
    pedidoValido.setIdPedido(idPedido);
    pedidoValido.setEstado("Pendiente"); 
    pedidoValido.setTipoOrden("Local");

    // Mocks para buscar y guardar el pedido en el repositorio
    when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoValido));
    when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Ejecución
    boolean resultado = servicioPedido.procesarEnvioCocina(idPedido, 0.0);

    // Verificaciones
    assertTrue(resultado, "El método debe devolver true si el envío fue exitoso.");
    assertEquals("En Preparación", pedidoValido.getEstado(), "El estado del pedido debió cambiar.");
    
    // Permite que save() se haya ejecutado al menos una vez
    verify(pedidoRepository, atLeastOnce()).save(pedidoValido);
}
  

    // PRUEBAS (HU-04: Cancela un Pedido)
    // PRUEBA JUSTIFICACION OBLIGATORIA 
    @Test
    void testCancelarPedido_FallaCuandoMotivoEstaVacio() {
        long idPedido = 1L;
        String motivoVacio = "   "; // MOTIVO INVALIDO
        String idUsuario = "Cajero01";
        // LANZA EL ERROR 
        Exception excepcion = assertThrows(IllegalArgumentException.class, () -> {
            servicioPedido.cancelarPedido(idPedido, motivoVacio, idUsuario);
        });

        assertEquals("El motivo de cancelación es obligatorio.", excepcion.getMessage());
    }

    // ASEGURA QUE NO CANCELE ALGO QUE NO EXISTE 
    @Test
    void testCancelarPedido_FallaCuandoPedidoNoExiste() {
        long idFalso = 99L;
        // SIMULA QUE NO ENCUENTRA EL PEDIDO
        when(pedidoRepository.findById(idFalso)).thenReturn(Optional.empty());

        Exception excepcion = assertThrows(IllegalArgumentException.class, () -> {
            servicioPedido.cancelarPedido(idFalso, "Ya no lo quiere", "Cajero01");
        });

        assertEquals("No se encontró el pedido con ID: " + idFalso, excepcion.getMessage());
    }

    // VERIFICA QUE EL ESTADO CAMBIE Y GUARDE EL FORMATO
    @Test
    void testCancelarPedido_ExitoYGuardaMotivoCorrectamente() {
        long idPedido = 2L;
        //PEDIDO VALIDO
        Pedido pedidoValido = new Pedido();
        pedidoValido.setIdPedido(idPedido);
        pedidoValido.setEstado("Pendiente"); 
        // EJECUTA LA CANCELACION
        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoValido));

        boolean resultado = servicioPedido.cancelarPedido(idPedido, "Cliente se arrepintió", "Admin123");
        // VERIFICA EL ESTADO
        assertTrue(resultado, "El método debe devolver true tras cancelar con éxito.");
        assertEquals("Cancelada", pedidoValido.getEstado(), "El estado debió cambiar a Cancelada.");
        // VERIFICA QUE ESTA EL MOTIVO Y USUARIO
        assertEquals("Cliente se arrepintió (Cancelado por: Admin123)", pedidoValido.getMotivoCancelacion(), 
            "El motivo de cancelación no se guardó con el formato esperado.");
        verify(pedidoRepository, times(1)).save(pedidoValido);
    }

    @Test
    void guardaPedidoValido() {
        // Given
        Pedido pedido = new Pedido();
        pedido.setIdPedido(1L);
        pedido.setNumeroOrden(10);
        when(pedidoRepository.save(pedido)).thenReturn(pedido);
        // When
        Pedido resultado = servicioPedido.guardarPedido(pedido);
        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPedido());
        verify(pedidoRepository, times(1)).save(pedido);
    }

    @Test
    void guardaPedidoNulo() {
        // Given
        when(pedidoRepository.save(null)).thenThrow(IllegalArgumentException.class);

        // When
        Pedido resultado = servicioPedido.guardarPedido(null);

        // Then
        assertNull(resultado, "El método debe devolver null si el guardado falla");
}

    // PRUEBA ESPECÍFICA HU-15: Procesar Envío a Cocina con Costo de Envío (Domicilio) 
    @Test
    void testProcesarEnvioCocina_ConCostoEnvioDomicilio_Exito() {
        long idPedido = 3L;
        double costoEnvio = 50.0;

        Pedido pedidoDomicilio = new Pedido();
        pedidoDomicilio.setIdPedido(idPedido);
        pedidoDomicilio.setEstado("Pendiente");
        pedidoDomicilio.setTipoOrden("Domicilio");

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedidoDomicilio));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean resultado = servicioPedido.procesarEnvioCocina(idPedido, costoEnvio);

        assertTrue(resultado, "El envío a cocina con costo de flete debe ser exitoso.");
        assertEquals("En Preparación", pedidoDomicilio.getEstado(), "El estado debió actualizarse a 'En Preparación'.");
        
        // Verifica que se haya persistido en el repositorio
        verify(pedidoRepository, atLeastOnce()).save(pedidoDomicilio);
    }


    // ---------------- marcarComoEntregadoTest ----------------
    /**
     * Test para verificar cuando un pedido en forma es ingresado a la función
     * @author JLCB
     */
    @Test 
    void testMarcarComoEntregadoEvento(){
        Pedido pedido = new Pedido("Tipo", 0, 'a', null, "Completado", null, 0);
        pedido.setIdPedido(1); // Suponemos que ya estaba en la base de datos
        Pedido pedidoCambiado = new Pedido("Tipo", 0, 'a', null, "Entregado", null, 0);
        pedidoCambiado.setIdPedido(1);

        when(pedidoRepository.existsById(1l)).thenReturn(true);
        when(pedidoRepository.save(pedido)).thenReturn(pedidoCambiado);

        assertTrue(servicioPedido.marcarComoEntregado(pedido));
    }
    /**
     * Test para verificar cuando un evento que no existe, ya sea por ser nulo o por no estar registrado en el repositorio
     * @author JLCB
     */
    @Test
    void testMarcarComoEntregadoEventoQueNoExiste(){
        // Pedido nulo
        Pedido p1 = null;
        // Pedido que no existe en repositorio
        Pedido p2 = new Pedido("Tipo", 0, 'a', null, "Completado", null, 0);
        
        when(pedidoRepository.existsById(p2.getIdPedido())).thenReturn(false);

        assertFalse(servicioPedido.marcarComoEntregado(p1));
        assertFalse(servicioPedido.marcarComoEntregado(p2));
    }

    // ---------------- asignarDetallesAPedidoTest ----------------
    /**
     * Test para verificar que a un pedido nulo se regresa false
     * @author JLCB
     */
    @Test
    void testAsignarDetallesAPedidoNulo(){
        Pedido p = null;
        List<DetallesPedido> detalles = new ArrayList<>();
        DetallesPedido detalle = new DetallesPedido();
        detalles.add(detalle);

        when(pedidoRepository.save(p)).thenReturn(null);

        assertFalse(servicioPedido.asignarDetallesAPedido(p, null));
    }
    /**
     * Test para verificar si regresa false al mandar unos detalles vacíos
     * @author JLCB
     */
    @Test
    void testAsignarDetallesVaciosAPedido(){
        Pedido p = new Pedido();
        List<DetallesPedido> detalles = new ArrayList<>();

        when(pedidoRepository.save(p)).thenReturn(p);
        assertFalse(servicioPedido.asignarDetallesAPedido(p, detalles));
    }
    /**
     * Test para verificar que regrese verdadero al mandar un pedido y unos detalles correctos
     * @author JLCB
     */
    @Test
    void testAsignarDetallesConPedidoCorrecto(){
        Pedido p = new Pedido();
        List<DetallesPedido> detalles = new ArrayList<>();
        DetallesPedido d1 = new DetallesPedido();
        DetallesPedido d2 = new DetallesPedido();
        detalles.add(d1);
        detalles.add(d2);

        when(pedidoRepository.save(p)).thenReturn(p);
        when(detallesPedidoRepository.save(d1)).thenReturn(d1);
        when(detallesPedidoRepository.save(d2)).thenReturn(d2);

        assertTrue(servicioPedido.asignarDetallesAPedido(p, detalles));
    }



// -----------------------PRUEBAS HU-13: MIS ENTREGAS-----------------------------------

// CUANDO HAY PEDIDOS A ENTREGAR, DEBE REGRESAR LA LISTA DE PEDIDOS
@Test
    void testObtenerPedidosAEntregar_Exitoso() {
        // PREPARA EL ESCENARIO SIMULANDO UN PEDIDO A DOMICILIO COMPLETADO
        Pedido pedidoDomicilio = new Pedido();
        pedidoDomicilio.setIdPedido(10L);
        pedidoDomicilio.setTipoOrden("Domicilio");
        pedidoDomicilio.setEstado("Completado");

        List<Pedido> listaSimulada = new ArrayList<>();
        listaSimulada.add(pedidoDomicilio);

        when(pedidoRepository.findByTipoOrdenAndEstado("Domicilio", "Completado")).thenReturn(listaSimulada);

        // EJECUTA EL MÉTODO A PROBAR
        List<Pedido> resultado = servicioPedido.obtenerPedidosAEntregar();

        // VERIFICA QUE EL RESULTADO SEA EL ESPERADO
        assertNotNull(resultado, "La lista de pedidos no debe ser nula");
        assertEquals(1, resultado.size(), "Debe retornar exactamente 1 pedido a entregar");
        assertEquals("Completado", resultado.get(0).getEstado());
        assertEquals("Domicilio", resultado.get(0).getTipoOrden());
        verify(pedidoRepository, times(1)).findByTipoOrdenAndEstado("Domicilio", "Completado");
    }
    
    // CUANDO NO HAY PEDIDOS A ENTREGAR, DEBE REGRESAR UNA LISTA VACÍA
    @Test
    void testObtenerPedidosAEntregar_ListaVacia() {
        // PREPARA EL ESCENARIO SIMULANDO QUE NO HAY PEDIDOS A DOMICILIO COMPLETADOS
        when(pedidoRepository.findByTipoOrdenAndEstado("Domicilio", "Completado")).thenReturn(new ArrayList<>());

        // EJECUTA EL MÉTODO A PROBAR
        List<Pedido> resultado = servicioPedido.obtenerPedidosAEntregar();

        // VERIFICA QUE EL RESULTADO SEA UNA LISTA VACÍA
        assertNotNull(resultado, "No debe regresar null si no hay pedidos pendientes");
        assertTrue(resultado.isEmpty(), "La lista debe estar vacía si no hay pedidos a domicilio completados");
        verify(pedidoRepository, times(1)).findByTipoOrdenAndEstado("Domicilio", "Completado");
    }
}