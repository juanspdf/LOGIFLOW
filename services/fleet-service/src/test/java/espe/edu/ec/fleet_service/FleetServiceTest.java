package espe.edu.ec.fleet_service;

import espe.edu.ec.fleet_service.model.EstadoVehiculo;
import espe.edu.ec.fleet_service.model.Moto;
import espe.edu.ec.fleet_service.model.Repartidor;
import espe.edu.ec.fleet_service.model.Vehiculo;
import espe.edu.ec.fleet_service.repository.RepartidorRepository;
import espe.edu.ec.fleet_service.repository.VehiculoRepository;
import espe.edu.ec.fleet_service.service.FleetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class FleetServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private RepartidorRepository repartidorRepository;

    @InjectMocks
    private FleetService fleetService;

    @Test
    @SuppressWarnings("null")
    public void testCrearVehiculo() {
        Moto moto = new Moto();
        moto.setPlaca("M-001");
        moto.setTipoVehiculo("MOTO");

        when(vehiculoRepository.existsByPlaca("M-001")).thenReturn(false);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(i -> {
            @SuppressWarnings("null")
            var v = (Vehiculo) i.getArguments()[0];
            return v;
        });

        @SuppressWarnings("null")
        Vehiculo resultado = fleetService.crearVehiculo(moto);

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("M-001", resultado.getPlaca());
        Assertions.assertEquals(EstadoVehiculo.DISPONIBLE, resultado.getEstado());
    }

    @Test
    public void testListarVehiculos() {
        when(vehiculoRepository.findAll()).thenReturn(Arrays.asList(new Moto(), new Moto()));
        List<Vehiculo> lista = fleetService.listarVehiculos();
        Assertions.assertEquals(2, lista.size());
    }

    @Test
    @SuppressWarnings("null")
    public void testAsignarVehiculoARepartidor() {
        Long repId = 1L;
        String placa = "ABC-123";

        Repartidor repartidor = new Repartidor();
        repartidor.setId(repId);
        repartidor.setNombre("Juan");

        Moto vehiculo = new Moto();
        vehiculo.setPlaca(placa);

        when(repartidorRepository.findById(repId)).thenReturn(Optional.of(repartidor));
        when(vehiculoRepository.findByPlaca(placa)).thenReturn(Optional.of(vehiculo));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(i -> {
            @SuppressWarnings("null")
            var r = (Repartidor) i.getArguments()[0];
            return r;
        });

        @SuppressWarnings("null")
        Repartidor resultado = fleetService.asignarVehiculo(repId, placa);

        Assertions.assertNotNull(resultado.getVehiculo());
        Assertions.assertEquals(placa, resultado.getVehiculo().getPlaca());
    }
}