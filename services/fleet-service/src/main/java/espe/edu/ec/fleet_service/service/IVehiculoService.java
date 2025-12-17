package espe.edu.ec.fleet_service.service;

import espe.edu.ec.fleet_service.model.EstadoVehiculo;
import espe.edu.ec.fleet_service.model.Vehiculo;

import java.util.List;

public interface IVehiculoService {
    Vehiculo crearVehiculo(Vehiculo vehiculo);
    List<Vehiculo> listarVehiculos();
    Vehiculo buscarVehiculoPorPlaca(String placa);
    Vehiculo actualizarEstadoVehiculo(String placa, EstadoVehiculo nuevoEstado);
    List<Vehiculo> obtenerVehiculosDisponibles(String zonaId, String fleetType);
}
