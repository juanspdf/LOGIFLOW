package espe.edu.ec.fleet_service;

import espe.edu.ec.fleet_service.model.EstadoVehiculo;
import espe.edu.ec.fleet_service.model.Vehiculo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class VehiculoTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static class VehiculoConcreto extends Vehiculo {
    }

    @Test
    public void testGettersAndSetters() {
        VehiculoConcreto v = new VehiculoConcreto();
        v.setId(1L);
        v.setColor("Rojo");
        v.setCilindraje(2000);

        Assertions.assertEquals(1L, v.getId());
        Assertions.assertEquals("Rojo", v.getColor());
        Assertions.assertEquals(2000, v.getCilindraje());
    }

    @Test
    public void testValidacionPlacaCorrecta() {
        VehiculoConcreto v = crearVehiculoValido();
        v.setPlaca("PBA-1234");

        Set<ConstraintViolation<VehiculoConcreto>> violaciones = validator.validate(v);
        Assertions.assertTrue(violaciones.isEmpty(), "No debería haber errores con placa válida");
    }

    @Test
    public void testValidacionPlacaIncorrecta() {
        VehiculoConcreto v = crearVehiculoValido();
        v.setPlaca("pba-123");

        Set<ConstraintViolation<VehiculoConcreto>> violaciones = validator.validate(v);
        Assertions.assertFalse(violaciones.isEmpty(), "Debe fallar si la placa es minúscula");

        String mensaje = violaciones.iterator().next().getMessage();
        Assertions.assertEquals("La placa debe tener formato AAA-123 o AAA-1234", mensaje);
    }

    @Test
    public void testValidacionAnioIncorrecto() {
        VehiculoConcreto v = crearVehiculoValido();
        v.setAnioFabricacion("1800");

        Set<ConstraintViolation<VehiculoConcreto>> violaciones = validator.validate(v);
        Assertions.assertFalse(violaciones.isEmpty());
        Assertions.assertEquals("El año debe ser válido (ej: 1999, 2024)", violaciones.iterator().next().getMessage());
    }

    @Test
    public void testValidacionCilindrajeMaximo() {
        VehiculoConcreto v = crearVehiculoValido();
        v.setCilindraje(20000);

        Set<ConstraintViolation<VehiculoConcreto>> violaciones = validator.validate(v);
        Assertions.assertFalse(violaciones.isEmpty());
        Assertions.assertEquals("El cilindraje incorrecto máx 10000", violaciones.iterator().next().getMessage());
    }

    private VehiculoConcreto crearVehiculoValido() {
        VehiculoConcreto v = new VehiculoConcreto();
        v.setCilindraje(1600);
        v.setPlaca("PBA-1234");
        v.setMarca("Toyota");
        v.setColor("Negro");
        v.setModelo("Corolla");
        v.setAnioFabricacion("2022");
        v.setActivo(true);
        v.setEstado(EstadoVehiculo.DISPONIBLE);
        return v;
    }
}