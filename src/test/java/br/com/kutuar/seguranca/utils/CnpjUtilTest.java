package br.com.kutuar.seguranca.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CnpjUtilTest {
    @Test
    void normalizaCnpjComPontuacao() {
        assertEquals("12345678000190", CnpjUtil.normalizar("12.345.678/0001-90"));
    }

    @Test
    void preservaNulo() {
        assertNull(CnpjUtil.normalizar(null));
    }
}
