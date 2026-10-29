package mx.uam.ayd.proyecto.negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de {@link UtilidadPassword}.
 */
class UtilidadPasswordTest {

    @Test
    @DisplayName("SHA-256 de \"abc\" coincide con el valor de referencia conocido")
    void encriptarProduceElHashEsperado() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                UtilidadPassword.encriptar("abc"));
    }

    @Test
    @DisplayName("El hash siempre tiene 64 caracteres hexadecimales")
    void hashTieneLongitudFija() {
        assertEquals(64, UtilidadPassword.encriptar("a").length());
        assertEquals(64, UtilidadPassword.encriptar("Admin123!").length());
    }

    @Test
    @DisplayName("Contraseñas distintas producen hashes distintos")
    void contrasenasDistintasHashesDistintos() {
        assertNotEquals(UtilidadPassword.encriptar("Admin123!"), UtilidadPassword.encriptar("admin123!"));
    }

    @Test
    @DisplayName("coincide() acepta la contraseña correcta y rechaza la incorrecta")
    void coincideComparaContraElHash() {
        String hash = UtilidadPassword.encriptar("Admin123!");

        assertTrue(UtilidadPassword.coincide("Admin123!", hash));
        assertFalse(UtilidadPassword.coincide("Admin123?", hash));
    }

    @Test
    @DisplayName("coincide() con valores nulos devuelve false sin lanzar excepción")
    void coincideConNulos() {
        assertFalse(UtilidadPassword.coincide(null, "hash"));
        assertFalse(UtilidadPassword.coincide("clave", null));
    }

    @Test
    @DisplayName("encriptar(null) lanza IllegalArgumentException")
    void encriptarNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> UtilidadPassword.encriptar(null));
    }
}