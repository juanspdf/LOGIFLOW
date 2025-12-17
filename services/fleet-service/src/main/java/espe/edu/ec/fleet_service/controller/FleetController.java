package espe.edu.ec.fleet_service.controller;

import espe.edu.ec.fleet_service.model.EstadoVehiculo;
import espe.edu.ec.fleet_service.model.Repartidor;
import espe.edu.ec.fleet_service.model.Vehiculo;
import espe.edu.ec.fleet_service.service.FleetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fleet")
public class FleetController {
    private final FleetService fleetService;

    public FleetController(FleetService fleetService) { this.fleetService = fleetService; }

    @PostMapping("/vehiculos")
    public ResponseEntity<Vehiculo> crearVehiculo(@Valid @RequestBody Vehiculo vehiculo) {
        return ResponseEntity.ok(fleetService.crearVehiculo(vehiculo));
    }

    @GetMapping("/vehiculos")
    public ResponseEntity<List<Vehiculo>> listarVehiculos() {
        return ResponseEntity.ok(fleetService.listarVehiculos());
    }

    @GetMapping("/vehiculos/{placa}")
    public ResponseEntity<Vehiculo> buscarVehiculo(@PathVariable String placa) {
        return ResponseEntity.ok(fleetService.buscarVehiculoPorPlaca(placa));
    }

    @PatchMapping("/vehiculos/{placa}/estado")
    public ResponseEntity<Vehiculo> actualizarEstado(
            @PathVariable String placa,
            @RequestParam EstadoVehiculo estado) {
        return ResponseEntity.ok(fleetService.actualizarEstadoVehiculo(placa, estado));
    }

    @GetMapping("/disponible")
    public ResponseEntity<List<Vehiculo>> obtenerVehiculosDisponibles(
            @RequestParam(required = false) String zonaId,
            @RequestParam(required = false) String fleetType) {
        return ResponseEntity.ok(fleetService.obtenerVehiculosDisponibles(zonaId, fleetType));
    }

    @PostMapping("/repartidores")
    public ResponseEntity<Repartidor> crearRepartidor(@Valid @RequestBody Repartidor repartidor) {
        return ResponseEntity.ok(fleetService.registrarRepartidor(repartidor));
    }

    @GetMapping("/repartidores")
    public ResponseEntity<List<Repartidor>> listarRepartidores() {
        return ResponseEntity.ok(fleetService.listarRepartidores());
    }

    @PutMapping("/repartidores/{id}/asignar-vehiculo")
    public ResponseEntity<Repartidor> asignarVehiculo(
            @PathVariable Long id,
            @RequestParam String placa) {
        return ResponseEntity.ok(fleetService.asignarVehiculo(id, placa));
    }
}
