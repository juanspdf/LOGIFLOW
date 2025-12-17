package espe.edu.ec.fleet_service;

import espe.edu.ec.fleet_service.model.validator.CedulaEcuadorValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CedulaValidatorTest {

    private final CedulaEcuadorValidator validator = new CedulaEcuadorValidator();

    @Test
    public void testCedulaValida() {
        String cedulaValida = "1710034065";
        Assertions.assertTrue(validator.isValid(cedulaValida, null));
    }

    @Test
    public void testCedulaInvalidaDigitoVerificador() {
        String cedulaMala = "1710034066";
        Assertions.assertFalse(validator.isValid(cedulaMala, null));
    }

    @Test
    public void testCedulaLongitudIncorrecta() {
        Assertions.assertFalse(validator.isValid("123", null));
        Assertions.assertFalse(validator.isValid("123456789012", null));
    }

    @Test
    public void testCedulaNula() {
        Assertions.assertFalse(validator.isValid(null, null));
    }

    @Test
    public void testCedulaConLetras() {
        Assertions.assertFalse(validator.isValid("171003406A", null));
    }

    @Test
    public void testProvinciaInvalida() {
        Assertions.assertFalse(validator.isValid("9910034065", null));
    }
}