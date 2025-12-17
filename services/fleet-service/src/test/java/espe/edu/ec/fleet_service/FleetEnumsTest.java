package espe.edu.ec.fleet_service;

import espe.edu.ec.fleet_service.model.AutoType;
import espe.edu.ec.fleet_service.model.TipoLicencia;
import espe.edu.ec.fleet_service.model.TipoVehiculo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FleetEnumsTest {

    @Test
    public void testAutoType() {
        Assertions.assertNotNull(AutoType.SEDAN);
        Assertions.assertNotNull(AutoType.SUV);
        Assertions.assertEquals(7, AutoType.values().length);
        Assertions.assertEquals(AutoType.COUPE, AutoType.valueOf("COUPE"));
    }

    @Test
    public void testTipoLicencia() {
        Assertions.assertNotNull(TipoLicencia.A);
        Assertions.assertNotNull(TipoLicencia.B);
        Assertions.assertEquals(4, TipoLicencia.values().length);
        Assertions.assertEquals(TipoLicencia.E, TipoLicencia.valueOf("E"));
    }

    @Test
    public void testTipoVehiculo() {
        Assertions.assertNotNull(TipoVehiculo.MOTO);
        Assertions.assertNotNull(TipoVehiculo.LIVIANO);
        Assertions.assertNotNull(TipoVehiculo.CAMION);
        Assertions.assertEquals(3, TipoVehiculo.values().length);
        Assertions.assertEquals(TipoVehiculo.LIVIANO, TipoVehiculo.valueOf("LIVIANO"));
    }
}