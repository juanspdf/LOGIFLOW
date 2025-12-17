package espe.edu.ec.fleet_service.service;

import espe.edu.ec.fleet_service.model.EstadoVehiculo;
import espe.edu.ec.fleet_service.model.Repartidor;
import espe.edu.ec.fleet_service.model.Vehiculo;
import espe.edu.ec.fleet_service.repository.RepartidorRepository;
import espe.edu.ec.fleet_service.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FleetService implements IVehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final RepartidorRepository repartidorRepository;

    public FleetService(VehiculoRepository vehiculoRepository, RepartidorRepository repartidorRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.repartidorRepository = repartidorRepository;
    }

    //vehiculos
    @Transactional
    public Vehiculo crearVehiculo(Vehiculo vehiculo) {
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new RuntimeException("Ya existe un vehículo con la placa: " + vehiculo.getPlaca());
        }
        if (vehiculo.getEstado() == null) {
            vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
        }
        return vehiculoRepository.save(vehiculo);
    }

    public List<Vehiculo> listarVehiculos() {
        return vehiculoRepository.findAll();
    }

    public Vehiculo buscarVehiculoPorPlaca(String placa) {
        return vehiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));
    }

    @Transactional
    public Vehiculo actualizarEstadoVehiculo(String placa, EstadoVehiculo nuevoEstado) {
        Vehiculo vehiculo = buscarVehiculoPorPlaca(placa);
        vehiculo.setEstado(nuevoEstado);
        return vehiculoRepository.save(vehiculo);
    }

    //repartidores
    @Transactional
    public Repartidor registrarRepartidor(Repartidor repartidor) {
        if (repartidorRepository.existsByIdentificacion(repartidor.getIdentificacion())) {
            throw new RuntimeException("El repartidor con esa cedula si existe");
        }
        return repartidorRepository.save(repartidor);
    }

    public List<Repartidor> listarRepartidores() {
        return repartidorRepository.findAll();
    }

    @Transactional
    public Repartidor asignarVehiculo(Long repartidorId, String placaVehiculo) {
        @SuppressWarnings("null")
        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Repartidor no encontrado"));

        Vehiculo vehiculo = buscarVehiculoPorPlaca(placaVehiculo);

        repartidor.setVehiculo(vehiculo);
        return repartidorRepository.save(repartidor);
    }

    public List<Vehiculo> obtenerVehiculosDisponibles(String zonaId, String fleetType) {
        List<Vehiculo> vehiculos = vehiculoRepository.findByEstado(EstadoVehiculo.DISPONIBLE);
        
        // Filtrar por tipo si se proporciona
        if (fleetType != null && !fleetType.isEmpty()) {
            vehiculos = vehiculos.stream()
                    .filter(v -> v.getTipoVehiculo().equalsIgnoreCase(fleetType))
                    .toList();
        }
        
        return vehiculos;
    }
}
